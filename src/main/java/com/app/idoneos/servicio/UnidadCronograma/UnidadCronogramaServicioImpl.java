package com.app.idoneos.servicio.UnidadCronograma;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Programa;
import com.app.idoneos.modelo.Unidad;
import com.app.idoneos.modelo.UnidadCronograma;
import com.app.idoneos.repositorio.UnidadCronogramaRepositorio;
import com.app.idoneos.servicio.CrudServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link UnidadCronograma}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link UnidadCronogramaServicio}.
 */
@Service
public class UnidadCronogramaServicioImpl implements UnidadCronogramaServicio, CrudServicio<UnidadCronograma> {

    @Autowired
    private UnidadCronogramaRepositorio unidadCronogramaRepositorio;

    /**
     * Guarda la unidad del cronograma en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public UnidadCronograma guardar(UnidadCronograma entidad) {
        return unidadCronogramaRepositorio.save(entidad);
    }

    /**
     * Busca la unidad del cronograma por su identificador. Solo devuelve registros activos (no dados de baja).
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<UnidadCronograma> buscarPorId(Integer id) {
        return unidadCronogramaRepositorio.findById(id)
                .filter(entidad -> !entidad.esInactivo()); // Solo devuelve registros activos
    }

    /**
     * Obtiene todos los registros. Solo devuelve registros activos.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<UnidadCronograma> obtenerTodo() {
        return unidadCronogramaRepositorio.findByBajaFalse();
    }

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public UnidadCronograma modificar(UnidadCronograma entidad) {
        return unidadCronogramaRepositorio.save(entidad);
    }

    /**
     * Da de baja el registro (baja lógica), en lugar de eliminarlo de la base de datos.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(UnidadCronograma entidad) {
        entidad.marcarInactivo(); // Baja lógica
        unidadCronogramaRepositorio.save(entidad);
    }

    /**
     * Verifica si existe un registro con el identificador dado y está activo.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return unidadCronogramaRepositorio.existsById(id) &&
                buscarPorId(id).isPresent(); // Solo cuenta registros activos
    }

    /**
     * Busca los registros vigentes de unidadCronograma asociados a programa.
     *
     * @param programa el registro de Programa asociado
     * @return una lista de UnidadCronograma asociados al Programa indicado
     */
    @Override
    public List<UnidadCronograma> buscarPorPrograma(Programa programa) {
        return unidadCronogramaRepositorio.findByProgramaAndBajaFalse(programa);
    }

    /**
     * Busca los registros vigentes de unidadCronograma asociados a unidad.
     *
     * @param unidad el registro de Unidad asociado
     * @return una lista de UnidadCronograma asociados al Unidad indicado
     */
    @Override
    public List<UnidadCronograma> buscarPorUnidad(Unidad unidad) {
        return unidadCronogramaRepositorio.findByUnidadAndBajaFalse(unidad);
    }

    /**
     * Busca la unidad del cronograma a partir de la combinación única de sus relaciones. Incluye los registros dados de baja.
     *
     * @param programa el registro de Programa asociado
     * @param unidad el registro de Unidad asociado
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    @Override
    public Optional<UnidadCronograma> buscarPorProgramaYUnidad(Programa programa, Unidad unidad) {
        return unidadCronogramaRepositorio.findByProgramaAndUnidad(programa, unidad);
    }
}
