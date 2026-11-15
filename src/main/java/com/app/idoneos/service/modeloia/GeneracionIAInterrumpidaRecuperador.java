package com.app.idoneos.service.modeloia;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.app.idoneos.model.EstadoGeneracionIA;
import com.app.idoneos.model.MaterialLectura;
import com.app.idoneos.model.Pool;
import com.app.idoneos.repository.EstadoGeneracionIARepository;
import com.app.idoneos.repository.MaterialLecturaRepository;
import com.app.idoneos.repository.PoolRepository;

/** Componente que cierra como fallidas las generaciones con inteligencia artificial que quedaron a medias al reiniciarse la aplicación. */
@Component
public class GeneracionIAInterrumpidaRecuperador {

    private static final Logger REGISTRO = LoggerFactory.getLogger(GeneracionIAInterrumpidaRecuperador.class);

    /** Motivo que se le muestra al docente. */
    static final String MOTIVO = "La generación se interrumpió porque el sistema se reinició. Se puede volver a pedir.";

    private final PoolRepository poolRepository;
    private final MaterialLecturaRepository materialLecturaRepository;
    private final EstadoGeneracionIARepository estadoRepository;

    /** Crea el componente con los repositorios que necesita. */
    public GeneracionIAInterrumpidaRecuperador(PoolRepository poolRepository, MaterialLecturaRepository materialLecturaRepository,
            EstadoGeneracionIARepository estadoRepository) {
        this.poolRepository = poolRepository;
        this.materialLecturaRepository = materialLecturaRepository;
        this.estadoRepository = estadoRepository;
    }

    /** Marca con error los pools y materiales que estaban pendientes cuando la aplicación se detuvo. */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void recuperar() {
        List<String> enCurso = List.of(EstadoGeneracionIA.PENDIENTE);
        List<Pool> pools = poolRepository.findByEstadoGeneracionIaNombreIn(enCurso);
        List<MaterialLectura> materiales = materialLecturaRepository.findByEstadoGeneracionIaNombreIn(enCurso);
        if (pools.isEmpty() && materiales.isEmpty()) {
            return;
        }
        EstadoGeneracionIA error = estadoRepository.findByNombre(EstadoGeneracionIA.FALLIDO).orElseThrow();
        for (Pool pool : pools) {
            pool.setEstadoGeneracionIa(error);
            pool.setDetalleError(MOTIVO);
        }
        for (MaterialLectura material : materiales) {
            material.setEstadoGeneracionIa(error);
            material.setDetalleError(MOTIVO);
        }
        REGISTRO.warn("Se cerraron como fallidas {} pools y {} materiales que quedaron generándose.", pools.size(), materiales.size());
    }
}
