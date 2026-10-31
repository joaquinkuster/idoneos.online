package com.app.idoneos.controlador;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.app.idoneos.modelo.Curso;
import com.app.idoneos.modelo.Parametro;
import com.app.idoneos.modelo.Tema;
import com.app.idoneos.modelo.Usuario;
import com.app.idoneos.servicio.Curso.ContextoCurso;
import com.app.idoneos.servicio.Curso.CursoServicio;
import com.app.idoneos.servicio.Inscripcion.InscripcionServicio;
import com.app.idoneos.servicio.Parametro.ParametroServicio;

/**
 * Base de los controladores de las pantallas que viven dentro del panel de un curso (temas, programas, cronograma,
 * materiales, glosario, foros, participantes, cohortes y la página del curso). Resuelve con qué rol entra la persona,
 * con qué curso y programa trabaja y arma los datos de pantalla que todas comparten ({@code urlBase},
 * {@code urlMisCursos}, {@code esDocente}, {@code esAdministrador} y {@code menuActivo}). Las reglas de acceso las
 * resuelven los servicios; acá solo se arma lo que necesita la vista.
 */
public abstract class PanelCursoControlador {

    @Autowired
    protected CursoServicio cursoServicio;

    @Autowired
    protected InscripcionServicio inscripcionServicio;

    @Autowired
    protected ParametroServicio parametroServicio;

    /**
     * Resuelve el contexto de las pantallas que se abren desde el panel con {@code cursoId}: el administrador trabaja con
     * el programa vigente del curso y el docente, con el que tiene elegido.
     *
     * @param cursoId El identificador del curso (obligatorio).
     * @param usuario El usuario autenticado.
     * @return El contexto de la pantalla.
     * @throws IllegalArgumentException Si no se indicó el curso, no está activo o el docente no participa.
     */
    protected ContextoCurso contextoDelPanel(Integer cursoId, Usuario usuario) {
        return cursoServicio.resolverContextoDelPanel(cursoId, usuario);
    }

    /**
     * Resuelve el contexto de las pantallas que se abren desde un tema (foro, glosario, material): el administrador
     * y los docentes del curso entran como tales; el alumno, por su inscripción con acceso al tema. Quien las
     * llame debe haber comprobado antes que la persona puede ver el contenido.
     *
     * @param tema  El tema de la pantalla.
     * @param usuario El usuario autenticado.
     * @return El contexto de la pantalla.
     * @throws ResponseStatusException 403 si no es de la gestión del curso ni tiene una inscripción con acceso.
     */
    protected ContextoCurso contextoDeLaTema(Tema tema, Usuario usuario) {
        return cursoServicio.resolverContextoDeLaTema(tema, usuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));
    }

    /**
     * Vuelca al modelo los datos que comparten todas las pantallas del panel de un curso.
     *
     * @param modelo     El modelo de la vista.
     * @param contexto   El contexto de la pantalla.
     * @param menuActivo La opción del menú lateral que se marca como activa.
     */
    protected void cargarContexto(Model modelo, ContextoCurso contexto, String menuActivo) {
        modelo.addAttribute("curso", contexto.curso());
        modelo.addAttribute("programa", contexto.programa());
        modelo.addAttribute("esAdministrador", contexto.esAdministrador());
        modelo.addAttribute("esDocente", contexto.esDocente());
        modelo.addAttribute("urlBase", contexto.urlBase());
        modelo.addAttribute("urlMisCursos", contexto.urlMisCursos());
        modelo.addAttribute("menuActivo", menuActivo);
    }

    /**
     * Obtiene los días que ocupa un tema nuevo en el cronograma cuando no se indica otra duración (parámetro del sistema).
     *
     * @return Los días de un tema nuevo.
     */
    protected int diasPorTemaNuevo() {
        return Math.max(1, parametroServicio.obtenerEntero(Parametro.CRONOGRAMA_DIAS_POR_TEMA_NUEVO,
                Parametro.CRONOGRAMA_DIAS_POR_TEMA_NUEVO_POR_DEFECTO));
    }

    /**
     * Vuelca al modelo el máximo de días que puede sumar un cronograma del curso: la duración máxima del curso, sin
     * superar el máximo que el sistema admite (parámetro del sistema).
     *
     * @param modelo El modelo de la vista.
     * @param curso  El curso del cronograma.
     */
    protected void cargarDiasMaximosDelCronograma(Model modelo, Curso curso) {
        modelo.addAttribute("diasNuevoTema", diasPorTemaNuevo());
        modelo.addAttribute("diasMaximos", Math.min(curso.getDuracionMaximaDias(),
                parametroServicio.obtenerEntero(Parametro.CRONOGRAMA_DIAS_MAXIMOS, Parametro.CRONOGRAMA_DIAS_MAXIMOS_POR_DEFECTO)));
    }

    /**
     * Vuelve al listado de cursos mostrando el motivo por el que no se pudo abrir la pantalla.
     *
     * @param redirectAttributes Atributos para mensajes de redirección.
     * @param error              El error que impidió abrir la pantalla.
     * @return La redirección al listado de cursos.
     */
    protected String volverAMisCursos(RedirectAttributes redirectAttributes, IllegalArgumentException error) {
        redirectAttributes.addFlashAttribute("error", error.getMessage());
        return "redirect:/curso/buscar";
    }

    /**
     * Un curso sin programas no tiene temas ni cronograma: el docente va a registrar el primero y el administrador
     * vuelve al listado de cursos.
     *
     * @param contexto           El contexto de la pantalla.
     * @param redirectAttributes Atributos para mensajes de redirección.
     * @return La redirección que corresponde al rol.
     */
    protected String avisarCursoSinProgramas(ContextoCurso contexto, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error",
                "Error! El curso todavía no tiene programas: registrá uno para poder cargar temas.");
        return contexto.esAdministrador() ? "redirect:/curso/buscar"
                : "redirect:/programa/buscar?cursoId=" + contexto.curso().getIdCurso() + "&nuevo";
    }
}
