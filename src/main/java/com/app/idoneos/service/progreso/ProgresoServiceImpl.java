package com.app.idoneos.service.progreso;

import java.util.HashMap;

import com.app.idoneos.model.Cohorte;
import com.app.idoneos.model.Inscripcion;
import com.app.idoneos.model.Programa;
import com.app.idoneos.model.Progreso;
import com.app.idoneos.model.Tema;
import com.app.idoneos.model.TemaCronograma;
import com.app.idoneos.repository.InscripcionRepository;
import com.app.idoneos.repository.ProgresoRepository;
import com.app.idoneos.service.CrudService;
import com.app.idoneos.service.certificado.CertificadoService;
import com.app.idoneos.service.inscripcion.InscripcionService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link Progreso}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link ProgresoService}.
 */
@Service
public class ProgresoServiceImpl implements ProgresoService {

    @Autowired
    private ProgresoRepository progresoRepository;

    @Autowired
    private com.app.idoneos.repository.EstadoAvanceRepository estadoAvanceRepository;

    @Autowired
    private InscripcionService inscripcionService;

    @Autowired
    private InscripcionRepository inscripcionRepository;

    @Autowired
    private CertificadoService certificadoService;

    // ---------------------------------------------------------------- Lecturas

    /**
     * Busca el progreso por su identificador. Devuelve el registro si existe.
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<Progreso> buscarPorId(Integer id) {
        return progresoRepository.findById(id);
    }

    /**
     * Obtiene todos los registros. Devuelve todos los registros.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<Progreso> obtenerTodo() {
        return progresoRepository.findAll();
    }

    /**
     * Verifica si existe un registro con el identificador dado.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return progresoRepository.existsById(id);
    }

    /**
     * Busca los registros de progreso asociados a tema.
     *
     * @param tema el registro de Tema asociado
     * @return una lista de Progreso asociados al Tema indicado
     */
    @Override
    public List<Progreso> buscarPorTema(Tema tema) {
        return progresoRepository.findByTema(tema);
    }


    /**
     * Busca el progreso a partir de la combinación única de sus relaciones. Incluye los registros dados de baja.
     *
     * @param inscripcion el registro de Inscripcion asociado
     * @param tema el registro de Tema asociado
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    @Override
    public Optional<Progreso> buscarPorInscripcionYTema(Inscripcion inscripcion, Tema tema) {
        return progresoRepository.findByInscripcionAndTema(inscripcion, tema);
    }

    @Override
    public List<Inscripcion> buscarConFiltros(Integer cursoId, String alumno) {
        return inscripcionService.buscarConFiltros(cursoId, alumno, null);
    }

    @Override
    public DetalleProgreso buscarDetalle(Integer idInscripcion) {
        Inscripcion inscripcion = inscripcionRepository.findById(idInscripcion).filter(i -> !i.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! La inscripción seleccionada no existe."));
        Map<Integer, Progreso> completados = new java.util.HashMap<>();
        for (Progreso progreso : progresoRepository.findByInscripcion(inscripcion)) {
            if (Boolean.TRUE.equals(progreso.getCompletada())) {
                completados.put(progreso.getTema().getIdTema(), progreso);
            }
        }
        List<DetalleProgreso.AvanceTema> temas = inscripcion.getCohorte().getPrograma().getCronogramaOrdenado()
                .stream().map((TemaCronograma cronograma) -> {
                    Progreso progreso = completados.get(cronograma.getTema().getIdTema());
                    return new DetalleProgreso.AvanceTema(cronograma, progreso != null,
                            progreso == null ? null : progreso.getFechaCompletada());
                }).toList();
        return new DetalleProgreso(inscripcion, temas);
    }

    // ---------------------------------------------------------------- Creaciones

    /**
     * Guarda el progreso en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public Progreso guardar(Progreso entidad) {
        return progresoRepository.save(entidad);
    }

    // ---------------------------------------------------------------- Modificaciones

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public Progreso modificar(Progreso entidad) {
        return progresoRepository.save(entidad);
    }

    /**
     * Registra el tema como completada para un alumno si la aprobó (ver {@link Tema#estaAprobadaPor}).
     */
    @Override
    @Transactional
    public boolean actualizar(Inscripcion inscripcion, Tema tema) {
        boolean esDelPrograma = inscripcion.getCohorte().getPrograma().getCronogramaOrdenado().stream()
                .anyMatch(cronograma -> cronograma.getTema().getIdTema() == tema.getIdTema());
        if (inscripcion.esInactivo() || !esDelPrograma || inscripcion.estaTemaCompletada(tema)
                || !tema.estaAprobadaPor(inscripcion)) {
            return false;
        }
        Progreso progreso = progresoRepository.findByInscripcionAndTema(inscripcion, tema).orElseGet(() -> {
            Progreso nuevo = progresoRepository.save(new Progreso(tema, inscripcion));
            inscripcion.getProgresos().add(nuevo);
            return nuevo;
        });
        progreso.setCompletada(true);
        progreso.setFechaCompletada(LocalDateTime.now());
        progresoRepository.save(progreso);
        // Con la último tema pendiente completada, el alumno terminó el programa: se emite su certificado
        if (inscripcion.getTemaEnCurso() == null) {
            // Terminó el programa: el avance pasa a Finalizado y se emite su certificado
            inscripcion.actualizarAvance(estadoAvanceRepository.findByNombre(com.app.idoneos.model.EstadoAvance.FINALIZADO).orElseThrow());
            inscripcionRepository.save(inscripcion);
            certificadoService.emitir(inscripcion);
        }
        return true;
    }

    /**
     * Verifica el tema para cada alumno con inscripción vigente en un programa que la incluye.
     */
    @Override
    @Transactional
    public void actualizarPorTema(Tema tema) {
        for (Programa programa : tema.getProgramasQueLaIncluyen()) {
            for (Cohorte cohorte : programa.getCohortesActivas()) {
                for (Inscripcion inscripcion : cohorte.getInscripciones()) {
                    if (!inscripcion.esInactivo() && inscripcion.fueHabilitada()) {
                        actualizar(inscripcion, tema);
                    }
                }
            }
        }
    }

    // ---------------------------------------------------------------- Bajas

    /**
     * Elimina el registro de la base de datos. Esta entidad no tiene baja lógica.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(Progreso entidad) {
        progresoRepository.delete(entidad);
    }

    /**
     * Revierte el tema a no completada y anula el certificado vigente de la inscripción, si lo tenía.
     */
    @Override
    @Transactional
    public boolean revertir(Inscripcion inscripcion, Tema tema) {
        Progreso progreso = progresoRepository.findByInscripcionAndTema(inscripcion, tema)
                .filter(p -> Boolean.TRUE.equals(p.getCompletada())).orElse(null);
        if (progreso == null) {
            return false;
        }
        progreso.setCompletada(false);
        progreso.setFechaCompletada(null);
        progresoRepository.save(progreso);
        certificadoService.anular(inscripcion);
        return true;
    }
}
