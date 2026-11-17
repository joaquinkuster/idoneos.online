package com.app.idoneos.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Configuración de las tareas programadas y en segundo plano. Habilita en toda la aplicación las tareas con @Scheduled y los métodos con @Async. */
@Configuration
@EnableScheduling
@EnableAsync
public class TareasProgramadasConfig {
}
