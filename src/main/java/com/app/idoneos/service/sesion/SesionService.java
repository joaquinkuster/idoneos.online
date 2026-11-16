package com.app.idoneos.service.sesion;

import com.app.idoneos.service.CrudService;
import com.app.idoneos.model.Sesion;
import com.app.idoneos.model.Usuario;

import java.util.List;
import java.util.Optional;

/** Servicio de la sesión. */
public interface SesionService extends CrudService<Sesion> {

    /** Comienzo del token de una sesión recordada, que dura más tiempo y no se renueva con la actividad. */
    String PREFIJO_RECORDADA = "r.";

    /** Comienzo del token de una sesión común, que se renueva con la actividad. */
    String PREFIJO_COMUN = "n.";

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /**
     * Busca una sesión vigente por su token.
     *
     * @param token El token guardado en la cookie del navegador.
     * @return Un {@link Optional} con la sesión si existe y no venció, o vacío si no.
     */
    Optional<Sesion> buscarVigentePorToken(String token);

    /**
     * Busca las sesiones vigentes de un usuario, de la más reciente a la más antigua.
     *
     * @param usuario El usuario.
     * @return Sus sesiones vigentes.
     */
    List<Sesion> buscarVigentes(Usuario usuario);

    /**
     * Busca las sesiones vigentes de todos los usuarios, de la más reciente a la más antigua.
     *
     * @return Todas las sesiones vigentes.
     */
    List<Sesion> buscarTodasVigentes();

    /**
     * Obtiene la cantidad de sesiones simultáneas que puede tener una cuenta.
     *
     * @return El límite configurado.
     */
    int obtenerLimite();

    /**
     * Indica si una sesión es recordada.
     *
     * @param sesion La sesión.
     * @return {@code true} si se inició con la opción "Recordarme".
     */
    boolean esRecordada(Sesion sesion);

    // ----------------------------------------------------------------------
    // Creaciones
    // ----------------------------------------------------------------------

    /**
     * Registra una sesión nueva del usuario, si no alcanzó el límite de sesiones simultáneas.
     *
     * @param usuario El usuario que inicia sesión.
     * @param recordar Indica si eligió "Recordarme".
     * @param ip Dirección IP desde la que ingresa.
     * @param dispositivo Descripción del dispositivo desde el que ingresa.
     * @return La sesión registrada, con el token que se guarda en la cookie.
     * @throws IllegalStateException Si el usuario ya tiene el máximo de sesiones vigentes.
     */
    Sesion iniciar(Usuario usuario, boolean recordar, String ip, String dispositivo);

    // ----------------------------------------------------------------------
    // Modificaciones
    // ----------------------------------------------------------------------

    /**
     * Extiende la vigencia de una sesión común por su latido o su actividad. Las sesiones recordadas no se renuevan.
     *
     * @param sesion La sesión.
     */
    void renovar(Sesion sesion);

    // ----------------------------------------------------------------------
    // Bajas
    // ----------------------------------------------------------------------

    /**
     * Cierra de forma forzada una sesión distinta de la actual.
     *
     * @param idSesion Identificador de la sesión.
     * @param idActor Identificador del usuario que pide el cierre.
     * @param esAdministrador Indica si el actor puede cerrar las sesiones de cualquier usuario.
     * @param tokenActual Token de la sesión desde la que se pide el cierre.
     * @throws IllegalArgumentException Si la sesión no existe, es de otro usuario o es la actual.
     */
    void cerrar(int idSesion, int idActor, boolean esAdministrador, String tokenActual);

    /**
     * Cierra la sesión que tiene el token indicado, si existe.
     *
     * @param token El token de la sesión.
     */
    void cerrarPorToken(String token);

    /**
     * Cierra todas las sesiones de un usuario.
     *
     * @param usuario El usuario.
     */
    void cerrarTodas(Usuario usuario);

    /** Borra las sesiones vencidas. */
    void eliminarVencidas();
}
