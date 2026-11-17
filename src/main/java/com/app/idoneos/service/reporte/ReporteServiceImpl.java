package com.app.idoneos.service.reporte;

import com.app.idoneos.model.Curso;
import com.app.idoneos.model.Reporte;
import com.app.idoneos.repository.ReporteRepository;
import com.app.idoneos.service.CrudService;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Implementación de ReporteService. */
@Service
public class ReporteServiceImpl implements ReporteService {

    @Autowired
    private ReporteRepository reporteRepository;

    @Autowired
    private com.app.idoneos.repository.TipoReporteRepository tipoReporteRepository;

    @Autowired
    private com.app.idoneos.repository.CursoRepository cursoRepository;

    @Autowired
    private com.app.idoneos.service.estadistica.EstadisticaService estadisticaService;

    @Autowired
    private com.app.idoneos.service.parametro.ParametroService parametroService;

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /** Busca el reporte por su identificador. */
    @Override
    public Optional<Reporte> buscarPorId(Integer id) {
        return reporteRepository.findById(id);
    }

    /** Obtiene todos los registros. */
    @Override
    public List<Reporte> obtenerTodo() {
        return reporteRepository.findAll();
    }

    /** Verifica si existe un registro con el identificador dado. */
    @Override
    public boolean existePorId(Integer id) {
        return reporteRepository.existsById(id);
    }

    /** Busca los registros de reporte asociados a curso. */
    @Override
    public List<Reporte> buscarPorCurso(Curso curso) {
        return reporteRepository.findByCurso(curso);
    }

    /** Obtiene el historial de informes generados, del más reciente al más antiguo. */
    @Override
    public List<Reporte> buscarRecientes() {
        return reporteRepository.findAllByOrderByFechaGeneracionDescIdReporteDesc();
    }

    // ----------------------------------------------------------------------
    // Creaciones
    // ----------------------------------------------------------------------

    /** Genera el informe de un curso y registra la generación en el historial. */
    @Override
    @org.springframework.transaction.annotation.Transactional
    public byte[] generarInforme(com.app.idoneos.model.Administrador administrador, String tipo, Integer idCurso, java.time.LocalDate desde,
            java.time.LocalDate hasta) {
        if (administrador == null) {
            throw new IllegalArgumentException("Error! El usuario no tiene el rol de administrador.");
        }
        com.app.idoneos.model.TipoReporte tipoReporte = tipoReporteRepository.findByNombre(tipo == null ? "" : tipo)
                .orElseThrow(() -> new IllegalArgumentException("Error! Elegí el tipo de informe."));
        Curso curso = idCurso == null ? null : cursoRepository.findById(idCurso).filter(c -> !c.getBaja()).orElse(null);
        if (curso == null) {
            throw new IllegalArgumentException("Error! Elegí el curso sobre el que querés informar.");
        }
        String emisor = administrador.getUsuario().getNombreCompleto();
        String empresa = parametroService.obtenerTexto(com.app.idoneos.model.Parametro.INSTITUCION_RAZON_SOCIAL).filter(v -> !v.isBlank())
                .orElse("Idóneos Online");
        byte[] pdf;
        if (com.app.idoneos.model.TipoReporte.INFORME_ALUMNOS.equals(tipoReporte.getNombre())) {
            pdf = com.app.idoneos.util.InformePdfUtil.alumnos(estadisticaService.informeAlumnos(curso, desde, hasta), emisor, empresa);
        } else {
            pdf = com.app.idoneos.util.InformePdfUtil.ingresos(estadisticaService.informeIngresos(curso, desde, hasta), emisor, empresa);
        }
        reporteRepository.save(new Reporte(administrador, tipoReporte, curso, java.time.LocalDateTime.now()));
        return pdf;
    }

    /** Guarda el reporte en la base de datos. */
    @Override
    public Reporte guardar(Reporte entidad) {
        return reporteRepository.save(entidad);
    }

    // ----------------------------------------------------------------------
    // Modificaciones
    // ----------------------------------------------------------------------

    /** Modifica un registro existente. */
    @Override
    public Reporte modificar(Reporte entidad) {
        return reporteRepository.save(entidad);
    }

    // ----------------------------------------------------------------------
    // Bajas
    // ----------------------------------------------------------------------

    /** Elimina el registro de la base de datos. */
    @Override
    public void borrar(Reporte entidad) {
        reporteRepository.delete(entidad);
    }
}
