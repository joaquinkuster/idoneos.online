package com.app.idoneos.service.notificacion;

import java.util.List;

import com.app.idoneos.model.Cohorte;
import com.app.idoneos.model.Notificacion;
import com.app.idoneos.model.Usuario;

/** Servicio de la notificación. */
public interface NotificacionService {

    /** Cantidad máxima de intentos para enviar el correo de una notificación. */
    int MAXIMO_INTENTOS = 3;

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /**
     * Busca las notificaciones más recientes de un usuario, para el panel de la campana.
     *
     * @param usuario El usuario.
     * @return Sus últimas notificaciones, de la más reciente a la más antigua.
     */
    List<Notificacion> buscarRecientes(Usuario usuario);

    /**
     * Busca todas las notificaciones de un usuario.
     *
     * @param usuario El usuario.
     * @return Sus notificaciones, de la más reciente a la más antigua.
     */
    List<Notificacion> buscarTodas(Usuario usuario);

    /**
     * Cuenta las notificaciones que un usuario todavía no leyó.
     *
     * @param usuario El usuario.
     * @return La cantidad de notificaciones sin leer.
     */
    long contarNoLeidas(Usuario usuario);

    // ----------------------------------------------------------------------
    // Creaciones
    // ----------------------------------------------------------------------

    /**
     * Crea una notificación para un usuario. Aparece en su bandeja y, si se pide, también se le envía por correo.
     *
     * @param usuario Usuario que recibe el aviso.
     * @param tipo Nombre del tipo de notificación (una constante de TipoNotificacion).
     * @param asunto Asunto del correo y título del aviso, de hasta 150 caracteres.
     * @param mensaje Texto del aviso, de hasta 500 caracteres.
     * @param url Dirección del sistema a la que lleva el aviso, o {@code null} si no lleva a ninguna.
     * @param cohorte Cohorte a la que se refiere, o {@code null} si no corresponde.
     * @param porCorreo Indica si además se envía por correo electrónico.
     * @return La notificación creada, o {@code null} si el tipo de notificación no está definido (el error queda en el registro).
     */
    Notificacion notificar(Usuario usuario, String tipo, String asunto, String mensaje, String url, Cohorte cohorte, boolean porCorreo);

    /**
     * Crea una notificación para varios usuarios a la vez.
     *
     * @param usuarios Usuarios que reciben el aviso.
     * @param tipo Nombre del tipo de notificación.
     * @param asunto Asunto del correo y título del aviso.
     * @param mensaje Texto del aviso.
     * @param url Dirección del sistema a la que lleva el aviso, o {@code null}.
     * @param cohorte Cohorte a la que se refiere, o {@code null}.
     * @param porCorreo Indica si además se envía por correo electrónico.
     */
    void notificarATodos(List<Usuario> usuarios, String tipo, String asunto, String mensaje, String url, Cohorte cohorte, boolean porCorreo);

    /**
     * Crea una notificación para todos los administradores activos.
     *
     * @param tipo Nombre del tipo de notificación.
     * @param asunto Asunto del correo y título del aviso.
     * @param mensaje Texto del aviso.
     * @param url Dirección del sistema a la que lleva el aviso, o {@code null}.
     * @param cohorte Cohorte a la que se refiere, o {@code null}.
     * @param porCorreo Indica si además se envía por correo electrónico.
     */
    void notificarAAdministradores(String tipo, String asunto, String mensaje, String url, Cohorte cohorte, boolean porCorreo);

    /**
     * Indica si el usuario ya recibió una notificación de ese tipo con ese mismo mensaje, para no avisarle dos veces.
     *
     * @param usuario El usuario.
     * @param tipo Nombre del tipo de notificación.
     * @param mensaje Texto del aviso.
     * @return {@code true} si ya la recibió.
     */
    boolean yaNotificado(Usuario usuario, String tipo, String mensaje);

    // ----------------------------------------------------------------------
    // Modificaciones
    // ----------------------------------------------------------------------

    /**
     * Marca una notificación como leída y devuelve la dirección a la que lleva.
     *
     * @param idNotificacion Identificador de la notificación.
     * @param usuario El usuario que la abre, que debe ser su destinatario.
     * @return La dirección a la que lleva, o {@code null} si no lleva a ninguna.
     * @throws IllegalArgumentException Si la notificación no existe o es de otro usuario.
     */
    String abrir(int idNotificacion, Usuario usuario);

    /**
     * Marca como leídas todas las notificaciones de un usuario.
     *
     * @param usuario El usuario.
     */
    void marcarTodasLeidas(Usuario usuario);

    /** Envía por correo las notificaciones pendientes y reintenta las que fallaron, hasta el máximo de intentos. */
    void enviarPendientes();
}
