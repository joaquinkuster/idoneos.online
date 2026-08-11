package com.app.idoneos.service.Dictado;

import com.app.idoneos.exception.ExcepcionRecursoNoEncontrado;
import com.app.idoneos.exception.ExcepcionValidacion;
import com.app.idoneos.model.Dictado;
import com.app.idoneos.model.Inscripcion;
import com.app.idoneos.model.Programa;
import com.app.idoneos.repository.DictadoRepository;
import com.app.idoneos.repository.InscripcionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementación de servicios para la gestión de dictados (CU-15 a CU-18).
 */
@Service
@Transactional
public class DictadoServiceImpl implements DictadoService {

    @Autowired private DictadoRepository dictadoRepository;
    @Autowired private InscripcionRepository inscripcionRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<Dictado> buscarPorId(Integer id) {
        return dictadoRepository.findById(id).filter(d -> !d.getBaja());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Dictado> obtenerTodo() {
        return dictadoRepository.findAll().stream().filter(d -> !d.getBaja()).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Dictado> obtenerPorPrograma(Programa programa) {
        return dictadoRepository.findByPrograma(programa).stream().filter(d -> !d.getBaja()).toList();
    }

    /**
     * CU-16 — Registrar dictado.
     * Reglas de negocio:
     * - Programa obligatorio.
     * - Fechas de inicio y fin obligatorias y fechaFin posterior a fechaInicio (Excepción CU-16, paso 4).
     * - Cupo máximo mayor a cero (Excepción CU-16, paso 5).
     */
    @Override
    public Dictado registrarDictado(Dictado dictado) {
        if (dictado.getPrograma() == null) {
            throw new ExcepcionValidacion("CU-16 Excepción paso 4: El programa asociado es obligatorio.");
        }
        if (dictado.getFechaInicio() == null || dictado.getFechaFin() == null) {
            throw new ExcepcionValidacion("CU-16 Excepción paso 4: Las fechas de inicio y fin son obligatorias.");
        }
        if (!dictado.getFechaFin().isAfter(dictado.getFechaInicio())) {
            throw new ExcepcionValidacion("CU-16 Excepción paso 4: La fecha de fin debe ser posterior a la fecha de inicio.");
        }
        if (dictado.getCupoMaximo() <= 0) {
            throw new ExcepcionValidacion("CU-16 Excepción paso 5: El cupo máximo debe ser mayor a cero.");
        }

        dictado.setBaja(false);
        return dictadoRepository.save(dictado);
    }

    /**
     * CU-17 — Modificar dictado.
     * Reglas de negocio:
     * - Dictado existente y activo.
     * - Coherencia de fechas.
     * - Cupo no menor a inscripciones activas registradas.
     */
    @Override
    public Dictado modificarDictado(Dictado dictado) {
        Dictado existente = dictadoRepository.findById(dictado.getId())
                .orElseThrow(() -> new ExcepcionRecursoNoEncontrado("Dictado", "id", dictado.getId()));

        if (existente.getBaja()) {
            throw new ExcepcionValidacion("CU-17 Precondición: No se puede modificar un dictado dado de baja.");
        }

        if (dictado.getFechaInicio() == null || dictado.getFechaFin() == null) {
            throw new ExcepcionValidacion("CU-17 Excepción paso 4: Las fechas de inicio y fin son obligatorias.");
        }
        if (!dictado.getFechaFin().isAfter(dictado.getFechaInicio())) {
            throw new ExcepcionValidacion("CU-17 Excepción paso 4: La fecha de fin debe ser posterior a la fecha de inicio.");
        }

        List<Inscripcion> inscripciones = inscripcionRepository.findByDictado(existente);
        long inscriptosActivos = inscripciones.stream().filter(i -> !i.getBaja()).count();
        if (dictado.getCupoMaximo() < inscriptosActivos) {
            throw new ExcepcionValidacion("CU-17 Excepción paso 5: El cupo máximo (" + dictado.getCupoMaximo() + ") no puede ser menor a los alumnos inscriptos actuales (" + inscriptosActivos + ").");
        }

        existente.setFechaInicio(dictado.getFechaInicio());
        existente.setFechaFin(dictado.getFechaFin());
        existente.setCupoMaximo(dictado.getCupoMaximo());
        return dictadoRepository.save(existente);
    }

    /**
     * CU-18 — Eliminar dictado (Baja Lógica).
     * Impide la baja si posee alumnos inscriptos activos.
     */
    @Override
    public void darDeBajaDictado(int dictadoId) {
        Dictado dictado = dictadoRepository.findById(dictadoId)
                .orElseThrow(() -> new ExcepcionRecursoNoEncontrado("Dictado", "id", dictadoId));

        if (dictado.getBaja()) {
            throw new ExcepcionValidacion("CU-18 Excepción: El dictado ya se encuentra dado de baja.");
        }

        List<Inscripcion> inscripciones = inscripcionRepository.findByDictado(dictado);
        boolean tieneInscripcionesActivas = inscripciones.stream().anyMatch(i -> !i.getBaja());
        if (tieneInscripcionesActivas) {
            throw new ExcepcionValidacion("CU-18 Excepción paso 5: No se puede dar de baja el dictado porque posee alumnos inscriptos.");
        }

        dictado.setBaja(true);
        dictadoRepository.save(dictado);
    }

    @Override
    public Dictado guardar(Dictado dictado) {
        return registrarDictado(dictado);
    }

    @Override
    public Dictado modificar(Dictado dictado) {
        return modificarDictado(dictado);
    }

    @Override
    public void borrar(Dictado dictado) {
        darDeBajaDictado(dictado.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorId(Integer id) {
        return dictadoRepository.existsById(id) && buscarPorId(id).isPresent();
    }
}
