package com.app.idoneos.servicio.ConsultaForo;

import java.time.LocalDate;
import java.util.List;
import com.app.idoneos.modelo.Alumno;
import com.app.idoneos.modelo.ConsultaForo;
import com.app.idoneos.modelo.Inscripcion;
import com.app.idoneos.modelo.Tema;

/**
 * Servicio para gestionar las operaciones relacionadas con la consulta del foro.
 */
public interface ConsultaForoServicio {

    // ---------------------------------------------------------------- Lecturas

    /**
     * Busca los registros vigentes de consultaForo asociados a tema.
     *
     * @param tema el registro de Tema asociado
     * @return una lista de ConsultaForo asociados al Tema indicado
     */
    List<ConsultaForo> buscarPorTema(Tema tema);

    /**
     * Busca los registros vigentes de consultaForo asociados a inscripcion.
     *
     * @param inscripcion el registro de Inscripcion asociado
     * @return una lista de ConsultaForo asociados al Inscripcion indicado
     */
    List<ConsultaForo> buscarPorInscripcion(Inscripcion inscripcion);

    /**
     * CU-35: Busca las consultas vigentes del foro de un tema que contienen un texto y fueron registradas dentro de
     * un rango de fechas, con el último mensaje más reciente primero.
     *
     * @param tema el tema del foro
     * @param texto  el texto a buscar, sin distinguir mayúsculas ni tildes (opcional)
     * @param desde  la fecha de registro mínima, inclusive (opcional)
     * @param hasta  la fecha de registro máxima, inclusive (opcional)
     * @return las consultas que cumplen los criterios
     */
    List<ConsultaForo> buscarConFiltros(Tema tema, String texto, LocalDate desde, LocalDate hasta);

    // ---------------------------------------------------------------- Creaciones

    /**
     * CU-36: Registra una consulta en el foro de un tema. Solo la hace un alumno con inscripción vigente al curso y
     * el tema habilitado.
     *
     * @param alumno   el alumno que consulta
     * @param temaId el identificador del tema
     * @param texto    el texto de la consulta
     * @return la consulta registrada
     * @throws IllegalArgumentException si falta el texto, supera el máximo o el alumno no tiene acceso al tema
     */
    ConsultaForo registrarConsulta(Alumno alumno, Integer temaId, String texto);

    // ---------------------------------------------------------------- Modificaciones

    /**
     * CU-37: Modifica el texto de una consulta propia, dentro del plazo de edición.
     *
     * @param id     el identificador de la consulta
     * @param alumno el alumno que modifica
     * @param texto  el texto nuevo
     * @return la consulta modificada
     * @throws IllegalArgumentException si no es suya, no está activa, pasó el plazo o el texto no es válido
     */
    ConsultaForo modificarConsulta(Integer id, Alumno alumno, String texto);

    // ---------------------------------------------------------------- Bajas

    /**
     * CU-38: Da de baja una consulta, junto con sus respuestas (baja lógica). Lo hace el administrador.
     *
     * @param id el identificador de la consulta
     * @throws IllegalArgumentException si la consulta no está activa
     */
    void darDeBajaConsulta(Integer id);
}
