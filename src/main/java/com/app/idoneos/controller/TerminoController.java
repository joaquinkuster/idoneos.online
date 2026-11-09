package com.app.idoneos.controller;

import com.app.idoneos.controller.component.PanelCursoComponent;
import com.app.idoneos.util.FiltroUtil;

import com.app.idoneos.util.PaginacionUtil;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.app.idoneos.service.curso.CursoService.ContextoCurso;
import com.app.idoneos.model.Docente;
import com.app.idoneos.model.Programa;
import com.app.idoneos.model.TerminoGlosario;
import com.app.idoneos.model.Tema;
import com.app.idoneos.model.TemaCronograma;
import com.app.idoneos.model.Usuario;
import com.app.idoneos.service.terminoglosario.TerminoGlosarioService;
import com.app.idoneos.service.tema.TemaService;
import com.app.idoneos.util.RespuestaFormularioUtil;

/**
 * Controlador del glosario (MOD-F-02): CU-32 Buscar término (GET /glosario/buscar y GET /glosario/buscarPorTema/{temaId}),
 * CU-33 Registrar término (POST /termino/registrar), CU-34 Modificar término (POST /termino/modificar/{id}) y CU-35
 * Dar de baja término (POST /termino/darDeBaja/{id}).
 *
 * Cada tema tiene su glosario ("Glosario 1", "Glosario 2", … según el orden del tema en el programa). El docente
 * (titular o ayudante) registra, modifica, da de baja y busca los términos de los cursos en los que participa; el
 * administrador puede buscar y dar de baja los de cualquier curso; el alumno lo ve en lectura cuando tiene el tema
 * habilitada.
 */
@Controller
public class TerminoController {

    @Autowired
    private PanelCursoComponent panel;

    @Autowired
    private TerminoGlosarioService terminoGlosarioService;

    @Autowired
    private TemaService temaService;

    // ---------------------------------------------------------------- Lecturas

    /**
     * Lista los glosarios de un curso, uno por tema del programa, cada uno con su cantidad de términos y su última
     * actualización. Es el listado del ABM: desde cada fila se entra a la vista del glosario.
     *
     * @param cursoId            Identificador del curso.
     * @param temaId           Identificador del tema (opcional).
     * @param model             El modelo de la vista.
     * @param authentication               La autenticación actual.
     * @param redirectAttributes Atributos para mensajes de redirección.
     * @return La vista del listado, o una redirección a "Mis cursos" si no se puede acceder.
     */
    @GetMapping("/glosario/buscar")
    public String buscar(@RequestParam(value = "cursoId", required = false) Integer cursoId,
            @RequestParam(value = "temaId", required = false) Integer temaId, Model model,
            Authentication authentication, RedirectAttributes redirectAttributes, jakarta.servlet.http.HttpServletRequest request) {
        try {
            ContextoCurso contexto = panel.contextoDelPanel(cursoId, (Usuario) authentication.getPrincipal());
            Programa programa = contexto.programa();
            List<TemaCronograma> todas = panel.temasDelCursoEnOrden(contexto);

            panel.cargarContexto(model, contexto, "glosario");
            model.addAttribute("temasDelPrograma", todas);
            model.addAttribute("temaSeleccionada", temaId);
            // El filtro, el orden por columna y la página los resuelve el servidor (motor común de listados)
            PaginacionUtil.Pagina<TemaCronograma> pagina = Filtros.tabla.paginar(todas, request.getParameterMap());
            PaginacionUtil.cargarEnModelo(model, pagina);
            model.addAttribute("temas", pagina.filas());
            if (PaginacionUtil.pideSoloFilas(request)) {
                return "pages/termino/buscar :: filas"; // solo las filas de la página pedida
            }
            model.addAttribute("titulo", "Glosarios | Idóneos Online");
            return "pages/termino/buscar";
        } catch (IllegalArgumentException e) {
            return panel.volverAMisCursos(redirectAttributes, e);
        }
    }

