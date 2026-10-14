package com.app.idoneos.controlador;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import com.app.idoneos.modelo.Alumno;
import com.app.idoneos.modelo.Comprobante;
import com.app.idoneos.modelo.Curso;
import com.app.idoneos.modelo.Inscripcion;
import com.app.idoneos.modelo.Pago;
import com.app.idoneos.modelo.Parametro;
import com.app.idoneos.modelo.Usuario;
import com.app.idoneos.servicio.Inscripcion.InscripcionServicio;
import com.app.idoneos.servicio.Pago.PagoServicio;
import com.app.idoneos.servicio.Pago.SolicitudDePago;
import com.app.idoneos.servicio.Parametro.ParametroServicio;
import com.app.idoneos.utilidades.ComprobanteUtilidad;
import com.app.idoneos.utilidades.RespuestaUtilidad;

/**
 * Controlador de los pagos de las inscripciones (MOD-F-03).
 *
 * Mapea las pantallas y operaciones de los casos de uso: CU-50 Buscar pago (GET /pago/buscar del Administrador y
 * GET /pago/misPagos del Alumno, con la descarga del comprobante), CU-51 Realizar pago (POST /pago/pagar y el aviso
 * de MODO en POST /pago/webhook), CU-52 Reintentar pago, CU-53 Registrar pago (transferencia) y CU-54 Modificar pago.
 */
@Controller
@RequestMapping("/pago")
public class PagoControlador {

    private static final Logger REGISTRO = LoggerFactory.getLogger(PagoControlador.class);

    @Autowired
    private PagoServicio pagoServicio;

    @Autowired
    private InscripcionServicio inscripcionServicio;

    @Autowired
    private ParametroServicio parametroServicio;

    /**
     * CU-50: Lista los pagos de todos los alumnos (vista del Administrador). El listado llega completo y los filtros
     * se aplican en la pantalla.
     *
     * @param registrarPara Inscripción para la que se abre la ventana de registro de pago (opcional).
     * @param modelo        El modelo de la vista.
     * @return La vista de búsqueda de pagos.
     */
    @GetMapping("/buscar")
    public String buscarPagos(@RequestParam(value = "registrarPara", required = false) Integer registrarPara, Model modelo) {
        List<Pago> pagos = pagoServicio.buscarConFiltros(null, null, null, null, null, null);
        modelo.addAttribute("pagos", pagos);
        modelo.addAttribute("cursosFiltro", pagos.stream().map(pago -> pago.getInscripcion().getCurso()).distinct()
                .sorted(Comparator.comparing(Curso::getNombre, String.CASE_INSENSITIVE_ORDER)).toList());
        List<Inscripcion> pendientes = inscripcionServicio.buscarConFiltros(null, null, Inscripcion.ACCESO_PENDIENTE_DE_PAGO);
        modelo.addAttribute("inscripcionesPendientes", pendientes);
        // Lo que se le cobra a cada alumno (precio con el descuento que le corresponde), para proponerlo como monto
        Map<Integer, Float> montos = new LinkedHashMap<>();
        pendientes.forEach(inscripcion -> montos.put(inscripcion.getIdInscripcion(), pagoServicio.calcularMontoAPagar(inscripcion)));
        modelo.addAttribute("montosSugeridos", montos);
        modelo.addAttribute("cbu", parametroServicio.buscarPorClave(Parametro.CBU_INSTITUCION).map(Parametro::getValor).orElse(null));
        modelo.addAttribute("registrarPara", registrarPara);
        modelo.addAttribute("titulo", "Pagos | Idóneos Online");
        modelo.addAttribute("menuActivo", "pagos");
        return "pages/pago/buscarAdministrador";
    }

    /**
     * CU-50: Lista los pagos del alumno. Desde acá reintenta los rechazados (CU-52) y descarga los comprobantes.
     *
     * @param modelo El modelo de la vista.
     * @param auth   La autenticación actual.
     * @return La vista de "Mis pagos".
     */
    @GetMapping("/misPagos")
    public String verMisPagos(Model modelo, Authentication auth) {
        Alumno alumno = ((Usuario) auth.getPrincipal()).getAlumno();
        List<Pago> pagos = pagoServicio.buscarConFiltros(null, null, null, null, null, alumno);
        modelo.addAttribute("pagos", pagos);
        modelo.addAttribute("cursosFiltro", pagos.stream().map(pago -> pago.getInscripcion().getCurso()).distinct()
                .sorted(Comparator.comparing(Curso::getNombre, String.CASE_INSENSITIVE_ORDER)).toList());
        modelo.addAttribute("titulo", "Mis pagos | Idóneos Online");
        return "pages/pago/misPagos";
    }

