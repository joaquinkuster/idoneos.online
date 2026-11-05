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

/**
 * Controlador de los pagos de las inscripciones (MOD-F-03).
 *
 * Mapea las pantallas y operaciones de los casos de uso: CU-50 Buscar pago (GET /pago/buscar del Administrador y
 * GET /pago/buscar del Alumno, con la descarga del comprobante), CU-51 Realizar pago (POST /pago/pagar y el aviso
 * de MODO en POST /pago/webhook), CU-52 Reintentar pago, CU-53 Registrar pago (transferencia) y CU-54 Modificar pago.
 */
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

    // ---------------------------------------------------------------- Lecturas

    /**
     * CU-50: Lista los pagos de todos los alumnos (vista del Administrador). El listado llega completo y los filtros
     * se aplican en la pantalla.
     *
     * @param registrarPara Inscripción para la que se abre la ventana de registro de pago (opcional).
     * @param model        El modelo de la vista.
     * @return La vista de búsqueda de pagos.
     */
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
        List<Inscripcion> pendientes = inscripcionService.buscarConFiltros(null, null, Inscripcion.ACCESO_PENDIENTE_DE_PAGO);
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

    /**
     * CU-50: Lista los pagos del alumno. Desde acá reintenta los rechazados (CU-52) y descarga los comprobantes.
     *
     * @param model El modelo de la vista.
     * @param authentication   La autenticación actual.
     * @return La vista de "Mis pagos".
     */
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

    /**
     * CU-51: Muestra la caja de una inscripción pendiente de pago: qué se compra, el descuento aplicado, el total y el
     * botón para pagar. A ella llevan la inscripción a un curso con costo y el botón «Pagar» de Mis cursos.
     *
     * @param idInscripcion      Identificador de la inscripción.
     * @param model             El modelo de la vista.
     * @param authentication               La autenticación actual.
     * @param redirectAttributes Atributos para mensajes de redirección.
     * @return La vista de la caja, o una redirección a Mis cursos si la inscripción no se puede pagar.
     */
    @GetMapping("/caja/{idInscripcion}")
    public String verCaja(@PathVariable("idInscripcion") Integer idInscripcion, Model model, Authentication authentication,
            RedirectAttributes redirectAttributes) {
        Inscripcion inscripcion = inscripcionService.buscarMisCursos(((Usuario) authentication.getPrincipal()).getAlumno(), null, null)
                .stream().filter(i -> i.getIdInscripcion() == idInscripcion).findFirst().orElse(null);
        if (inscripcion == null) {
            redirectAttributes.addFlashAttribute("error", "Error! La inscripción no se encuentra disponible.");
            return "redirect:/inscripcion/buscar";
        }
        if (inscripcion.estaHabilitada()) {
            redirectAttributes.addFlashAttribute("error", "Error! Esta inscripción no tiene un pago pendiente.");
            return "redirect:/inscripcion/buscar";
        }
        model.addAttribute("inscripcion", inscripcion);
        model.addAttribute("descuento", pagoService.calcularDescuento(inscripcion).orElse(null));
        model.addAttribute("total", pagoService.calcularMontoAPagar(inscripcion));
        model.addAttribute("parcial", inscripcion.tienePagoAprobado());
        model.addAttribute("pagado", inscripcion.getMontoPagado());
        model.addAttribute("titulo", "Caja | Idóneos Online");
        return "pages/pago/caja";
    }

    /**
     * Consulta el estado de un pago (lo concilia con MODO si está pendiente). El navegador la consulta cada pocos
     * segundos mientras el alumno paga. Responde en JSON.
     *
     * @param idPago Identificador del pago.
     * @param authentication   La autenticación actual.
     * @return El estado del pago y, si fue rechazado, el motivo.
     */
    @GetMapping("/estado/{idPago}")
    public ResponseEntity<Map<String, Object>> estado(@PathVariable("idPago") Integer idPago, Authentication authentication) {
        try {
            Pago pago = pagoService.consultarEstado(idPago, solicitante((Usuario) authentication.getPrincipal()));
            Map<String, Object> cuerpo = new LinkedHashMap<>();
            cuerpo.put("estado", pago.getEstadoPago().getNombre());
            cuerpo.put("motivo", pago.getDetalleErrorOperacion());
            cuerpo.put("idPago", pago.getIdPago());
            return RespuestaFormularioUtil.armarRespuestaConDatos(cuerpo);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConErrorEnDatos(e);
        }
    }

    /**
     * Retorno del sitio de pagos de MODO cuando el pago se aprobó. El aviso de MODO (webhook) puede tardar unos segundos,
     * así que si el pago todavía figura pendiente la página lo consulta hasta que se resuelva.
     *
     * @param idPago Identificador del pago.
     * @param model  El modelo de la vista.
     * @param authentication La autenticación actual.
     * @return La página del resultado, o la del error si el pago resultó rechazado.
     */
    @GetMapping("/exito/{idPago}")
    public String exito(@PathVariable("idPago") Integer idPago, Model model, Authentication authentication) {
        Pago pago = buscarPagoDelRetorno(idPago, authentication);
        if (pago.estaRechazado()) {
            return "redirect:/pago/error/" + idPago;
        }
        cargarRetorno(model, pago, pago.estaAprobado() ? "aprobado" : "pendiente");
        return "pages/pago/retorno";
    }

    /**
     * Retorno del sitio de pagos de MODO cuando el pago se rechazó, falló o el alumno lo abandonó. Ofrece reintentarlo.
     *
     * @param idPago Identificador del pago.
     * @param model  El modelo de la vista.
     * @param authentication La autenticación actual.
     * @return La página del resultado, o la de éxito si en realidad el pago se aprobó.
     */
    @GetMapping("/error/{idPago}")
    public String error(@PathVariable("idPago") Integer idPago, Model model, Authentication authentication) {
        Pago pago = buscarPagoDelRetorno(idPago, authentication);
        if (pago.estaAprobado()) {
            return "redirect:/pago/exito/" + idPago;
        }
        cargarRetorno(model, pago, "rechazado");
        return "pages/pago/retorno";
    }

    /** El pago al que vuelve el alumno, conciliado con MODO; si no es suyo o no existe, no se encuentra. */
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
        model.addAttribute("motivo", pago.getDetalleErrorOperacion());
        model.addAttribute("plazoMinutos", pagoService.plazoConfirmacionMinutos());
        model.addAttribute("titulo", "Resultado del pago | Idóneos Online");
    }

    /**
     * CU-50: Descarga el comprobante (PDF) de un pago aprobado: el alumno el de sus pagos y el administrador cualquiera.
     *
     * @param idPago Identificador del pago.
     * @param authentication   La autenticación actual.
     * @return El archivo PDF.
     */
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

    // ---------------------------------------------------------------- Creaciones

    /**
     * CU-51: El alumno paga una inscripción pendiente de pago. Registra el pago pendiente y crea la solicitud en MODO;
     * responde con la dirección del sitio de pagos de MODO a la que el navegador redirige al alumno. Responde en JSON.
     *
     * @param idInscripcion Identificador de la inscripción.
     * @param monto         El monto que el alumno vio en la caja (opcional): si cambió, no se cobra y se avisa.
     * @param authentication          La autenticación actual.
     * @param request                 La petición (de ahí sale la dirección de retorno si el sistema no tiene una pública).
     * @return Los datos de la solicitud de pago o el mensaje de error.
     */
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

    /**
     * CU-53: El administrador registra el pago por transferencia bancaria de una inscripción. Responde en JSON.
     *
     * @param idInscripcion Identificador de la inscripción.
     * @param monto         El monto recibido.
     * @param numeroComprobante El número de comprobante de la transferencia.
     * @param observaciones Notas (opcional).
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
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
            return RespuestaFormularioUtil.armarRespuestaExitosa(inscripcion.estaHabilitada()
                    ? "Pago de " + alumno + " registrado con éxito. Se habilitó el acceso al curso y se emitió el comprobante."
                    : "Pago de " + alumno + " registrado, pero no cubre el precio del curso: la inscripción sigue pendiente de pago. "
                            + "Faltan $" + String.format(java.util.Locale.forLanguageTag("es-AR"), "%,.2f",
                                    pagoService.calcularMontoRequerido(inscripcion) - inscripcion.getMontoPagado()) + ".");
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    // ---------------------------------------------------------------- Modificaciones

    /**
     * CU-54: El administrador acredita o rechaza un pago pendiente o rechazado. Responde en JSON.
     *
     * @param id            Identificador del pago.
     * @param estado        Aprobado o Rechazado.
     * @param monto         El monto.
     * @param referencia    El código de referencia de MODO.
     * @param numeroComprobante El número de comprobante (de la transferencia o de MODO).
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

    /**
     * CU-52: El alumno reintenta un pago rechazado, reutilizando su registro. Responde en JSON.
     *
     * @param idPago Identificador del pago rechazado.
     * @param authentication   La autenticación actual.
     * @return Los datos de la nueva solicitud de pago o el mensaje de error.
     */
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

    // ---------------------------------------------------------------- Ventana de modificación a pedido

    /**
     * Ventana de modificación de un pago, que el listado pide al abrirla (en lugar de dibujar una por fila).
     *
     * @param id     El pago.
     * @param model El modelo de la vista.
     * @return El fragmento con la ventana.
     */
    @GetMapping("/modificar/{id}")
    public String modificar(@PathVariable("id") Integer id, Model model) {
        Pago p = pagoService.buscarPorId(id).filter(x -> !x.estaAprobado())
                .orElseThrow(() -> new IllegalArgumentException("Error! El pago no se encuentra disponible para modificar."));
        model.addAttribute("p", p);
        model.addAttribute("detalles", Map.of(p.getInscripcion().getIdInscripcion(), pagoService.calcularDetalle(p.getInscripcion())));
        return "pages/pago/modificar :: modal(p=${p})";
    }


    /** Columnas de filtro, orden y página de los listados (las aplica el servidor). */
    public static final class Filtros {

        private Filtros() {
        }

        // ---------------------------------------------------------------- Listado del administrador (motor común)

        /** Las columnas del listado: lo que se filtra y se ordena en el servidor. */
        public static final FiltroUtil<Pago> tabla = new FiltroUtil<>(List.of(
                new FiltroUtil.Columna<Pago>("alumno", FiltroUtil.Tipo.TEXTO,
                        p -> p.getInscripcion().getAlumno().getUsuario().getNombreCompleto() + " "
                                + p.getInscripcion().getAlumno().getUsuario().getDni() + " "
                                + p.getInscripcion().getAlumno().getUsuario().getCorreo()),
                new FiltroUtil.Columna<Pago>("curso", FiltroUtil.Tipo.LISTA, p -> p.getInscripcion().getCurso().getNombre()),
                new FiltroUtil.Columna<Pago>("monto", FiltroUtil.Tipo.NUMERO, p -> String.valueOf(p.getMonto())),
                new FiltroUtil.Columna<Pago>("medioDePago", FiltroUtil.Tipo.LISTA, p -> p.getMetodoPago().getNombre()),
                new FiltroUtil.Columna<Pago>("estado", FiltroUtil.Tipo.LISTA, p -> p.getEstadoPago().getNombre()),
                new FiltroUtil.Columna<Pago>("comprobante", FiltroUtil.Tipo.TEXTO,
                        p -> p.getComprobanteVigente() == null ? "" : p.getComprobanteVigente().getNumero()),
                new FiltroUtil.Columna<Pago>("fechaAprobacion", FiltroUtil.Tipo.FECHA,
                        p -> FiltroUtil.formatearFecha(p.getFechaAprobacion())),
                new FiltroUtil.Columna<Pago>("fechaCreacion", FiltroUtil.Tipo.FECHA,
                        p -> FiltroUtil.formatearFechaHora(p.getFechaCreacion()))),
                p -> false);

        /** Las columnas de "Mis pagos": lo que se filtra y se ordena en el servidor. */
        public static final FiltroUtil<Pago> tablaAlumno = new FiltroUtil<>(List.of(
                new FiltroUtil.Columna<Pago>("curso", FiltroUtil.Tipo.LISTA, p -> p.getInscripcion().getCurso().getNombre()),
                new FiltroUtil.Columna<Pago>("monto", FiltroUtil.Tipo.NUMERO, p -> String.valueOf(p.getMonto())),
                new FiltroUtil.Columna<Pago>("medioDePago", FiltroUtil.Tipo.LISTA, p -> p.getMetodoPago().getNombre()),
                new FiltroUtil.Columna<Pago>("estado", FiltroUtil.Tipo.LISTA, p -> p.getEstadoPago().getNombre()),
                new FiltroUtil.Columna<Pago>("fechaCreacion", FiltroUtil.Tipo.FECHA,
                        p -> FiltroUtil.formatearFechaHora(p.getFechaCreacion()))),
                p -> false);
    }
}
