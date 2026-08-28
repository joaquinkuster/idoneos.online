package com.app.idoneos.service.modulo_inscripciones;
import com.app.idoneos.service.modulo_reportes.*;

import com.app.idoneos.exception.*;
import com.app.idoneos.repository.modulo_cursos.*;
import com.app.idoneos.repository.modulo_gestion_academica.*;
import com.app.idoneos.repository.modulo_inscripciones.*;
import com.app.idoneos.repository.modulo_evaluaciones.*;
import com.app.idoneos.repository.modulo_clases_vivo.*;
import com.app.idoneos.repository.modulo_ia.*;
import com.app.idoneos.repository.modulo_usuarios.*;
import com.app.idoneos.repository.modulo_auditoria.*;
import com.app.idoneos.repository.modulo_reportes.*;
import com.app.idoneos.repository.modulo_configuracion.*;
import com.app.idoneos.service.modulo_configuracion.*;
import com.app.idoneos.service.modulo_cursos.*;
import com.app.idoneos.service.modulo_gestion_academica.*;
import com.app.idoneos.service.modulo_inscripciones.*;
import com.app.idoneos.service.modulo_evaluaciones.*;
import com.app.idoneos.service.modulo_ia.*;
import com.app.idoneos.service.modulo_usuarios.*;

import com.app.idoneos.model.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * TRAZABILIDAD — Servicio para el seguimiento y calculo del porcentaje de avance del alumno en la cursada.
 *
 * MOD-F-03: Modulo de Inscripciones y Pagos
 *   CU-44 — Inscribir alumno: registro inicial del progreso.
 *   CU-48 — Buscar progreso: consulta y registro de unidades completadas por el estudiante.
 *   CU-63 — Realizar intento: habilitacion secuencial de unidades al aprobar.
 */
@Service
@Transactional
public class ProgresoServiceImpl implements ProgresoService {

    private static final Logger log = LoggerFactory.getLogger(ProgresoServiceImpl.class);

