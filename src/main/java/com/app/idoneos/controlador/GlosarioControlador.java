package com.app.idoneos.controlador;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.app.idoneos.servicio.Curso.ContextoCurso;
import com.app.idoneos.modelo.Docente;
import com.app.idoneos.modelo.Programa;
import com.app.idoneos.modelo.TerminoGlosario;
import com.app.idoneos.modelo.Unidad;
import com.app.idoneos.modelo.UnidadCronograma;
import com.app.idoneos.modelo.Usuario;
import com.app.idoneos.servicio.TerminoGlosario.TerminoGlosarioServicio;
import com.app.idoneos.servicio.Unidad.UnidadServicioImpl;
import com.app.idoneos.utilidades.RespuestaJsonUtilidad;

/**
 * Controlador del glosario (MOD-F-02): CU-32 Buscar término (GET /glosario/buscar y GET /glosario/ver/{unidadId}),
 * CU-33 Registrar término (POST /glosario/registrar), CU-34 Modificar término (POST /glosario/modificar/{id}) y CU-35
 * Dar de baja término (POST /glosario/darDeBaja/{id}).
 *
 * Cada unidad tiene su glosario ("Glosario 1", "Glosario 2", … según el orden de la unidad en el programa). El docente
 * (titular o ayudante) registra, modifica, da de baja y busca los términos de los cursos en los que participa; el
 * administrador puede buscar y dar de baja los de cualquier curso; el alumno lo ve en lectura cuando tiene la unidad
 * habilitada.
 */
@Controller
@RequestMapping("/glosario")
public class GlosarioControlador extends PanelCursoControlador {

    @Autowired
    private TerminoGlosarioServicio terminoGlosarioServicio;

    @Autowired
    private UnidadServicioImpl unidadServicio;

    // ---------------------------------------------------------------- Lecturas

    /**
     * Lista los glosarios de un curso, uno por unidad del programa, cada uno con su cantidad de términos y su última
     * actualización. Es el listado del ABM: desde cada fila se entra a la vista del glosario.
     *
     * @param cursoId            Identificador del curso.
     * @param unidadId           Identificador de la unidad (opcional).
     * @param modelo             El modelo de la vista.
     * @param auth               La autenticación actual.
     * @param redirectAttributes Atributos para mensajes de redirección.
     * @return La vista del listado, o una redirección a "Mis cursos" si no se puede acceder.
     */
    @GetMapping("/buscar")
    public String buscarGlosarios(@RequestParam(value = "cursoId", required = false) Integer cursoId,
            @RequestParam(value = "unidadId", required = false) Integer unidadId, Model modelo,
            Authentication auth, RedirectAttributes redirectAttributes) {
        try {
            ContextoCurso contexto = contextoDelPanel(cursoId, (Usuario) auth.getPrincipal());
            Programa programa = contexto.programa();
            List<UnidadCronograma> todas = programa == null ? List.of() : programa.getCronogramaOrdenado();

            cargarContexto(modelo, contexto, "glosario");
            modelo.addAttribute("unidadesDelPrograma", todas);
            modelo.addAttribute("unidadSeleccionada", unidadId);
            modelo.addAttribute("unidades", todas); // todas las unidades: el filtro se aplica en la pantalla
            modelo.addAttribute("titulo", "Glosarios | Idóneos Online");
            return "pages/glosario/buscar";
        } catch (IllegalArgumentException e) {
            return volverAMisCursos(redirectAttributes, e);
        }
    }

