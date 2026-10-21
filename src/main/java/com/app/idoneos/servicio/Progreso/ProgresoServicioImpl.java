package com.app.idoneos.servicio.Progreso;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.app.idoneos.modelo.Cohorte;
import com.app.idoneos.modelo.Inscripcion;
import com.app.idoneos.modelo.Programa;
import com.app.idoneos.modelo.Progreso;
import com.app.idoneos.modelo.UnidadCronograma;
import com.app.idoneos.modelo.Unidad;
import com.app.idoneos.repositorio.InscripcionRepositorio;
import com.app.idoneos.repositorio.ProgresoRepositorio;
import com.app.idoneos.servicio.Certificado.CertificadoServicio;
import com.app.idoneos.servicio.CrudServicio;
import com.app.idoneos.servicio.Inscripcion.InscripcionServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link Progreso}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link ProgresoServicio}.
 */
@Service
public class ProgresoServicioImpl implements ProgresoServicio, CrudServicio<Progreso> {

    @Autowired
    private ProgresoRepositorio progresoRepositorio;

    @Autowired
    private InscripcionServicio inscripcionServicio;

    @Autowired
    private InscripcionRepositorio inscripcionRepositorio;

    @Autowired
    private CertificadoServicio certificadoServicio;

    // ---------------------------------------------------------------- Lecturas

    /**
     * Busca el progreso por su identificador. Devuelve el registro si existe.
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<Progreso> buscarPorId(Integer id) {
        return progresoRepositorio.findById(id);
    }

    /**
     * Obtiene todos los registros. Devuelve todos los registros.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<Progreso> obtenerTodo() {
        return progresoRepositorio.findAll();
    }

    /**
     * Verifica si existe un registro con el identificador dado.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return progresoRepositorio.existsById(id);
    }

    /**
     * Busca los registros de progreso asociados a unidad.
     *
     * @param unidad el registro de Unidad asociado
     * @return una lista de Progreso asociados al Unidad indicado
     */
    @Override
    public List<Progreso> buscarPorUnidad(Unidad unidad) {
        return progresoRepositorio.findByUnidad(unidad);
    }

    /**
     * Busca los registros de progreso asociados a inscripcion.
     *
     * @param inscripcion el registro de Inscripcion asociado
     * @return una lista de Progreso asociados al Inscripcion indicado
     */
    @Override
    public List<Progreso> buscarPorInscripcion(Inscripcion inscripcion) {
        return progresoRepositorio.findByInscripcion(inscripcion);
    }

    /**
     * Busca el progreso a partir de la combinación única de sus relaciones. Incluye los registros dados de baja.
     *
     * @param inscripcion el registro de Inscripcion asociado
     * @param unidad el registro de Unidad asociado
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    @Override
    public Optional<Progreso> buscarPorInscripcionYUnidad(Inscripcion inscripcion, Unidad unidad) {
        return progresoRepositorio.findByInscripcionAndUnidad(inscripcion, unidad);
    }

    @Override
    public List<Inscripcion> buscarConFiltros(Integer cursoId, String alumno) {
        return inscripcionServicio.buscarConFiltros(cursoId, alumno, null);
    }

    @Override
    public DetalleProgreso buscarDetalle(Integer idInscripcion) {
        Inscripcion inscripcion = inscripcionRepositorio.findById(idInscripcion).filter(i -> !i.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! La inscripción seleccionada no existe."));
        Map<Integer, Progreso> completados = new java.util.HashMap<>();
        for (Progreso progreso : progresoRepositorio.findByInscripcion(inscripcion)) {
            if (Boolean.TRUE.equals(progreso.getCompletada())) {
                completados.put(progreso.getUnidad().getIdUnidad(), progreso);
            }
        }
        List<DetalleProgreso.AvanceUnidad> unidades = inscripcion.getCohorte().getPrograma().getCronogramaOrdenado()
                .stream().map((UnidadCronograma cronograma) -> {
                    Progreso progreso = completados.get(cronograma.getUnidad().getIdUnidad());
                    return new DetalleProgreso.AvanceUnidad(cronograma, progreso != null,
                            progreso == null ? null : progreso.getFechaCompletada());
                }).toList();
        return new DetalleProgreso(inscripcion, unidades);
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
        return progresoRepositorio.save(entidad);
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
        return progresoRepositorio.save(entidad);
    }

    /**
     * Registra la unidad como completada para un alumno si la aprobó (ver {@link Unidad#estaAprobadaPor}).
     */
    @Override
    @Transactional
    public boolean actualizarProgreso(Inscripcion inscripcion, Unidad unidad) {
        boolean esDelPrograma = inscripcion.getCohorte().getPrograma().getCronogramaOrdenado().stream()
                .anyMatch(cronograma -> cronograma.getUnidad().getIdUnidad() == unidad.getIdUnidad());
        if (inscripcion.esInactivo() || !esDelPrograma || inscripcion.estaUnidadCompletada(unidad)
                || !unidad.estaAprobadaPor(inscripcion)) {
            return false;
        }
        Progreso progreso = progresoRepositorio.findByInscripcionAndUnidad(inscripcion, unidad).orElseGet(() -> {
            Progreso nuevo = progresoRepositorio.save(new Progreso(unidad, inscripcion));
            inscripcion.getProgresos().add(nuevo);
            return nuevo;
        });
        progreso.setCompletada(true);
        progreso.setFechaCompletada(LocalDateTime.now());
        progresoRepositorio.save(progreso);
        // Con la última unidad pendiente completada, el alumno terminó el programa: se emite su certificado
        if (inscripcion.getUnidadEnCurso() == null) {
            certificadoServicio.emitirCertificado(inscripcion);
        }
        return true;
    }

    /**
     * Verifica la unidad para cada alumno con inscripción vigente en un programa que la incluye.
     */
    @Override
    @Transactional
    public void actualizarProgresoDeLaUnidad(Unidad unidad) {
        for (Programa programa : unidad.getProgramasQueLaIncluyen()) {
            for (Cohorte cohorte : programa.getCohortesActivas()) {
                for (Inscripcion inscripcion : cohorte.getInscripciones()) {
                    if (!inscripcion.esInactivo() && inscripcion.estaHabilitada()) {
                        actualizarProgreso(inscripcion, unidad);
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
        progresoRepositorio.delete(entidad);
    }

    /**
     * Revierte la unidad a no completada y anula el certificado vigente de la inscripción, si lo tenía.
     */
    @Override
    @Transactional
    public boolean revertirProgreso(Inscripcion inscripcion, Unidad unidad) {
        Progreso progreso = progresoRepositorio.findByInscripcionAndUnidad(inscripcion, unidad)
                .filter(p -> Boolean.TRUE.equals(p.getCompletada())).orElse(null);
        if (progreso == null) {
            return false;
        }
        progreso.setCompletada(false);
        progreso.setFechaCompletada(null);
        progresoRepositorio.save(progreso);
        certificadoServicio.anularCertificados(inscripcion);
        return true;
    }
}
