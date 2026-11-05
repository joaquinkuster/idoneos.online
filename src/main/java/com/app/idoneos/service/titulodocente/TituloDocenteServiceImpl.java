package com.app.idoneos.service.titulodocente;

import com.app.idoneos.model.Docente;
import com.app.idoneos.model.TituloDocente;
import com.app.idoneos.repository.TituloDocenteRepository;
import com.app.idoneos.service.CrudService;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link TituloDocente}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link TituloDocenteService}.
 */
@Service
public class TituloDocenteServiceImpl implements TituloDocenteService {

    @Autowired
    private TituloDocenteRepository tituloDocenteRepository;

    // ---------------------------------------------------------------- Lecturas

    /**
     * Busca el título del docente por su identificador. Solo devuelve registros activos (no dados de baja).
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<TituloDocente> buscarPorId(Integer id) {
        return tituloDocenteRepository.findById(id)
                .filter(entidad -> !entidad.esInactivo()); // Solo devuelve registros activos
    }

    /**
     * Obtiene todos los registros. Solo devuelve registros activos.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<TituloDocente> obtenerTodo() {
        return tituloDocenteRepository.findByBajaFalse();
    }

    /**
     * Verifica si existe un registro con el identificador dado y está activo.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return tituloDocenteRepository.existsById(id) &&
                buscarPorId(id).isPresent(); // Solo cuenta registros activos
    }

    /**
     * Busca los registros vigentes de tituloDocente asociados a docente.
     *
     * @param docente el registro de Docente asociado
     * @return una lista de TituloDocente asociados al Docente indicado
     */
    @Override
    public List<TituloDocente> buscarPorDocente(Docente docente) {
        return tituloDocenteRepository.findByDocenteAndBajaFalse(docente);
    }

    // ---------------------------------------------------------------- Creaciones

    /**
     * Guarda el título del docente en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public TituloDocente guardar(TituloDocente entidad) {
        return tituloDocenteRepository.save(entidad);
    }

    // ---------------------------------------------------------------- Modificaciones

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public TituloDocente modificar(TituloDocente entidad) {
        return tituloDocenteRepository.save(entidad);
    }

    // ---------------------------------------------------------------- Bajas

    /**
     * Da de baja el registro (baja lógica), en lugar de eliminarlo de la base de datos.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(TituloDocente entidad) {
        entidad.marcarInactivo(); // Baja lógica
        tituloDocenteRepository.save(entidad);
    }
}