    /**
     * CU-32: Muestra el glosario de un tema: sus términos agrupados por letra inicial, con buscador, índice
     * alfabético y paginación. Si se indica {@code nuevo}, el docente ve abierto el formulario para registrar un
     * término (es el destino de la opción Glosario del selector "Agregar recurso o actividad").
     *
     * @param temaId       Identificador del tema.
     * @param texto          Texto a buscar (opcional).
     * @param buscar         Presente cuando se envió el buscador (distingue "casilla destildada" de "sin buscar").
     * @param enDefiniciones Si se busca también en las definiciones (por defecto sí; destildada, solo en el término).
     * @param letra          Letra del índice: de la A a la Z, "#" para los especiales; vacío = todas (opcional).
     * @param pagina         Número de página, desde 1; 0 muestra todos los términos en una sola página.
     * @param terminoId      Identificador de un término: muestra solo ese término (es su enlace permanente).
     * @param nuevo          Presente para abrir el formulario de registro (solo docente).
     * @param model         El modelo de la vista.
     * @param authentication           La autenticación actual.
     * @return La vista del glosario.
     */
    @GetMapping("/glosario/buscarPorTema/{temaId}")
    public String ver(@PathVariable("temaId") Integer temaId,
            @RequestParam(value = "texto", required = false) String texto,
            @RequestParam(value = "buscar", required = false) String buscar,
            @RequestParam(value = "enDefiniciones", required = false) Boolean enDefiniciones,
            @RequestParam(value = "letra", required = false) String letra,
            @RequestParam(value = "pagina", defaultValue = "1") int pagina,
            @RequestParam(value = "terminoId", required = false) Integer terminoId,
            @RequestParam(value = "nuevo", required = false) String nuevo, Model model, Authentication authentication,
            jakarta.servlet.http.HttpServletRequest request) {
        Tema tema = temaService.buscarPorId(temaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Usuario usuario = (Usuario) authentication.getPrincipal();
        if (!terminoGlosarioService.puedeVer(tema, usuario)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        ContextoCurso contexto = panel.contextoDeLaTema(tema, usuario);
        boolean esAdministrador = contexto.esAdministrador();
        boolean esDocente = contexto.esDocente();
        Programa programa = contexto.programa();
        int numeroOrden = programa == null ? 0 : programa.getCronogramaOrdenado().stream()
                .filter(cronograma -> cronograma.getTema().getIdTema() == tema.getIdTema())
                .mapToInt(TemaCronograma::getNumeroOrden).findFirst().orElse(0);

        // El texto, la letra y la página los resuelve el servidor (pages/termino/filtros.js los pide por fetch)
        List<TerminoGlosario> encontrados = terminoGlosarioService.buscarConFiltros(tema, null, true);
        boolean unSoloTermino = terminoId != null;
        if (unSoloTermino) {
            encontrados = encontrados.stream().filter(termino -> termino.getIdTermino() == terminoId).toList();
        }
        String buscado = texto == null ? "" : texto.trim();
        if (!buscado.isEmpty()) {
            // Sin tildes ni mayúsculas; por defecto se busca también en las definiciones
            String clave = sinTildes(buscado);
            boolean enDefinicionesTambien = buscar == null || Boolean.TRUE.equals(enDefiniciones);
            encontrados = encontrados.stream().filter(t -> sinTildes(t.getTermino()).contains(clave)
                    || (enDefinicionesTambien && sinTildes(t.getDefinicion()).contains(clave))).toList();
        }
        Map<String, List<TerminoGlosario>> porLetra = terminoGlosarioService.agruparPorLetra(encontrados);
        String letraActiva = letra == null || !porLetra.containsKey(letra) ? "" : letra;
        List<TerminoGlosario> deLaLetra = letraActiva.isEmpty() ? encontrados : porLetra.get(letraActiva);
        int totalPaginas = Math.max(1, (int) Math.ceil(deLaLetra.size() / (double) TERMINOS_POR_PAGINA));
        int paginaActual = pagina == 0 ? 0 : Math.min(Math.max(pagina, 1), totalPaginas);
        List<TerminoGlosario> visibles = paginaActual == 0 ? deLaLetra : deLaLetra.subList(
                (paginaActual - 1) * TERMINOS_POR_PAGINA, Math.min(paginaActual * TERMINOS_POR_PAGINA, deLaLetra.size()));

        panel.cargarContexto(model, contexto, "glosario");
        model.addAttribute("tema", tema);
        model.addAttribute("numeroOrden", numeroOrden);
        model.addAttribute("totalTerminos", tema.getTerminosGlosarioOrdenados().size());
        model.addAttribute("unSoloTermino", unSoloTermino);
        model.addAttribute("grupos", terminoGlosarioService.agruparPorLetra(visibles)); // por letra inicial
        model.addAttribute("indiceLetras", INDICE_DE_LETRAS);
        model.addAttribute("letrasConTerminos", porLetra.keySet());
        model.addAttribute("letraActiva", letraActiva);
        model.addAttribute("paginaActual", paginaActual);
        model.addAttribute("totalPaginas", totalPaginas);
        model.addAttribute("textoBuscado", buscado);
        model.addAttribute("cantidadEncontrados", encontrados.size());
        model.addAttribute("puedeGestionar", esDocente);
        model.addAttribute("puedeDarDeBaja", esDocente || esAdministrador);
        model.addAttribute("abrirNuevo", esDocente && nuevo != null);
        if (PaginacionUtil.pideSoloFilas(request)) {
            return "pages/termino/ver :: contenido"; // solo los términos, el índice y las páginas
        }
        model.addAttribute("titulo", "Glosario " + numeroOrden + " | Idóneos Online");
        return "pages/termino/ver";
    }

    // ---------------------------------------------------------------- Creaciones

    /**
     * CU-33: Registra un término en el glosario de un tema. Responde en JSON para que el formulario muestre el
     * resultado sin recargar la página.
     *
     * @param temaId   Identificador del tema.
     * @param termino    El término.
     * @param definicion La definición del término.
     * @param authentication       La autenticación actual.
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/termino/registrar")
    public ResponseEntity<Map<String, String>> registrar(
            @RequestParam(value = "temaId", required = false) Integer temaId,
            @RequestParam(value = "termino", required = false) String termino,
            @RequestParam(value = "definicion", required = false) String definicion, Authentication authentication) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();
            TerminoGlosario registrado = terminoGlosarioService.registrar(usuario.getDocente(), temaId,
                    termino, definicion);
            return RespuestaFormularioUtil.armarRespuestaExitosa("Término '" + registrado.getTermino() + "' registrado con éxito.");
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    // ---------------------------------------------------------------- Modificaciones

    /**
     * CU-34: Modifica un término del glosario. Responde en JSON.
     *
     * @param id         Identificador del término.
     * @param termino    El término.
     * @param definicion La definición del término.
     * @param authentication       La autenticación actual.
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/termino/modificar/{id}")
    public ResponseEntity<Map<String, String>> modificar(@PathVariable("id") Integer id,
            @RequestParam(value = "termino", required = false) String termino,
            @RequestParam(value = "definicion", required = false) String definicion, Authentication authentication) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();
            terminoGlosarioService.modificar(id, usuario.getDocente(), termino, definicion);
            return RespuestaFormularioUtil.armarRespuestaExitosa("Término modificado correctamente.");
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    // ---------------------------------------------------------------- Bajas

    /**
     * CU-35: Da de baja un término del glosario. Responde en JSON.
     *
     * @param id   Identificador del término.
     * @param authentication La autenticación actual.
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/termino/darDeBaja/{id}")
    public ResponseEntity<Map<String, String>> darDeBaja(@PathVariable("id") Integer id, Authentication authentication) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();
            // El administrador puede dar de baja los términos de cualquier curso
            Docente docente = usuario.esAdministradorActivo() ? null : usuario.getDocente();
            terminoGlosarioService.darDeBaja(id, docente);
            return RespuestaFormularioUtil.armarRespuestaExitosa("Término dado de baja con éxito.");
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    // ---------------------------------------------------------------- Ventana de modificación a pedido

    /**
     * Ventana de modificación de un término del glosario, que el listado pide al abrirla (en lugar de dibujar una por fila).
     *
     * @param id     El término.
     * @param model El modelo de la vista.
     * @param authentication   La autenticación actual.
     * @return El fragmento con la ventana.
     */
    @GetMapping("/termino/modificar/{id}")
    public String modificar(@PathVariable("id") Integer id, Model model, Authentication authentication) {
        TerminoGlosario t = terminoGlosarioService.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Error! El término no se encuentra disponible."));
        ContextoCurso contexto = panel.contextoDelPanel(t.getTema().getCurso().getIdCurso(), (Usuario) authentication.getPrincipal());
        if (contexto.esAdministrador()) {
            throw new IllegalArgumentException("Error! Solo el equipo docente modifica el glosario.");
        }
        model.addAttribute("t", t);
        return "pages/termino/modificar :: modal(t=${t})";
    }

    // ---------------------------------------------------------------- Búsqueda del glosario (en el servidor)

    /** Términos por página. */
    private static final int TERMINOS_POR_PAGINA = 10;

    /** Las letras del índice: los especiales y de la A a la Z. */
    private static final List<String> INDICE_DE_LETRAS = java.util.stream.Stream.concat(java.util.stream.Stream.of("#"),
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ".chars().mapToObj(c -> String.valueOf((char) c))).toList();

    private static String sinTildes(String texto) {
        return java.text.Normalizer.normalize(texto == null ? "" : texto, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase(java.util.Locale.ROOT).trim();
    }

    /** Columnas de filtro, orden y página de los listados (las aplica el servidor). */
    public static final class Filtros {

        private Filtros() {
        }

        // ---------------------------------------------------------------- Listado de glosarios (motor común)

        /** Las columnas del listado: lo que se filtra y se ordena en el servidor. */
        public static final FiltroUtil<TemaCronograma> tabla = new FiltroUtil<>(List.of(
                new FiltroUtil.Columna<TemaCronograma>("nombre", FiltroUtil.Tipo.TEXTO, u -> "Glosario " + u.getNumeroOrden()),
                new FiltroUtil.Columna<TemaCronograma>("tema", FiltroUtil.Tipo.LISTA, u -> u.getTema().getTitulo()),
                new FiltroUtil.Columna<TemaCronograma>("terminos", FiltroUtil.Tipo.NUMERO,
                        u -> String.valueOf(u.getTema().getTerminosGlosarioOrdenados().size())),
                new FiltroUtil.Columna<TemaCronograma>("ultimaActualizacion", FiltroUtil.Tipo.FECHA,
                        u -> FiltroUtil.formatearFechaHora(u.getTema().getUltimaActualizacionGlosario()))),
                u -> false);
    }
}
