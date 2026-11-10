package com.app.idoneos.controller;

import com.app.idoneos.util.FiltroUtil;

import com.app.idoneos.util.PaginacionUtil;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.app.idoneos.model.Alumno;
import com.app.idoneos.model.Comprobante;
import com.app.idoneos.model.Curso;
import com.app.idoneos.model.Inscripcion;
import com.app.idoneos.model.Pago;
import com.app.idoneos.model.Parametro;
import com.app.idoneos.model.Usuario;
import com.app.idoneos.service.comprobante.ComprobanteService;
import com.app.idoneos.service.inscripcion.InscripcionService;
import com.app.idoneos.service.pago.PagoService;
import com.app.idoneos.service.parametro.ParametroService;
import com.app.idoneos.service.pasarelapago.PasarelaPagoService;
import com.app.idoneos.util.RespuestaFormularioUtil;

/** Controlador del pago. */
@Controller
@RequestMapping("/pago")
public class PagoController {

    private static final Logger REGISTRO = LoggerFactory.getLogger(PagoController.class);

    @Autowired
    private PagoService pagoService;

    @Autowired
    private InscripcionService inscripcionService;

    @Autowired
    private ParametroService parametroService;

    @Autowired
    private PasarelaPagoService pasarelaPagoService;

    @Autowired
    private ComprobanteService comprobanteService;

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /** GET /pago/buscar: Lista los pagos de todos los alumnos (vista del Administrador). */
    @GetMapping("/buscar")
    public String buscar(@RequestParam(value = "registrarPara", required = false) Integer registrarPara, Model model,
            Authentication authentication, jakarta.servlet.http.HttpServletRequest request) {
        if (!((Usuario) authentication.getPrincipal()).esAdministradorActivo()) {
            return buscarPorAlumno(model, authentication, request);
        }
        List<Pago> pagos = pagoService.buscarConFiltros(null, null, null, null, null, null);
        // El filtro, el orden por columna y la página los resuelve el servidor (motor común de listados)
        PaginacionUtil.Pagina<Pago> pagina = Filtros.tabla.paginar(pagos, request.getParameterMap());
        PaginacionUtil.cargarEnModelo(model, pagina);
        model.addAttribute("pagos", pagina.filas());
        if (PaginacionUtil.pideSoloFilas(request)) {
            return "pages/pago/buscarPorAdministrador :: filas"; // solo las filas de la página pedida
        }
        pagos = pagina.filas(); // los montos y detalles se calculan solo para las filas que se muestran
        model.addAttribute("cursosFiltro", pagos.stream().map(pago -> pago.getInscripcion().getCurso()).distinct()
                .sorted(Comparator.comparing(Curso::getNombre, String.CASE_INSENSITIVE_ORDER)).toList());
        // Las que todavía hay que pagar: pendientes de pago o con un pago en curso
        List<Inscripcion> pendientes = inscripcionService.buscarConFiltros(null, null, null).stream()
                .filter(Inscripcion::requierePago).toList();
        model.addAttribute("inscripcionesPendientes", pendientes);
        // Lo que debe cada inscripción (precio con el descuento, menos lo ya pagado): se propone como monto y es su tope
        Map<Integer, Float> montos = new LinkedHashMap<>();
        pendientes.forEach(inscripcion -> montos.put(inscripcion.getIdInscripcion(), pagoService.calcularMontoPendiente(inscripcion)));
        pagos.forEach(pago -> montos.computeIfAbsent(pago.getInscripcion().getIdInscripcion(),
                id -> pagoService.calcularMontoPendiente(pago.getInscripcion())));
        model.addAttribute("montosSugeridos", montos);
        // Precio, descuento, lo ya pagado y lo que falta de cada inscripción pendiente, para el recuadro del formulario
        Map<Integer, PagoService.DetallePago> detalles = new LinkedHashMap<>();
        pendientes.forEach(inscripcion -> detalles.put(inscripcion.getIdInscripcion(), pagoService.calcularDetalle(inscripcion)));
        pagos.forEach(pago -> detalles.computeIfAbsent(pago.getInscripcion().getIdInscripcion(),
                id -> pagoService.calcularDetalle(pago.getInscripcion())));
        model.addAttribute("detalles", detalles);
        model.addAttribute("cbu", parametroService.buscarPorClave(Parametro.CBU_INSTITUCION).map(Parametro::getValor).orElse(null));
        model.addAttribute("registrarPara", registrarPara);
        model.addAttribute("titulo", "Pagos | Idóneos Online");
        model.addAttribute("menuActivo", "pagos");
        return "pages/pago/buscarPorAdministrador";
    }