    /**
     * CU-32: Muestra el glosario de una unidad: sus términos agrupados por letra inicial, con buscador, índice
     * alfabético y paginación. Si se indica {@code nuevo}, el docente ve abierto el formulario para registrar un
     * término (es el destino de la opción Glosario del selector "Agregar recurso o actividad").
     *
     * @param unidadId       Identificador de la unidad.
     * @param texto          Texto a buscar (opcional).
     * @param buscar         Presente cuando se envió el buscador (distingue "casilla destildada" de "sin buscar").
     * @param enDefiniciones Si se busca también en las definiciones (por defecto sí; destildada, solo en el término).
     * @param letra          Letra del índice: de la A a la Z, "#" para los especiales; vacío = todas (opcional).
     * @param pagina         Número de página, desde 1; 0 muestra todos los términos en una sola página.
     * @param terminoId      Identificador de un término: muestra solo ese término (es su enlace permanente).
     * @param nuevo          Presente para abrir el formulario de registro (solo docente).
     * @param modelo         El modelo de la vista.
     * @param auth           La autenticación actual.
     * @return La vista del glosario.
     */
    @GetMapping("/ver/{unidadId}")
    public String verGlosario(@PathVariable("unidadId") Integer unidadId,
            @RequestParam(value = "texto", required = false) String texto,
            @RequestParam(value = "buscar", required = false) String buscar,
            @RequestParam(value = "enDefiniciones", required = false) Boolean enDefiniciones,
            @RequestParam(value = "letra", required = false) String letra,
            @RequestParam(value = "pagina", defaultValue = "1") int pagina,
            @RequestParam(value = "terminoId", required = false) Integer terminoId,
            @RequestParam(value = "nuevo", required = false) String nuevo, Model modelo, Authentication auth) {
        Unidad unidad = unidadServicio.buscarPorId(unidadId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Usuario usuario = (Usuario) auth.getPrincipal();
        if (!terminoGlosarioServicio.puedeVer(unidad, usuario)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        ContextoCurso contexto = contextoDeLaUnidad(unidad, usuario);
        boolean esAdministrador = contexto.esAdministrador();
        boolean esDocente = contexto.esDocente();
        Programa programa = contexto.programa();
        int numeroOrden = programa == null ? 0 : programa.getCronogramaOrdenado().stream()
                .filter(cronograma -> cronograma.getUnidad().getIdUnidad() == unidad.getIdUnidad())
                .mapToInt(UnidadCronograma::getNumeroOrden).findFirst().orElse(0);

        // Los filtros (texto, letra y páginas) se aplican en el navegador (glosarioFiltros.js): acá se entregan todos
        // los términos de la unidad, salvo el enlace permanente a un término
        List<TerminoGlosario> encontrados = terminoGlosarioServicio.buscarConFiltros(unidad, null, true);
        boolean unSoloTermino = terminoId != null;
        if (unSoloTermino) {
            encontrados = encontrados.stream().filter(termino -> termino.getIdTermino() == terminoId).toList();
        }

        cargarContexto(modelo, contexto, "glosario");
        modelo.addAttribute("unidad", unidad);
        modelo.addAttribute("numeroOrden", numeroOrden);
        modelo.addAttribute("totalTerminos", unidad.getTerminosGlosarioOrdenados().size());
        modelo.addAttribute("unSoloTermino", unSoloTermino);
        modelo.addAttribute("grupos", terminoGlosarioServicio.agruparPorLetra(encontrados)); // por letra inicial
        modelo.addAttribute("puedeGestionar", esDocente);
        modelo.addAttribute("puedeDarDeBaja", esDocente || esAdministrador);
        modelo.addAttribute("abrirNuevo", esDocente && nuevo != null);
        modelo.addAttribute("titulo", "Glosario " + numeroOrden + " | Idóneos Online");
        return "pages/glosario/ver";
    }

    // ---------------------------------------------------------------- Creaciones

    /**
     * CU-33: Registra un término en el glosario de una unidad. Responde en JSON para que el formulario muestre el
     * resultado sin recargar la página.
     *
     * @param unidadId   Identificador de la unidad.
     * @param termino    El término.
     * @param definicion La definición del término.
     * @param auth       La autenticación actual.
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/registrar")
    public ResponseEntity<Map<String, String>> registrarTermino(
            @RequestParam(value = "unidadId", required = false) Integer unidadId,
            @RequestParam(value = "termino", required = false) String termino,
            @RequestParam(value = "definicion", required = false) String definicion, Authentication auth) {
        try {
            Usuario usuario = (Usuario) auth.getPrincipal();
            TerminoGlosario registrado = terminoGlosarioServicio.registrarTermino(usuario.getDocente(), unidadId,
                    termino, definicion);
            return RespuestaJsonUtilidad.respuestaExitosa("Término '" + registrado.getTermino() + "' registrado con éxito.");
        } catch (Exception e) {
            return RespuestaJsonUtilidad.respuestaConError(e);
        }
    }

    // ---------------------------------------------------------------- Modificaciones

    /**
     * CU-34: Modifica un término del glosario. Responde en JSON.
     *
     * @param id         Identificador del término.
     * @param termino    El término.
     * @param definicion La definición del término.
     * @param auth       La autenticación actual.
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/modificar/{id}")
    public ResponseEntity<Map<String, String>> modificarTermino(@PathVariable("id") Integer id,
            @RequestParam(value = "termino", required = false) String termino,
            @RequestParam(value = "definicion", required = false) String definicion, Authentication auth) {
        try {
            Usuario usuario = (Usuario) auth.getPrincipal();
            terminoGlosarioServicio.modificarTermino(id, usuario.getDocente(), termino, definicion);
            return RespuestaJsonUtilidad.respuestaExitosa("Término modificado correctamente.");
        } catch (Exception e) {
            return RespuestaJsonUtilidad.respuestaConError(e);
        }
    }

    // ---------------------------------------------------------------- Bajas

    /**
     * CU-35: Da de baja un término del glosario. Responde en JSON.
     *
     * @param id   Identificador del término.
     * @param auth La autenticación actual.
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/darDeBaja/{id}")
    public ResponseEntity<Map<String, String>> darDeBajaTermino(@PathVariable("id") Integer id, Authentication auth) {
        try {
            Usuario usuario = (Usuario) auth.getPrincipal();
            // El administrador puede dar de baja los términos de cualquier curso
            Docente docente = usuario.esAdministradorActivo() ? null : usuario.getDocente();
            terminoGlosarioServicio.darDeBajaTermino(id, docente);
            return RespuestaJsonUtilidad.respuestaExitosa("Término dado de baja con éxito.");
        } catch (Exception e) {
            return RespuestaJsonUtilidad.respuestaConError(e);
        }
    }
}
