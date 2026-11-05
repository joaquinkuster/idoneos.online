package com.app.idoneos.service.docente;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.idoneos.model.Docente;
import com.app.idoneos.repository.DocenteRepository;
import com.app.idoneos.service.CrudService;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link Docente}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link DocenteService}.
 */
@Service
public class DocenteServiceImpl implements DocenteService {

    @Autowired
    private DocenteRepository docenteRepository;

    // ---------------------------------------------------------------- Lecturas

    /**
     * Busca el docente por su identificador. Devuelve el registro si existe.
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<Docente> buscarPorId(Integer id) {
        return docenteRepository.findById(id);
    }

    /**
     * Obtiene todos los registros. Devuelve todos los registros.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<Docente> obtenerTodo() {
        return docenteRepository.findAll();
    }

    /**
     * Verifica si existe un registro con el identificador dado.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return docenteRepository.existsById(id);
    }





    /**
     * Busca los docentes activos y habilitados, que pueden participar en cursos.
     *
     * @return una lista de docentes habilitados
     */
    @Override
    public List<Docente> buscarHabilitados() {
        return docenteRepository.buscarPorHabilitado();
    }

    // ---------------------------------------------------------------- Creaciones

    /**
     * Guarda el docente en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public Docente guardar(Docente entidad) {
        return docenteRepository.save(entidad);
    }

    // ---------------------------------------------------------------- Modificaciones

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public Docente modificar(Docente entidad) {
        return docenteRepository.save(entidad);
    }

    // ---------------------------------------------------------------- Bajas

    /**
     * Elimina el registro de la base de datos. Esta entidad no tiene baja lógica.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(Docente entidad) {
        docenteRepository.delete(entidad);
    }
}