    /** Lista los pagos del alumno. */
    private String buscarPorAlumno(Model model, Authentication authentication, jakarta.servlet.http.HttpServletRequest request) {
        Alumno alumno = ((Usuario) authentication.getPrincipal()).getAlumno();
        List<Pago> pagos = pagoService.buscarConFiltros(null, null, null, null, null, alumno);
        // El filtro, el orden por columna y la página los resuelve el servidor
        PaginacionUtil.Pagina<Pago> pagina = Filtros.tablaAlumno.paginar(pagos, request.getParameterMap());
        PaginacionUtil.cargarEnModelo(model, pagina);
        model.addAttribute("pagos", pagina.filas());
        if (PaginacionUtil.pideSoloFilas(request)) {
            return "pages/pago/buscarPorAlumno :: filas"; // solo las filas de la página pedida
        }
        model.addAttribute("cursosFiltro", pagos.stream().map(pago -> pago.getInscripcion().getCurso()).distinct()
                .sorted(Comparator.comparing(Curso::getNombre, String.CASE_INSENSITIVE_ORDER)).toList());
        model.addAttribute("titulo", "Mis pagos | Idóneos Online");
        return "pages/pago/buscarPorAlumno";
    }

    /** GET /pago/caja/{idInscripcion}: Muestra la caja de una inscripción pendiente de pago: qué se compra, el descuento aplicado. */
    @GetMapping("/caja/{idInscripcion}")
    public String verCaja(@PathVariable("idInscripcion") Integer idInscripcion, Model model, Authentication authentication,
            RedirectAttributes redirectAttributes,
            @RequestParam(value = "inscripcionNueva", required = false) String inscripcionNueva) {
        Inscripcion inscripcion = inscripcionService.buscarMisCursos(((Usuario) authentication.getPrincipal()).getAlumno(), null, null)
                .stream().filter(i -> i.getIdInscripcion() == idInscripcion).findFirst().orElse(null);
        if (inscripcion == null) {
            redirectAttributes.addFlashAttribute("error", "Error! La inscripción no se encuentra disponible.");
            return "redirect:/inscripcion/buscar";
        }
        if (inscripcion.fueHabilitada()) {
            redirectAttributes.addFlashAttribute("error", "Error! Esta inscripción no tiene un pago pendiente.");
            return "redirect:/inscripcion/buscar";
        }
        if (inscripcion.estaCancelada()) {
            redirectAttributes.addFlashAttribute("error", "Error! La inscripción fue cancelada: no se puede pagar.");
            return "redirect:/inscripcion/buscar";
        }
        // Cerrada la inscripción solo se puede continuar un pago ya iniciado
        if (inscripcion.getCohorte().inscripcionCerrada() && inscripcion.getPagoEnCurso() == null) {
            redirectAttributes.addFlashAttribute("error", "Error! La inscripción a esta cohorte ya cerró: no se puede pagar.");
            return "redirect:/inscripcion/buscar";
        }
        model.addAttribute("inscripcion", inscripcion);
        model.addAttribute("inscripcionNueva", inscripcionNueva != null);
        // Sin lugar: no ocupa cupo y la cohorte ya no tiene (se libera si alguien no completa su pago)
        model.addAttribute("sinCupo", !inscripcion.ocupaCupo() && !inscripcion.getCohorte().tieneCupo());
        model.addAttribute("descuento", pagoService.calcularDescuento(inscripcion).orElse(null));
        model.addAttribute("total", pagoService.calcularMontoAPagar(inscripcion));
        model.addAttribute("parcial", inscripcion.tienePagoAprobado());
        model.addAttribute("pagado", inscripcion.getMontoPagado());
        model.addAttribute("titulo", "Caja | Idóneos Online");
        return "pages/pago/caja";
    }

    /** GET /pago/estado/{idPago}: Consulta el estado de un pago (lo concilia con MODO si está pendiente). */
    @GetMapping("/estado/{idPago}")
    public ResponseEntity<Map<String, Object>> estado(@PathVariable("idPago") Integer idPago, Authentication authentication) {
        try {
            Pago pago = pagoService.consultarEstado(idPago, solicitante((Usuario) authentication.getPrincipal()));
            Map<String, Object> cuerpo = new LinkedHashMap<>();
            cuerpo.put("estado", pago.getEstadoPago().getNombre());
            cuerpo.put("motivo", pago.getMotivoRechazo());
            cuerpo.put("idPago", pago.getIdPago());
            cuerpo.put("verificando", pagoService.estaVerificando(pago));
            return RespuestaFormularioUtil.armarRespuestaConDatos(cuerpo);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConErrorEnDatos(e);
        }
    }

