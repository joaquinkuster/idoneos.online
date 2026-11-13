package com.app.idoneos.service.claseenvivo;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Servicio del chat de las clases en vivo. Los mensajes viven solo en la memoria de la aplicación y se borran al terminar la clase,
 * por eso solo sirve con una instancia de la aplicación.
 */
@Service
public class ChatClaseService {

    /** Largo máximo de un mensaje. */
    public static final int LARGO_MAXIMO = 300;

    /** Mensajes que se guardan de cada clase, para quien entra tarde. */
    public static final int MENSAJES_GUARDADOS = 200;

    /** Milisegundos sin avisar su presencia después de los cuales un alumno deja de contarse como conectado. */
    public static final long VIGENCIA_DE_PRESENCIA_MS = 15_000;

    /** Milisegundos que debe esperar un usuario entre un mensaje y el siguiente. */
    public static final long ESPERA_ENTRE_MENSAJES_MS = 2000;

    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Mensaje del chat.
     *
     * @param id identificador dentro de la aplicación
     * @param autor nombre con que se muestra a quien lo escribió
     * @param imagen dirección de la foto de perfil de quien lo escribió, o null si no tiene
     * @param iniciales iniciales de quien lo escribió, para cuando no tiene foto
     * @param docente si lo escribió el docente de la clase
     * @param texto el texto, sin escapar: quien lo muestra lo inserta como texto
     * @param hora hora en que se escribió
     */
    public record Mensaje(long id, String autor, String imagen, String iniciales, boolean docente, String texto, String hora) {
    }

    /**
     * Quien escribe en el chat.
     *
     * @param idUsuario identificador del usuario
     * @param nombre nombre con que se muestra
     * @param imagen dirección de su foto de perfil, o null si no tiene
     * @param iniciales sus iniciales
     * @param docente si es el docente de la clase
     */
    public record Autor(int idUsuario, String nombre, String imagen, String iniciales, boolean docente) {
    }

    /** Sala de chat de una clase. */
    private static final class Sala {
        private final Deque<Mensaje> mensajes = new ArrayDeque<>();
        private final List<SseEmitter> suscriptores = new CopyOnWriteArrayList<>();
        private final Map<Integer, Long> ultimoMensaje = new ConcurrentHashMap<>();
        private final Map<Integer, Long> presencia = new ConcurrentHashMap<>();
        private volatile boolean habilitado = true;
    }

    private final Map<Integer, Sala> salas = new ConcurrentHashMap<>();
    private final AtomicLong siguienteId = new AtomicLong(1);
    private final ObjectMapper json = new ObjectMapper();

    private Sala sala(int idClase) {
        return salas.computeIfAbsent(idClase, id -> new Sala());
    }

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /** Obtiene los últimos mensajes de la clase. */
    public List<Mensaje> historial(int idClase) {
        Sala sala = sala(idClase);
        synchronized (sala) {
            return new ArrayList<>(sala.mensajes);
        }
    }

    /** Indica si el chat de la clase está habilitado. */
    public boolean estaHabilitado(int idClase) {
        return sala(idClase).habilitado;
    }

    // ----------------------------------------------------------------------
    // Mensajes
    // ----------------------------------------------------------------------

    /**
     * Envía un mensaje al chat de la clase.
     *
     * @param idClase la clase
     * @param autor quien escribe
     * @param texto el texto del mensaje
     * @return el mensaje publicado
     * @throws IllegalArgumentException si el chat está deshabilitado, el texto está vacío o es muy largo, o se escribe muy seguido
     */
    public Mensaje enviar(int idClase, Autor autor, String texto) {
        boolean docente = autor.docente();
        int idUsuario = autor.idUsuario();
        Sala sala = sala(idClase);
        String limpio = texto == null ? "" : texto.replaceAll("\\p{Cntrl}", " ").trim();
        if (!sala.habilitado && !docente) {
            throw new IllegalArgumentException("Error! El docente deshabilitó el chat.");
        }
        if (limpio.isEmpty()) {
            throw new IllegalArgumentException("Error! Escribí un mensaje.");
        }
        if (limpio.length() > LARGO_MAXIMO) {
            throw new IllegalArgumentException("Error! El mensaje no puede superar los " + LARGO_MAXIMO + " caracteres.");
        }
        long ahora = System.currentTimeMillis();
        if (!docente) {
            Long anterior = sala.ultimoMensaje.get(idUsuario);
            if (anterior != null && ahora - anterior < ESPERA_ENTRE_MENSAJES_MS) {
                throw new IllegalArgumentException("Error! Esperá un momento antes de enviar otro mensaje.");
            }
            sala.ultimoMensaje.put(idUsuario, ahora);
        }
        Mensaje mensaje = new Mensaje(siguienteId.getAndIncrement(), autor.nombre(), autor.imagen(), autor.iniciales(), docente, limpio, LocalTime.now().format(FORMATO_HORA));
        synchronized (sala) {
            sala.mensajes.addLast(mensaje);
            while (sala.mensajes.size() > MENSAJES_GUARDADOS) {
                sala.mensajes.removeFirst();
            }
        }
        emitir(sala, "mensaje", mensaje);
        return mensaje;
    }

