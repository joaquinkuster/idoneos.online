package com.app.idoneos.servicio.Material;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Material;
import com.app.idoneos.modelo.ParticipacionDocente;
import com.app.idoneos.modelo.TipoMaterial;
import com.app.idoneos.modelo.Unidad;
import com.app.idoneos.repositorio.MaterialRepositorio;
import com.app.idoneos.servicio.CrudServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link Material}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link MaterialServicio}.
 */
@Service
public class MaterialServicioImpl implements MaterialServicio, CrudServicio<Material> {

    @Autowired
    private MaterialRepositorio materialRepositorio;

    /**
     * Guarda el material en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public Material guardar(Material entidad) {
        return materialRepositorio.save(entidad);
    }

    /**
     * Busca el material por su identificador. Solo devuelve registros activos (no dados de baja).
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<Material> buscarPorId(Integer id) {
        return materialRepositorio.findById(id)
                .filter(entidad -> !entidad.esInactivo()); // Solo devuelve registros activos
    }

    /**
     * Obtiene todos los registros. Solo devuelve registros activos.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<Material> obtenerTodo() {
        return materialRepositorio.findByBajaFalse();
    }

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public Material modificar(Material entidad) {
        return materialRepositorio.save(entidad);
    }

    /**
     * Da de baja el registro (baja lógica), en lugar de eliminarlo de la base de datos.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(Material entidad) {
        entidad.marcarInactivo(); // Baja lógica
        materialRepositorio.save(entidad);
    }

    /**
     * Verifica si existe un registro con el identificador dado y está activo.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return materialRepositorio.existsById(id) &&
                buscarPorId(id).isPresent(); // Solo cuenta registros activos
    }

    /**
     * Busca los registros vigentes de material asociados a participacionDocente.
     *
     * @param participacionDocente el registro de ParticipacionDocente asociado
     * @return una lista de Material asociados al ParticipacionDocente indicado
     */
    @Override
    public List<Material> buscarPorParticipacionDocente(ParticipacionDocente participacionDocente) {
        return materialRepositorio.findByParticipacionDocenteAndBajaFalse(participacionDocente);
    }

    /**
     * Busca los registros vigentes de material asociados a tipoMaterial.
     *
     * @param tipoMaterial el registro de TipoMaterial asociado
     * @return una lista de Material asociados al TipoMaterial indicado
     */
    @Override
    public List<Material> buscarPorTipoMaterial(TipoMaterial tipoMaterial) {
        return materialRepositorio.findByTipoMaterialAndBajaFalse(tipoMaterial);
    }

    /**
     * Busca los registros vigentes de material asociados a unidad.
     *
     * @param unidad el registro de Unidad asociado
     * @return una lista de Material asociados al Unidad indicado
     */
    @Override
    public List<Material> buscarPorUnidad(Unidad unidad) {
        return materialRepositorio.findByUnidadAndBajaFalse(unidad);
    }
}
