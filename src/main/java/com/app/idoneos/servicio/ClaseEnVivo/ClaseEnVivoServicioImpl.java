package com.app.idoneos.servicio.ClaseEnVivo;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.ClaseEnVivo;
import com.app.idoneos.modelo.Cohorte;
import com.app.idoneos.modelo.EstadoClaseEnVivo;
import com.app.idoneos.modelo.Material;
import com.app.idoneos.modelo.ParticipacionDocente;
import com.app.idoneos.repositorio.ClaseEnVivoRepositorio;
import com.app.idoneos.servicio.CrudServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link ClaseEnVivo}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link ClaseEnVivoServicio}.
 */
@Service
public class ClaseEnVivoServicioImpl implements ClaseEnVivoServicio, CrudServicio<ClaseEnVivo> {

    @Autowired
    private ClaseEnVivoRepositorio claseEnVivoRepositorio;

    /**
     * Guarda la clase en vivo en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public ClaseEnVivo guardar(ClaseEnVivo entidad) {
        return claseEnVivoRepositorio.save(entidad);
    }

    /**
     * Busca la clase en vivo por su identificador. Solo devuelve registros activos (no dados de baja).
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<ClaseEnVivo> buscarPorId(Integer id) {
        return claseEnVivoRepositorio.findById(id)
                .filter(entidad -> !entidad.esInactivo()); // Solo devuelve registros activos
    }

    /**
     * Obtiene todos los registros. Solo devuelve registros activos.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<ClaseEnVivo> obtenerTodo() {
        return claseEnVivoRepositorio.findByBajaFalse();
    }

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public ClaseEnVivo modificar(ClaseEnVivo entidad) {
        return claseEnVivoRepositorio.save(entidad);
    }

    /**
     * Da de baja el registro (baja lógica), en lugar de eliminarlo de la base de datos.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(ClaseEnVivo entidad) {
        entidad.marcarInactivo(); // Baja lógica
        claseEnVivoRepositorio.save(entidad);
    }

    /**
     * Verifica si existe un registro con el identificador dado y está activo.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return claseEnVivoRepositorio.existsById(id) &&
                buscarPorId(id).isPresent(); // Solo cuenta registros activos
    }

    /**
     * Busca los registros vigentes de claseEnVivo asociados a participacionDocente.
     *
     * @param participacionDocente el registro de ParticipacionDocente asociado
     * @return una lista de ClaseEnVivo asociados al ParticipacionDocente indicado
     */
    @Override
    public List<ClaseEnVivo> buscarPorParticipacionDocente(ParticipacionDocente participacionDocente) {
        return claseEnVivoRepositorio.findByParticipacionDocenteAndBajaFalse(participacionDocente);
    }

    /**
     * Busca los registros vigentes de claseEnVivo asociados a estadoClaseEnVivo.
     *
     * @param estadoClaseEnVivo el registro de EstadoClaseEnVivo asociado
     * @return una lista de ClaseEnVivo asociados al EstadoClaseEnVivo indicado
     */
    @Override
    public List<ClaseEnVivo> buscarPorEstadoClaseEnVivo(EstadoClaseEnVivo estadoClaseEnVivo) {
        return claseEnVivoRepositorio.findByEstadoClaseEnVivoAndBajaFalse(estadoClaseEnVivo);
    }

    /**
     * Busca los registros vigentes de claseEnVivo asociados a material.
     *
     * @param material el registro de Material asociado
     * @return una lista de ClaseEnVivo asociados al Material indicado
     */
    @Override
    public List<ClaseEnVivo> buscarPorMaterial(Material material) {
        return claseEnVivoRepositorio.findByMaterialAndBajaFalse(material);
    }

    /**
     * Busca los registros vigentes de claseEnVivo asociados a cohorte.
     *
     * @param cohorte el registro de Cohorte asociado
     * @return una lista de ClaseEnVivo asociados al Cohorte indicado
     */
    @Override
    public List<ClaseEnVivo> buscarPorCohorte(Cohorte cohorte) {
        return claseEnVivoRepositorio.findByCohorteAndBajaFalse(cohorte);
    }

    /**
     * Busca la clase en vivo por su atributo único 'claveStream'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param claveStream el valor de 'claveStream' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    @Override
    public Optional<ClaseEnVivo> buscarPorClaveStream(String claveStream) {
        return claseEnVivoRepositorio.findByClaveStream(claveStream);
    }
}