    /**
     * CU-51: El alumno paga una inscripción pendiente de pago. Registra el pago pendiente y crea la solicitud en MODO;
     * responde con los datos que necesita el navegador para mostrar el QR. Responde en JSON.
     *
     * @param idInscripcion Identificador de la inscripción.
     * @param auth          La autenticación actual.
     * @return Los datos de la solicitud de pago o el mensaje de error.
     */
    @PostMapping("/pagar/{idInscripcion}")
    public ResponseEntity<Map<String, Object>> pagar(@PathVariable("idInscripcion") Integer idInscripcion,
            Authentication auth) {
        try {
            return ResponseEntity.ok(armarRespuesta(pagoServicio.iniciarPago(idInscripcion,
                    ((Usuario) auth.getPrincipal()).getAlumno())));
        } catch (Exception e) {
            return errorJson(e);
        }
    }

    /**
     * CU-52: El alumno reintenta un pago rechazado, reutilizando su registro. Responde en JSON.
     *
     * @param idPago Identificador del pago rechazado.
     * @param auth   La autenticación actual.
     * @return Los datos de la nueva solicitud de pago o el mensaje de error.
     */
    @PostMapping("/reintentar/{idPago}")
    public ResponseEntity<Map<String, Object>> reintentar(@PathVariable("idPago") Integer idPago, Authentication auth) {
        try {
            return ResponseEntity.ok(armarRespuesta(pagoServicio.reintentarPago(idPago,
                    ((Usuario) auth.getPrincipal()).getAlumno())));
        } catch (Exception e) {
            return errorJson(e);
        }
    }

    /**
     * Consulta el estado de un pago (lo concilia con MODO si está pendiente). El navegador la consulta cada pocos
     * segundos mientras el alumno paga. Responde en JSON.
     *
     * @param idPago Identificador del pago.
     * @param auth   La autenticación actual.
     * @return El estado del pago y, si fue rechazado, el motivo.
     */
    @GetMapping("/estado/{idPago}")
    public ResponseEntity<Map<String, Object>> estado(@PathVariable("idPago") Integer idPago, Authentication auth) {
        try {
            Pago pago = pagoServicio.consultarEstado(idPago, solicitante((Usuario) auth.getPrincipal()));
            Map<String, Object> cuerpo = new LinkedHashMap<>();
            cuerpo.put("estado", pago.getEstadoPago().getNombre());
            cuerpo.put("motivo", pago.getDetalleErrorOperacion());
            cuerpo.put("idPago", pago.getIdPago());
            return ResponseEntity.ok(cuerpo);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            return errorJson(e);
        }
    }

    /**
     * Aviso de MODO de que cambió el estado de un pago (webhook). Responde siempre 200 de inmediato, como exige MODO: el
     * aviso solo identifica el pago, y el resultado se toma consultándolo a MODO (por eso un aviso falso no puede
     * aprobar nada).
     *
     * @param cuerpo El contenido del aviso.
     * @return Una respuesta vacía con estado 200.
     */
    @PostMapping("/webhook")
    public ResponseEntity<Map<String, Object>> webhook(@RequestBody(required = false) Map<String, Object> cuerpo) {
        try {
            if (cuerpo != null) {
                String solicitud = texto(cuerpo.get("id"));
                if (solicitud == null) {
                    solicitud = texto(cuerpo.get("payment_request_id"));
                }
                pagoServicio.procesarNotificacion(solicitud, texto(cuerpo.get("external_intention_id")));
            }
        } catch (RuntimeException e) {
            REGISTRO.warn("No se pudo procesar el aviso de MODO", e);
        }
        return ResponseEntity.ok(Map.of("recibido", true));
    }

