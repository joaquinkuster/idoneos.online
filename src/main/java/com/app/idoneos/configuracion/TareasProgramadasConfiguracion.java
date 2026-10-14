package com.app.idoneos.configuracion;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Habilita en toda la aplicación la ejecución de las tareas programadas ({@code @Scheduled}). Hoy hay una sola: la
 * conciliación de los pagos online pendientes con MODO, que resuelve los pagos cuyo aviso no llegó y rechaza los que
 * vencieron sin confirmación (ver {@code PagoServicioImpl.conciliarPagosPendientesPeriodicamente}).
 *
 * Sin esta configuración, Spring ignora las anotaciones {@code @Scheduled} y esas tareas nunca se ejecutan.
 */
@Configuration
@EnableScheduling
public class TareasProgramadasConfiguracion {
}
