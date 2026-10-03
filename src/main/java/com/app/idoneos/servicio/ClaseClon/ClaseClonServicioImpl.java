package com.app.idoneos.servicio.ClaseClon;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.ClaseClon;
import com.app.idoneos.modelo.EstadoClaseClon;
import com.app.idoneos.modelo.Material;
import com.app.idoneos.modelo.ParticipacionDocente;
import com.app.idoneos.repositorio.ClaseClonRepositorio;
import com.app.idoneos.servicio.CrudServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link ClaseClon}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link ClaseClonServicio}.
 */
@Service
public class ClaseClonServicioImpl implements ClaseClonServicio, CrudServicio<ClaseClon> {

    @Autowired
    private ClaseClonRepositorio claseClonRepositorio;

    /**
     * Guarda la clase con clon en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public ClaseClon guardar(ClaseClon entidad) {
        return claseClonRepositorio.save(entidad);
    }

    /**
     * Busca la clase con clon por su identificador. Solo devuelve registros activos (no dados de baja).
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<ClaseClon> buscarPorId(Integer id) {
        return claseClonRepositorio.findById(id)
                .filter(entidad -> !entidad.esInactivo()); // Solo devuelve registros activos
    }

    /**
     * Obtiene todos los registros. Solo devuelve registros activos.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<ClaseClon> obtenerTodo() {
        return claseClonRepositorio.findByBajaFalse();
    }

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public ClaseClon modificar(ClaseClon entidad) {
        return claseClonRepositorio.save(entidad);
    }

    /**
     * Da de baja el registro (baja lógica), en lugar de eliminarlo de la base de datos.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(ClaseClon entidad) {
        entidad.marcarInactivo(); // Baja lógica
        claseClonRepositorio.save(entidad);
    }

    /**
     * Verifica si existe un registro con el identificador dado y está activo.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return claseClonRepositorio.existsById(id) &&
                buscarPorId(id).isPresent(); // Solo cuenta registros activos
    }

    /**
     * Busca los registros vigentes de claseClon asociados a participacionDocente.
     *
     * @param participacionDocente el registro de ParticipacionDocente asociado
     * @return una lista de ClaseClon asociados al ParticipacionDocente indicado
     */
    @Override
    public List<ClaseClon> buscarPorParticipacionDocente(ParticipacionDocente participacionDocente) {
        return claseClonRepositorio.findByParticipacionDocenteAndBajaFalse(participacionDocente);
    }

    /**
     * Busca los registros vigentes de claseClon asociados a estadoClaseClon.
     *
     * @param estadoClaseClon el registro de EstadoClaseClon asociado
     * @return una lista de ClaseClon asociados al EstadoClaseClon indicado
     */
    @Override
    public List<ClaseClon> buscarPorEstadoClaseClon(EstadoClaseClon estadoClaseClon) {
        return claseClonRepositorio.findByEstadoClaseClonAndBajaFalse(estadoClaseClon);
    }

    /**
     * Busca los registros vigentes de claseClon asociados a material.
     *
     * @param material el registro de Material asociado
     * @return una lista de ClaseClon asociados al Material indicado
     */
    @Override
    public List<ClaseClon> buscarPorMaterial(Material material) {
        return claseClonRepositorio.findByMaterialAndBajaFalse(material);
    }
}
