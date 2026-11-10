package com.app.idoneos.service.terminoglosario;

import com.app.idoneos.service.CrudService;

import com.app.idoneos.model.Docente;
import com.app.idoneos.model.Tema;
import com.app.idoneos.model.TerminoGlosario;
import com.app.idoneos.model.Usuario;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Servicio del término del glosario. */
public interface TerminoGlosarioService extends CrudService<TerminoGlosario> {

    /**
     * Busca un término vigente por su identificador.
     *
     * @param id el identificador
     * @return el registro, o vacío si no existe o está dado de baja
     */
    Optional<TerminoGlosario> buscarPorId(Integer id);

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /**
     * Busca los registros vigentes de terminoGlosario asociados a tema.
     *
     * @param tema el registro de Tema asociado
     * @return una lista de TerminoGlosario asociados al Tema indicado
     */
    List<TerminoGlosario> buscarPorTema(Tema tema);

    /**
     * Busca los términos vigentes del glosario de un tema que contienen un texto (sin distinguir mayúsculas ni tildes), en orden alfabético.
     *
     * @param tema el tema del glosario
     * @param texto el texto a buscar (opcional: si está vacío devuelve todos los términos)
     * @param enDefiniciones {@code true} para buscar también en las definiciones, {@code false} solo en el término
     * @return los términos que cumplen el criterio
     */
    List<TerminoGlosario> buscarConFiltros(Tema tema, String texto, boolean enDefiniciones);

    /**
     * Agrupa términos por su letra inicial, conservando el orden en que vienen (el alfabético).
     *
     * @param terminos los términos a agrupar
     * @return los términos de cada letra, con las letras en el orden en que aparecen
     */
    Map<String, List<TerminoGlosario>> agruparPorLetra(List<TerminoGlosario> terminos);

    /**
     * Indica si un usuario puede ver el glosario de un tema: el administrador.
     *
     * @param tema el tema del glosario
     * @param usuario el usuario que quiere verlo
     * @return {@code true} si puede verlo
     */
    boolean puedeVer(Tema tema, Usuario usuario);

    // ----------------------------------------------------------------------
    // Creaciones
    // ----------------------------------------------------------------------

    /**
     * Registra un término en el glosario de un tema.
     *
     * @param docente el docente que registra
     * @param temaId el identificador del tema
     * @param termino el término
     * @param definicion la definición del término
     * @return el término registrado
     * @throws IllegalArgumentException si falta algún dato, supera el máximo, ya existe o el docente no participa
     */
    TerminoGlosario registrar(Docente docente, Integer temaId, String termino, String definicion);

    // ----------------------------------------------------------------------
    // Modificaciones
    // ----------------------------------------------------------------------

    /**
     * Modifica el término y la definición de un término del glosario.
     *
     * @param id el identificador del término
     * @param docente el docente que modifica
     * @param termino el término
     * @param definicion la definición del término
     * @return el término modificado
     * @throws IllegalArgumentException si falta algún dato, supera el máximo, ya existe o el docente no participa
     */
    TerminoGlosario modificar(Integer id, Docente docente, String termino, String definicion);

    // ----------------------------------------------------------------------
    // Bajas
    // ----------------------------------------------------------------------

    /**
     * Da de baja un término del glosario (baja lógica).
     *
     * @param id el identificador del término
     * @param docente el docente que da de baja, o {@code null} si lo hace el administrador (que puede hacerlo en cualquier curso)
     * @throws IllegalArgumentException si el término no está activo o el docente no participa en el curso
     */
    void darDeBaja(Integer id, Docente docente);
}
