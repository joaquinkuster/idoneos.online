package com.app.idoneos.service.interesalumno;

import java.util.List;

import com.app.idoneos.model.Alumno;
import com.app.idoneos.model.Categoria;

/**
 * Servicio de los intereses del alumno: las categorías de cursos que dijo que le interesan, que se usan para recomendarle cursos
 * en el catálogo.
 */
public interface InteresAlumnoService {

    /** Cuántas veces se le pregunta al alumno por sus intereses (con «Ahora no») antes de dejar de insistir. */
    int MAXIMO_DE_OMISIONES = 3;

    // ---------------------------------------------------------------- Lecturas

    /**
     * Busca las categorías de interés del alumno, ordenadas por nombre.
     *
     * @param alumno El alumno.
     * @return Las categorías que le interesan (vacía si no eligió ninguna).
     */
    List<Categoria> buscarCategorias(Alumno alumno);

    /**
     * Indica si hay que preguntarle al alumno qué le interesa: todavía no eligió ninguna categoría y no llegó al máximo de veces
     * que puede elegir «Ahora no».
     *
     * @param alumno El alumno.
     * @return {@code true} si hay que preguntarle.
     */
    boolean debePreguntar(Alumno alumno);

    /**
     * Busca las categorías que el alumno puede elegir: las vigentes, ordenadas por nombre.
     *
     * @return Las categorías vigentes.
     */
    List<Categoria> buscarCategoriasElegibles();

    // ---------------------------------------------------------------- Modificaciones

    /**
     * Guarda los intereses del alumno: reemplaza los que tenía por las categorías elegidas.
     *
     * @param idAlumno      El identificador del alumno.
     * @param idsCategorias Los identificadores de las categorías elegidas (al menos una).
     * @throws IllegalArgumentException si no se elige ninguna categoría o alguna no existe.
     */
    void guardar(int idAlumno, List<Integer> idsCategorias);

    /**
     * Registra que el alumno eligió «Ahora no» al preguntarle qué le interesa.
     *
     * @param idAlumno El identificador del alumno.
     */
    void omitir(int idAlumno);
}
