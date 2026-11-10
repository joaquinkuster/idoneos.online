package com.app.idoneos.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Configuración de las tareas programadas. Habilita en toda la aplicación la ejecución de las tareas con @Scheduled. */
@Configuration
@EnableScheduling
public class TareasProgramadasConfig {
}
