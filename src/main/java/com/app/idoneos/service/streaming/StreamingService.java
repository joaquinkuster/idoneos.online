package com.app.idoneos.service.streaming;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import com.app.idoneos.model.ClaseEnVivo;

/** Servicio de la transmisión de las clases en vivo con el servidor de medios. */
public interface StreamingService {

    /**
     * Datos que el docente carga en OBS para transmitir una clase.
     *
     * @param urlRtmp campo Servidor de OBS
     * @param clave campo Clave de transmisión de OBS, que solo se conoce al generarla porque en la base queda su resumen
     * @param resumen resumen de la clave, que es lo que se guarda en la base
     */
    record DatosDeConexion(String urlRtmp, String clave, String resumen) {
    }

    /**
     * Estado de la señal de una clase.
     *
     * @param transmitiendo si el servidor está recibiendo la señal del docente
     * @param espectadores cantidad de alumnos conectados
     */
    record EstadoDeSenal(boolean transmitiendo, int espectadores) {
    }

    // ----------------------------------------------------------------------
    // Conexión del docente
    // ----------------------------------------------------------------------

    /**
     * Genera los datos de conexión de una clase, con una clave nueva.
     *
     * @param clase la clase que se va a transmitir
     * @return la dirección del servidor y la clave que carga el docente, y el resumen de la clave que se guarda
     */
    DatosDeConexion generarConexion(ClaseEnVivo clase);

    /**
     * Obtiene el resumen con el que se guarda una clave de transmisión.
     *
     * @param clave la clave tal como la envía OBS
     * @return el resumen de la clave
     */
    String resumirClave(String clave);

    /**
     * Corta la señal que está transmitiendo el docente de una clase.
     *
     * @param clase la clase
     */
    void cortarSenal(ClaseEnVivo clase);

    /**
     * Consulta si el servidor recibe la señal de una clase.
     *
     * @param clase la clase
     * @return el estado de la señal, o vacío si no se pudo consultar al servidor
     */
    Optional<EstadoDeSenal> consultarSenal(ClaseEnVivo clase);

    // ----------------------------------------------------------------------
    // Reproducción
    // ----------------------------------------------------------------------

    /**
     * Firma el acceso de un usuario a la transmisión de una clase.
     *
     * @param clase la clase
     * @param idUsuario el usuario que mira
     * @return el token que acompaña a cada pedido del reproductor
     */
    String firmarAcceso(ClaseEnVivo clase, int idUsuario);

    /**
     * Verifica un token de acceso a la transmisión.
     *
     * @param token el token firmado
     * @param camino el camino de la transmisión que se quiere ver
     * @return si el token es válido para ese camino y no venció
     */
    boolean validarAcceso(String token, String camino);

    /**
     * Obtiene la dirección donde el reproductor pide la transmisión de una clase.
     *
     * @param clase la clase
     * @return la dirección de la lista de reproducción
     */
    String urlDeReproduccion(ClaseEnVivo clase);

    /**
     * Obtiene el camino con que el servidor identifica la transmisión de una clase.
     *
     * @param clase la clase
     * @return el camino
     */
    String camino(ClaseEnVivo clase);

    /**
     * Verifica la clave de la API con la que el servidor se consulta a sí mismo.
     *
     * @param clave la clave recibida
     * @return si es la de la aplicación
     */
    boolean esClaveDeApi(String clave);

    // ----------------------------------------------------------------------
    // Grabación
    // ----------------------------------------------------------------------

    /**
     * Busca los archivos que el servidor grabó de una clase, esperando unos segundos a que termine de escribirlos.
     *
     * @param clase la clase
     * @return los archivos, del más antiguo al más nuevo
     */
    List<Path> buscarGrabaciones(ClaseEnVivo clase);

    /**
     * Guarda una grabación del servidor en su destino, lista para reproducirse en el navegador: si puede, la reescribe con el
     * índice al principio del archivo para que cargue enseguida, y si no, la copia tal como está.
     *
     * @param origen el archivo que grabó el servidor
     * @param destino el archivo que se publica
     * @throws java.io.IOException si no se puede escribir el destino
     */
    void exportarGrabacion(Path origen, Path destino) throws java.io.IOException;

    /**
     * Borra lo que el servidor grabó de una clase.
     *
     * @param clase la clase
     */
    void borrarGrabaciones(ClaseEnVivo clase);
}
