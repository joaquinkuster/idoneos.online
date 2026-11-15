package com.app.idoneos.config;

import java.net.http.HttpClient;
import java.time.Duration;

import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestClient;

/** Configuración de la conexión con el servidor Ollama del modelo de inteligencia artificial. */
@Configuration
public class ModeloIAConfig {

    /**
     * Define el cliente de Ollama con un tiempo de espera propio.
     * Con el límite general de las llamadas externas, de 10 segundos, ninguna generación llegaría a terminar.
     */
    @Bean
    public OllamaApi ollamaApi(@Value("${spring.ai.ollama.base-url:http://localhost:11434}") String url,
            @Value("${idoneos.ia.timeout-minutos:15}") long minutos) {
        JdkClientHttpRequestFactory fabrica = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build());
        fabrica.setReadTimeout(Duration.ofMinutes(minutos));
        return OllamaApi.builder().baseUrl(url).restClientBuilder(RestClient.builder().requestFactory(fabrica)).build();
    }

    /**
     * Define la cola de generaciones con inteligencia artificial.
     * Un solo hilo, porque el modelo corre en la CPU del servidor y dos pedidos a la vez se harían más lentos que uno detrás del otro.
     * Si la cola se llena, el pedido se rechaza y el docente puede reintentar más tarde.
     */
    @Bean
    public TaskExecutor colaIA(@Value("${idoneos.ia.cola-maxima:20}") int maximo) {
        ThreadPoolTaskExecutor cola = new ThreadPoolTaskExecutor();
        cola.setCorePoolSize(1);
        cola.setMaxPoolSize(1);
        cola.setQueueCapacity(maximo);
        cola.setThreadNamePrefix("ia-");
        return cola;
    }
}