    @Autowired private ProgresoRepository progresoRepository;
    @Autowired private CronogramaRepository cronogramaRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<Progreso> buscarPorId(Integer id) {
        return progresoRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Progreso> obtenerTodo() {
        return progresoRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Progreso> obtenerPorInscripcion(Inscripcion inscripcion) {
        return progresoRepository.findByInscripcion(inscripcion);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Progreso> obtenerPorInscripcionYUnidad(Inscripcion inscripcion, Unidad unidad) {
        return progresoRepository.findByInscripcionAndUnidad(inscripcion, unidad);
    }

    /**
     * CU-44 — Marcar unidad como completada e incrementar avance de cursada.
     */
    @Override
    public Progreso marcarCompletada(Inscripcion inscripcion, Unidad unidad) {
        Optional<Progreso> existente = progresoRepository.findByInscripcionAndUnidad(inscripcion, unidad);
        if (existente.isPresent()) {
            Progreso p = existente.get();
            if (!p.getCompletada()) {
                p.setCompletada(true);
                p.setFechaCompletada(LocalDateTime.now());
                log.info("Unidad '{}' marcada como completada para inscripcion #{}", unidad.getTitulo(), inscripcion.getId());
                return progresoRepository.save(p);
            }
            return p;
        }
        Progreso nuevo = new Progreso(inscripcion, unidad, true);
        log.info("Nuevo progreso completado creado para unidad '{}' e inscripcion #{}", unidad.getTitulo(), inscripcion.getId());
        return progresoRepository.save(nuevo);
    }

    /**
     * CU-44 — Contar unidades completadas por la inscripcion.
     */
    @Override
    @Transactional(readOnly = true)
    public int contarCompletadas(Inscripcion inscripcion) {
        return (int) progresoRepository.findByInscripcion(inscripcion)
                .stream()
                .filter(Progreso::getCompletada)
                .count();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean unidadCompletada(Inscripcion inscripcion, Unidad unidad) {
        return progresoRepository.findByInscripcionAndUnidad(inscripcion, unidad)
                .map(Progreso::getCompletada)
                .orElse(false);
    }

    /**
     * CU-44 paso 6 — Crea el primer registro de Progreso al inscribirse.
     * Obtiene la primera unidad del cronograma ordenado del programa de la cohorte.
     * Si el programa no tiene cronograma, no crea ningun progreso (sin error).
     */
    @Override
    public void registrarProgresoInicial(Inscripcion inscripcion) {
        if (inscripcion == null) return;
        try {
            Programa programa = inscripcion.getCohorte().getPrograma();
            List<Cronograma> cronograma = cronogramaRepository.findByProgramaOrderByNumeroOrden(programa);
            if (cronograma.isEmpty()) {
                log.warn("CU-44: El programa '{}' no tiene cronograma definido. No se crea progreso inicial.", programa.getNombre());
                return;
            }
            Unidad primeraUnidad = cronograma.get(0).getUnidad();
            // Verificar que no exista ya el registro
            if (progresoRepository.findByInscripcionAndUnidad(inscripcion, primeraUnidad).isEmpty()) {
                Progreso p = new Progreso(inscripcion, primeraUnidad, false);
                progresoRepository.save(p);
                log.info("CU-44: Progreso inicial creado para alumno en unidad '{}'", primeraUnidad.getTitulo());
            }
        } catch (Exception e) {
            log.error("CU-44: Error al registrar progreso inicial para inscripcion #{}: {}", inscripcion.getId(), e.getMessage());
        }
    }

    /**
     * CU-63 — Verifica si el alumno puede acceder a la unidad.
     * Regla: la primera unidad del cronograma siempre esta habilitada.
     * El resto requiere que la unidad anterior este completada.
     */
    @Override
    @Transactional(readOnly = true)
    public boolean esUnidadHabilitada(Inscripcion inscripcion, Unidad unidad) {
        if (inscripcion == null || unidad == null) return false;
        try {
            Programa programa = inscripcion.getCohorte().getPrograma();
            List<Cronograma> cronograma = cronogramaRepository.findByProgramaOrderByNumeroOrden(programa);
            if (cronograma.isEmpty()) return false;

            // Primera unidad siempre habilitada
            if (cronograma.get(0).getUnidad().getId() == unidad.getId()) return true;

            // Buscar la unidad anterior en el cronograma
            for (int i = 1; i < cronograma.size(); i++) {
                if (cronograma.get(i).getUnidad().getId() == unidad.getId()) {
                    Unidad unidadAnterior = cronograma.get(i - 1).getUnidad();
                    return unidadCompletada(inscripcion, unidadAnterior);
                }
            }
        } catch (Exception e) {
            log.error("CU-63: Error al verificar habilitacion de unidad: {}", e.getMessage());
        }
        return false;
    }

    /**
     * CU-63 paso 10 — Crea el Progreso para la siguiente unidad del cronograma.
     * Llamado cuando el alumno aprueba una autoevaluacion.
     */
    @Override
    public void habilitarSiguienteUnidad(Inscripcion inscripcion, Unidad unidadAprobada) {
        if (inscripcion == null || unidadAprobada == null) return;
        try {
            Programa programa = inscripcion.getCohorte().getPrograma();
            List<Cronograma> cronograma = cronogramaRepository.findByProgramaOrderByNumeroOrden(programa);

            for (int i = 0; i < cronograma.size() - 1; i++) {
                if (cronograma.get(i).getUnidad().getId() == unidadAprobada.getId()) {
                    Unidad siguienteUnidad = cronograma.get(i + 1).getUnidad();
                    if (progresoRepository.findByInscripcionAndUnidad(inscripcion, siguienteUnidad).isEmpty()) {
                        Progreso siguiente = new Progreso(inscripcion, siguienteUnidad, false);
                        progresoRepository.save(siguiente);
                        log.info("CU-63: Unidad siguiente '{}' habilitada para inscripcion #{}", siguienteUnidad.getTitulo(), inscripcion.getId());
                    }
                    break;
                }
            }
        } catch (Exception e) {
            log.error("CU-63: Error al habilitar siguiente unidad: {}", e.getMessage());
        }
    }

    /**
     * CU-48 — Calcula el porcentaje de avance del alumno (0-100).
     */
    @Override
    @Transactional(readOnly = true)
    public int calcularPorcentajeAvance(Inscripcion inscripcion) {
        if (inscripcion == null) return 0;
        try {
            Programa programa = inscripcion.getCohorte().getPrograma();
            int totalUnidades = cronogramaRepository.findByProgramaOrderByNumeroOrden(programa).size();
            if (totalUnidades == 0) return 0;
            int completadas = contarCompletadas(inscripcion);
            return (int) Math.round((completadas * 100.0) / totalUnidades);
        } catch (Exception e) {
            log.error("CU-48: Error al calcular porcentaje de avance: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * CU-26 — Detecta si el alumno esta atrasado respecto al cronograma esperado.
     * Calcula la semana esperada segun fechaInicioDictado + duracion acumulada del cronograma.
     * Compara con la ultima unidad que el alumno completo.
     */
    @Override
    @Transactional(readOnly = true)
    public boolean detectarAtraso(Inscripcion inscripcion) {
        if (inscripcion == null) return false;
        try {
            LocalDateTime fechaInicio = inscripcion.getCohorte().getFechaInicioDictado();
            if (fechaInicio == null) return false;

            Programa programa = inscripcion.getCohorte().getPrograma();
            List<Cronograma> cronograma = cronogramaRepository.findByProgramaOrderByNumeroOrden(programa);
            if (cronograma.isEmpty()) return false;

            // Calcular en que semana deberia estar el alumno segun la fecha de hoy
            long semanasCursadas = java.time.temporal.ChronoUnit.WEEKS.between(fechaInicio, LocalDateTime.now());

            // Sumar semanas del cronograma hasta encontrar la unidad esperada segun semanas cursadas
            int semanasAcumuladas = 0;
            int indiceEsperado = 0;
            for (int i = 0; i < cronograma.size(); i++) {
                semanasAcumuladas += cronograma.get(i).getSemanasDuracion();
                indiceEsperado = i;
                if (semanasAcumuladas >= semanasCursadas) break;
            }

            // Verificar cuantas unidades completo el alumno
            int completadas = contarCompletadas(inscripcion);

            // Si completo menos unidades de las esperadas, esta atrasado
            return completadas < indiceEsperado;
        } catch (Exception e) {
            log.error("CU-26: Error al detectar atraso: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public Progreso guardar(Progreso progreso) {
        return progresoRepository.save(progreso);
    }

    @Override
    public Progreso modificar(Progreso progreso) {
        return progresoRepository.save(progreso);
    }

    @Override
    public void borrar(Progreso progreso) {
        progresoRepository.delete(progreso);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorId(Integer id) {
        return progresoRepository.existsById(id);
    }
}
