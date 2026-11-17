package com.app.idoneos.support;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ScheduledFuture;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;

/**
 * Configuración de pruebas que impide que las tareas programadas se ejecuten solas.
 * Esas tareas cancelan pagos, cierran clases y vencen sesiones según el reloj, y cambiaban los datos de la semilla en medio de las pruebas.
 * Las pruebas que necesitan una tarea la llaman de forma directa.
 */
@Configuration
public class SinTareasProgramadasConfig {

    /**
     * Crea un programador que acepta las tareas y no las ejecuta nunca.
     *
     * @return El programador falso que usan las tareas con {@code @Scheduled}.
     */
    @Bean
    TaskScheduler taskScheduler() {
        TaskScheduler programador = mock(TaskScheduler.class);
        ScheduledFuture<?> tarea = mock(ScheduledFuture.class);
        when(programador.schedule(any(Runnable.class), any(Trigger.class))).thenAnswer(i -> tarea);
        when(programador.schedule(any(Runnable.class), any(Instant.class))).thenAnswer(i -> tarea);
        when(programador.scheduleAtFixedRate(any(Runnable.class), any(Instant.class), any(Duration.class))).thenAnswer(i -> tarea);
        when(programador.scheduleAtFixedRate(any(Runnable.class), any(Duration.class))).thenAnswer(i -> tarea);
        when(programador.scheduleWithFixedDelay(any(Runnable.class), any(Instant.class), any(Duration.class))).thenAnswer(i -> tarea);
        when(programador.scheduleWithFixedDelay(any(Runnable.class), any(Duration.class))).thenAnswer(i -> tarea);
        return programador;
    }
}