    /** Elimina un mensaje del chat de la clase. */
    public void eliminar(int idClase, long idMensaje) {
        Sala sala = sala(idClase);
        boolean quitado;
        synchronized (sala) {
            quitado = sala.mensajes.removeIf(m -> m.id() == idMensaje);
        }
        if (quitado) {
            emitir(sala, "eliminado", Map.of("id", idMensaje));
        }
    }

    /** Habilita o deshabilita el chat de la clase. */
    public void habilitar(int idClase, boolean habilitado) {
        Sala sala = sala(idClase);
        sala.habilitado = habilitado;
        emitir(sala, "habilitado", Map.of("habilitado", habilitado));
    }

    // ----------------------------------------------------------------------
    // Presencia
    // ----------------------------------------------------------------------

    /** Registra que un alumno tiene abierta la sala de la clase en este momento. */
    public void registrarPresencia(int idClase, int idUsuario) {
        sala(idClase).presencia.put(idUsuario, System.currentTimeMillis());
    }

    /** Cuenta los alumnos que avisaron su presencia hace poco. */
    public int conectados(int idClase) {
        Sala sala = salas.get(idClase);
        if (sala == null) {
            return 0;
        }
        long desde = System.currentTimeMillis() - VIGENCIA_DE_PRESENCIA_MS;
        sala.presencia.values().removeIf(visto -> visto < desde);
        return sala.presencia.size();
    }

    // ----------------------------------------------------------------------
    // Suscripción
    // ----------------------------------------------------------------------

    /** Suscribe a un navegador a los mensajes nuevos de la clase. */
    public SseEmitter suscribir(int idClase) {
        Sala sala = sala(idClase);
        SseEmitter emisor = new SseEmitter(0L);
        sala.suscriptores.add(emisor);
        emisor.onCompletion(() -> sala.suscriptores.remove(emisor));
        emisor.onTimeout(() -> sala.suscriptores.remove(emisor));
        emisor.onError(error -> sala.suscriptores.remove(emisor));
        try {
            emisor.send(SseEmitter.event().name("habilitado").data(json.writeValueAsString(Map.of("habilitado", sala.habilitado))));
        } catch (IOException e) {
            sala.suscriptores.remove(emisor);
        }
        return emisor;
    }

    /** Avisa a los navegadores conectados que la clase terminó y borra todo lo del chat. */
    public void cerrar(int idClase) {
        Sala sala = salas.remove(idClase);
        if (sala == null) {
            return;
        }
        emitir(sala, "cerrada", Map.of());
        sala.suscriptores.forEach(SseEmitter::complete);
        sala.suscriptores.clear();
    }

    /** Mantiene vivas las conexiones: un comentario cada tanto evita que un intermediario las corte. */
    @org.springframework.scheduling.annotation.Scheduled(fixedDelay = 25_000, initialDelay = 25_000)
    public void latir() {
        salas.values().forEach(sala -> sala.suscriptores.forEach(emisor -> {
            try {
                emisor.send(SseEmitter.event().comment("latido"));
            } catch (IOException | IllegalStateException e) {
                sala.suscriptores.remove(emisor);
            }
        }));
    }

    private void emitir(Sala sala, String evento, Object datos) {
        String cuerpo;
        try {
            cuerpo = json.writeValueAsString(datos);
        } catch (JsonProcessingException e) {
            return;
        }
        for (SseEmitter emisor : sala.suscriptores) {
            try {
                emisor.send(SseEmitter.event().name(evento).data(cuerpo));
            } catch (IOException | IllegalStateException e) {
                sala.suscriptores.remove(emisor);
            }
        }
    }
}
