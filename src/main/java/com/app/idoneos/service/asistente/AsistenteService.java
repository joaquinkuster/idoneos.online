package com.app.idoneos.service.asistente;

import java.util.List;

import com.app.idoneos.model.Usuario;

/** Servicio del asistente navegacional. Le dice al usuario qué tiene pendiente según su rol activo y lo lleva a la pantalla donde se resuelve. */
public interface AsistenteService {

    /** La cantidad máxima de pendientes que se listan en el panel. */
    int MAXIMO_PENDIENTES = 5;

    /** Días que faltan para que la etiqueta de la primera carta pase a "pronto", en dorado. */
    int DIAS_PRONTO = 7;

    /** Días que faltan para que ese "pronto" pase a rojo. */
    int DIAS_URGENTE = 3;

    /**
     * Un pendiente del usuario.
     *
     * @param titulo Qué es: la autoevaluación, la cohorte ("Mes Año") o el alumno del pago.
     * @param curso El nombre del curso.
     * @param accion El texto de la acción, como se la llama en el resto del sistema.
     * @param fecha Cuándo cierra o abre, por ejemplo "Cierra el 09/11", o {@code null} si no tiene fecha.
     * @param pronto La marca de la primera carta cuando falta poco, por ejemplo "Cierra pronto", o {@code null}.
     * @param urgencia {@code "alta"} (faltan {@link #DIAS_URGENTE} días o menos, en rojo), {@code "media"} (hasta {@link #DIAS_PRONTO}, en dorado) o {@code null}.
     * @param url A dónde lleva la acción.
     */
    record Pendiente(String titulo, String curso, String accion, String fecha, String pronto, String urgencia, String url) {
    }

    /**
     * Un atajo del panel.
     *
     * @param texto El texto del atajo.
     * @param url A dónde lleva.
     */
    record Atajo(String texto, String url) {
    }

    /**
     * Lo que muestra el panel.
     *
     * @param nombre El nombre de pila del usuario (para "¿Por dónde seguimos, Martín?").
     * @param resumen Lo que tiene pendiente, en una línea (por ejemplo, "7 autoevaluaciones pendientes en 2 cursos"), o que está al día.
     * @param aviso Una advertencia que acompaña a la ruta (por ejemplo, que la cohorte se cancela si no completa el contenido), o {@code null}.
     * @param titulo El nombre de la lista de pendientes (por ejemplo, "Autoevaluaciones pendientes").
     * @param total La cantidad total de pendientes (la lista trae como mucho {@link #MAXIMO_PENDIENTES}).
     * @param pendientes Los primeros pendientes, ya ordenados.
     * @param atajos Las acciones rápidas del rol.
     */
    record Respuesta(String nombre, String resumen, String aviso, String titulo, int total, List<Pendiente> pendientes, List<Atajo> atajos) {
    }

    /**
     * Arma lo que ve el usuario en el asistente, según su rol activo.
     *
     * @param usuario El usuario autenticado.
     * @return La respuesta del asistente.
     */
    Respuesta armar(Usuario usuario);
}