    /** GET /pago/exito/{idPago}: Retorno del sitio de pagos de MODO cuando el pago se aprobó. */
    @GetMapping("/exito/{idPago}")
    public String exito(@PathVariable("idPago") Integer idPago, Model model, Authentication authentication) {
        Pago pago = buscarPagoDelRetorno(idPago, authentication);
        if (pago.estaRechazado()) {
            return "redirect:/pago/error/" + idPago;
        }
        cargarRetorno(model, pago, pago.estaAprobado() ? "aprobado" : "pendiente");
        return "pages/pago/retorno";
    }

    /** GET /pago/error/{idPago}: Retorno del sitio de pagos de MODO cuando el pago se rechazó, falló o el alumno lo abandonó. */
    @GetMapping("/error/{idPago}")
    public String error(@PathVariable("idPago") Integer idPago, Model model, Authentication authentication) {
        Pago pago = buscarPagoDelRetorno(idPago, authentication);
        if (pago.estaAprobado()) {
            return "redirect:/pago/exito/" + idPago;
        }
        cargarRetorno(model, pago, "rechazado");
        return "pages/pago/retorno";
    }

    /** Busca el pago al que vuelve el alumno, conciliado con MODO. Si no es suyo o no existe, no se encuentra. */
    private Pago buscarPagoDelRetorno(Integer idPago, Authentication authentication) {
        try {
            return pagoService.consultarEstado(idPago, solicitante((Usuario) authentication.getPrincipal()));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    private void cargarRetorno(Model model, Pago pago, String resultado) {
        model.addAttribute("pago", pago);
        model.addAttribute("resultado", resultado);
        model.addAttribute("inscripcion", pago.getInscripcion());
        model.addAttribute("motivo", pago.getMotivoRechazo());
        model.addAttribute("plazoMinutos", pagoService.plazoConfirmacionMinutos());
        model.addAttribute("titulo", "Resultado del pago | Idóneos Online");
    }

    /** GET /pago/comprobante/{idPago}: Descarga el comprobante (PDF) de un pago aprobado. */
    @GetMapping("/comprobante/{idPago}")
    public ResponseEntity<byte[]> comprobante(@PathVariable("idPago") Integer idPago, Authentication authentication) {
        Pago pago;
        try {
            pago = pagoService.buscarVisible(idPago, solicitante((Usuario) authentication.getPrincipal()));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        Comprobante comprobante = pago.getComprobanteVigente();
        if (!pago.estaAprobado() || comprobante == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + comprobante.getNumero() + ".pdf\"")
                .body(comprobanteService.generarPdf(comprobante));
    }

    // ----------------------------------------------------------------------
    // Creaciones
    // ----------------------------------------------------------------------

    /** POST /pago/pagar/{idInscripcion}: El alumno paga una inscripción pendiente de pago. */
    @PostMapping("/pagar/{idInscripcion}")
    public ResponseEntity<Map<String, Object>> pagar(@PathVariable("idInscripcion") Integer idInscripcion,
            @RequestParam(value = "monto", required = false) Float monto, Authentication authentication,
            jakarta.servlet.http.HttpServletRequest request) {
        try {
            return RespuestaFormularioUtil.armarRespuestaConDatos(armarRespuesta(pagoService.iniciar(idInscripcion,
                    ((Usuario) authentication.getPrincipal()).getAlumno(), monto), request));
        } catch (PagoService.MontoCambiadoException e) {
            // La caja tiene que recargarse para mostrar el monto actual
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage(), "recargar", true));
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConErrorEnDatos(e);
        }
    }

    /** POST /pago/registrar: El administrador registra el pago por transferencia bancaria de una inscripción. */
    @PostMapping("/registrar")
    public ResponseEntity<Map<String, String>> registrar(
            @RequestParam(value = "idInscripcion", required = false) Integer idInscripcion,
            @RequestParam(value = "monto", required = false) Float monto,
            @RequestParam(value = "numeroComprobante", required = false) String numeroComprobante,
            @RequestParam(value = "observaciones", required = false) String observaciones) {
        try {
            if (idInscripcion == null) {
                throw RespuestaFormularioUtil.armarCampoInvalido("idInscripcion", "Error! Debe seleccionar la inscripción.");
            }
            Pago pago = pagoService.registrarManual(idInscripcion, monto, numeroComprobante, observaciones);
            Inscripcion inscripcion = pago.getInscripcion();
            String alumno = inscripcion.getAlumno().getUsuario().getNombreCompleto();
            return RespuestaFormularioUtil.armarRespuestaExitosa(inscripcion.fueHabilitada()
                    ? "Pago de " + alumno + " registrado con éxito. Se habilitó el acceso al curso y se emitió el comprobante."
                    : "Pago de " + alumno + " registrado, pero no cubre el precio del curso: la inscripción sigue pendiente de pago. "
                            + "Faltan $" + String.format(java.util.Locale.forLanguageTag("es-AR"), "%,.2f",
                                    pagoService.calcularMontoRequerido(inscripcion) - inscripcion.getMontoPagado()) + ".");
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    // ----------------------------------------------------------------------
    // Modificaciones
    // ----------------------------------------------------------------------

    /** POST /pago/modificar/{id}: El administrador acredita o rechaza un pago pendiente o rechazado. */
    @PostMapping("/modificar/{id}")
    public ResponseEntity<Map<String, String>> modificar(@PathVariable("id") Integer id,
            @RequestParam(value = "estado", required = false) String estado,
            @RequestParam(value = "monto", required = false) Float monto,
            @RequestParam(value = "referencia", required = false) String referencia,
            @RequestParam(value = "numeroComprobante", required = false) String numeroComprobante,
            @RequestParam(value = "nombrePagador", required = false) String nombrePagador,
            @RequestParam(value = "dniPagador", required = false) String dniPagador,
            @RequestParam(value = "motivo", required = false) String motivo) {
        try {
            Pago pago = pagoService.modificar(id, estado, monto, referencia, numeroComprobante, nombrePagador, dniPagador, motivo);
            return RespuestaFormularioUtil.armarRespuestaExitosa(pago.estaAprobado()
                    ? "Pago acreditado con éxito. Se habilitó el acceso al curso y se emitió el comprobante."
                    : "Pago actualizado con éxito.");
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    /** POST /pago/reintentar/{idPago}: El alumno reintenta un pago rechazado, reutilizando su registro. */
    @PostMapping("/reintentar/{idPago}")
    public ResponseEntity<Map<String, Object>> reintentar(@PathVariable("idPago") Integer idPago, Authentication authentication,
            jakarta.servlet.http.HttpServletRequest request) {
        try {
            return RespuestaFormularioUtil.armarRespuestaConDatos(armarRespuesta(pagoService.reintentar(idPago,
                    ((Usuario) authentication.getPrincipal()).getAlumno()), request));
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConErrorEnDatos(e);
        }
    }

    /** POST /pago/webhook: Aviso de MODO de que cambió el estado de un pago (webhook). */
    @PostMapping("/webhook")
    public ResponseEntity<Map<String, Object>> webhook(@RequestBody(required = false) Map<String, Object> cuerpo) {
        try {
            if (cuerpo != null) {
                String request = texto(cuerpo.get("id"));
                if (request == null) {
                    request = texto(cuerpo.get("payment_request_id"));
                }
                pagoService.procesarNotificacion(request, texto(cuerpo.get("external_intention_id")));
            }
        } catch (RuntimeException e) {
            REGISTRO.warn("No se pudo procesar el aviso de MODO", e);
        }
        return RespuestaFormularioUtil.armarRespuestaConDatos(Map.of("recibido", true));
    }

    /** Resuelve para quién se consulta un pago: null si es el administrador (ve todos) o el alumno si es uno. */
    private Alumno solicitante(Usuario usuario) {
        if (usuario.esAdministradorActivo()) {
            return null;
        }
        if (usuario.getAlumno() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return usuario.getAlumno();
    }

    private Map<String, Object> armarRespuesta(PagoService.SolicitudPago solicitud, jakarta.servlet.http.HttpServletRequest request) {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("idPago", solicitud.idPago());
        cuerpo.put("idSolicitud", solicitud.idSolicitud());
        cuerpo.put("aprobado", solicitud.aprobado());
        // Con el pago ya aprobado (o sin nada que cobrar) no hay a dónde redirigir
        if (!solicitud.aprobado()) {
            String base = parametroService.obtenerTexto(Parametro.MODO_URL_PUBLICA).map(String::trim).filter(v -> !v.isEmpty())
                    .map(v -> v.replaceAll("/+$", ""))
                    .orElseGet(() -> org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromContextPath(request)
                            .build().toUriString());
            cuerpo.put("urlCheckout", pasarelaPagoService.urlCheckout(solicitud.idSolicitud(),
                    base + "/pago/exito/" + solicitud.idPago(), base + "/pago/error/" + solicitud.idPago()));
        }
        return cuerpo;
    }

    private static String texto(Object valor) {
        return valor == null || valor.toString().isBlank() ? null : valor.toString();
    }

    // ----------------------------------------------------------------------
    // Ventana de modificación a pedido
    // ----------------------------------------------------------------------

    /** GET /pago/modificar/{id}: Ventana de modificación de un pago, que el listado pide al abrirla (en lugar de dibujar una por fila). */
    @GetMapping("/modificar/{id}")
    public String modificar(@PathVariable("id") Integer id, Model model) {
        Pago p = pagoService.buscarPorId(id).filter(Pago::estaPendiente)
                .orElseThrow(() -> new IllegalArgumentException("Error! El pago no se encuentra disponible para modificar."));
        model.addAttribute("p", p);
        model.addAttribute("detalles", Map.of(p.getInscripcion().getIdInscripcion(), pagoService.calcularDetalle(p.getInscripcion())));
        return "pages/pago/modificar :: modal(p=${p})";
    }

    /** Columnas de filtro, orden y página de los listados (las aplica el servidor). */
    public static final class Filtros {

        private Filtros() {
        }

        // ----------------------------------------------------------------------
        // Listado del administrador (motor común)
        // ----------------------------------------------------------------------

        /** Define las columnas del listado, que se filtran y ordenan en el servidor. */
        public static final FiltroUtil<Pago> tabla = new FiltroUtil<>(List.of(
                new FiltroUtil.Columna<Pago>("alumno", FiltroUtil.Tipo.TEXTO,
                        p -> p.getInscripcion().getAlumno().getUsuario().getNombreCompleto() + " "
                                + p.getInscripcion().getAlumno().getUsuario().getDni() + " "
                                + p.getInscripcion().getAlumno().getUsuario().getCorreo()),
                new FiltroUtil.Columna<Pago>("curso", FiltroUtil.Tipo.LISTA, p -> p.getInscripcion().getCurso().getNombre()),
                new FiltroUtil.Columna<Pago>("monto", FiltroUtil.Tipo.NUMERO, p -> String.valueOf(p.getMonto())),
                new FiltroUtil.Columna<Pago>("medioDePago", FiltroUtil.Tipo.LISTA, p -> p.getMetodoPago().getNombre()),
                new FiltroUtil.Columna<Pago>("estado", FiltroUtil.Tipo.LISTA, p -> p.getEstadoPago().getNombre()),
                new FiltroUtil.Columna<Pago>("motivo", FiltroUtil.Tipo.TEXTO,
                        p -> p.getMotivoRechazo() == null ? "" : p.getMotivoRechazo()),
                new FiltroUtil.Columna<Pago>("comprobante", FiltroUtil.Tipo.TEXTO,
                        p -> p.getComprobanteVigente() == null ? "" : p.getComprobanteVigente().getNumero()),
                new FiltroUtil.Columna<Pago>("fechaAprobacion", FiltroUtil.Tipo.FECHA,
                        p -> FiltroUtil.formatearFecha(p.getFechaAprobacion())),
                new FiltroUtil.Columna<Pago>("fechaCreacion", FiltroUtil.Tipo.FECHA,
                        p -> FiltroUtil.formatearFechaHora(p.getFechaCreacion()))),
                p -> false);

        /** Define las columnas de "Mis pagos", que se filtran y ordenan en el servidor. */
        public static final FiltroUtil<Pago> tablaAlumno = new FiltroUtil<>(List.of(
                new FiltroUtil.Columna<Pago>("curso", FiltroUtil.Tipo.LISTA, p -> p.getInscripcion().getCurso().getNombre()),
                new FiltroUtil.Columna<Pago>("monto", FiltroUtil.Tipo.NUMERO, p -> String.valueOf(p.getMonto())),
                new FiltroUtil.Columna<Pago>("medioDePago", FiltroUtil.Tipo.LISTA, p -> p.getMetodoPago().getNombre()),
                new FiltroUtil.Columna<Pago>("estado", FiltroUtil.Tipo.LISTA, p -> p.getEstadoPago().getNombre()),
                new FiltroUtil.Columna<Pago>("motivo", FiltroUtil.Tipo.TEXTO,
                        p -> p.getMotivoRechazo() == null ? "" : p.getMotivoRechazo()),
                new FiltroUtil.Columna<Pago>("fechaCreacion", FiltroUtil.Tipo.FECHA,
                        p -> FiltroUtil.formatearFechaHora(p.getFechaCreacion()))),
                p -> false);
    }
}
