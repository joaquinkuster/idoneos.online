package com.app.idoneos.servicio.Inscripcion;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Alumno;
import com.app.idoneos.modelo.Cohorte;
import com.app.idoneos.modelo.Inscripcion;
import com.app.idoneos.repositorio.InscripcionRepositorio;
import com.app.idoneos.servicio.CrudServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link Inscripcion}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link InscripcionServicio}.
 */
@Service
public class InscripcionServicioImpl implements InscripcionServicio, CrudServicio<Inscripcion> {

    @Autowired
    private InscripcionRepositorio inscripcionRepositorio;

    /**
     * Guarda la inscripción en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public Inscripcion guardar(Inscripcion entidad) {
        return inscripcionRepositorio.save(entidad);
    }

    /**
     * Busca la inscripción por su identificador. Solo devuelve registros activos (no dados de baja).
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<Inscripcion> buscarPorId(Integer id) {
        return inscripcionRepositorio.findById(id)
                .filter(entidad -> !entidad.esInactivo()); // Solo devuelve registros activos
    }

    /**
     * Obtiene todos los registros. Solo devuelve registros activos.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<Inscripcion> obtenerTodo() {
        return inscripcionRepositorio.findByBajaFalse();
    }

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public Inscripcion modificar(Inscripcion entidad) {
        return inscripcionRepositorio.save(entidad);
    }

    /**
     * Da de baja el registro (baja lógica), en lugar de eliminarlo de la base de datos.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(Inscripcion entidad) {
        entidad.marcarInactivo(); // Baja lógica
        inscripcionRepositorio.save(entidad);
    }

    /**
     * Verifica si existe un registro con el identificador dado y está activo.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return inscripcionRepositorio.existsById(id) &&
                buscarPorId(id).isPresent(); // Solo cuenta registros activos
    }

    /**
     * Busca los registros vigentes de inscripcion asociados a cohorte.
     *
     * @param cohorte el registro de Cohorte asociado
     * @return una lista de Inscripcion asociados al Cohorte indicado
     */
    @Override
    public List<Inscripcion> buscarPorCohorte(Cohorte cohorte) {
        return inscripcionRepositorio.findByCohorteAndBajaFalse(cohorte);
    }

    /**
     * Busca los registros vigentes de inscripcion asociados a alumno.
     *
     * @param alumno el registro de Alumno asociado
     * @return una lista de Inscripcion asociados al Alumno indicado
     */
    @Override
    public List<Inscripcion> buscarPorAlumno(Alumno alumno) {
        return inscripcionRepositorio.findByAlumnoAndBajaFalse(alumno);
    }
}