    /**
     * CU-53: El administrador registra el pago por transferencia bancaria de una inscripción. Responde en JSON.
     *
     * @param idInscripcion Identificador de la inscripción.
     * @param monto         El monto recibido.
     * @param referencia    El comprobante de la transferencia.
     * @param observaciones Notas (opcional).
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/registrar")
    public ResponseEntity<Map<String, String>> registrar(
            @RequestParam(value = "idInscripcion", required = false) Integer idInscripcion,
            @RequestParam(value = "monto", required = false) Float monto,
            @RequestParam(value = "referencia", required = false) String referencia,
            @RequestParam(value = "observaciones", required = false) String observaciones) {
        try {
            if (idInscripcion == null) {
                throw new IllegalArgumentException("Error! Debe seleccionar la inscripción.");
            }
            Pago pago = pagoServicio.registrarPagoManual(idInscripcion, monto, referencia, observaciones);
            return RespuestaUtilidad.respuestaExitosa("Pago de " + pago.getInscripcion().getAlumno().getUsuario().getNombreCompleto()
                    + " registrado con éxito. Se habilitó el acceso al curso y se emitió el comprobante.");
        } catch (Exception e) {
            return RespuestaUtilidad.respuestaConError(e);
        }
    }

    /**
     * CU-54: El administrador acredita o rechaza un pago pendiente o rechazado. Responde en JSON.
     *
     * @param id            Identificador del pago.
     * @param estado        Aprobado o Rechazado.
     * @param monto         El monto.
     * @param referencia    El comprobante de la transferencia.
     * @param nombrePagador El nombre del pagador (al aprobar un pago de MODO).
     * @param dniPagador    El DNI del pagador (al aprobar un pago de MODO).
     * @param motivo        El motivo del rechazo (opcional).
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/modificar/{id}")
    public ResponseEntity<Map<String, String>> modificar(@PathVariable("id") Integer id,
            @RequestParam(value = "estado", required = false) String estado,
            @RequestParam(value = "monto", required = false) Float monto,
            @RequestParam(value = "referencia", required = false) String referencia,
            @RequestParam(value = "nombrePagador", required = false) String nombrePagador,
            @RequestParam(value = "dniPagador", required = false) String dniPagador,
            @RequestParam(value = "motivo", required = false) String motivo) {
        try {
            Pago pago = pagoServicio.modificarPago(id, estado, monto, referencia, nombrePagador, dniPagador, motivo);
            return RespuestaUtilidad.respuestaExitosa(pago.estaAprobado()
                    ? "Pago acreditado con éxito. Se habilitó el acceso al curso y se emitió el comprobante."
                    : "Pago actualizado con éxito.");
        } catch (Exception e) {
            return RespuestaUtilidad.respuestaConError(e);
        }
    }

    /**
     * CU-50: Descarga el comprobante (PDF) de un pago aprobado: el alumno el de sus pagos y el administrador cualquiera.
     *
     * @param idPago Identificador del pago.
     * @param auth   La autenticación actual.
     * @return El archivo PDF.
     */
    @GetMapping("/comprobante/{idPago}")
    public ResponseEntity<byte[]> comprobante(@PathVariable("idPago") Integer idPago, Authentication auth) {
        Pago pago;
        try {
            pago = pagoServicio.buscarVisible(idPago, solicitante((Usuario) auth.getPrincipal()));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        Comprobante comprobante = pago.getComprobanteVigente();
        if (!pago.estaAprobado() || comprobante == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + comprobante.getNumero() + ".pdf\"")
                .body(ComprobanteUtilidad.generarPdf(comprobante));
    }

    /**
     * Resuelve para quién se consulta un pago: {@code null} si es el administrador (ve todos) o el alumno si es uno.
     */
    private Alumno solicitante(Usuario usuario) {
        if (usuario.esAdministradorActivo()) {
            return null;
        }
        if (usuario.getAlumno() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return usuario.getAlumno();
    }

    private Map<String, Object> armarRespuesta(SolicitudDePago solicitud) {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("idPago", solicitud.idPago());
        cuerpo.put("idSolicitud", solicitud.idSolicitud());
        cuerpo.put("qr", solicitud.qr());
        cuerpo.put("deeplink", solicitud.deeplink());
        cuerpo.put("simulado", solicitud.simulado());
        cuerpo.put("scriptModal", solicitud.scriptModal());
        cuerpo.put("aprobado", solicitud.aprobado());
        cuerpo.put("plazoMinutos", pagoServicio.plazoConfirmacionMinutos());
        return cuerpo;
    }

    private ResponseEntity<Map<String, Object>> errorJson(Exception e) {
        ResponseEntity<Map<String, String>> error = RespuestaUtilidad.respuestaConError(e);
        return ResponseEntity.status(error.getStatusCode()).body(Map.of("error", error.getBody().get("error")));
    }

    private static String texto(Object valor) {
        return valor == null || valor.toString().isBlank() ? null : valor.toString();
    }
}
