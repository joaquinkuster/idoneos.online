package com.app.idoneos.service.heygen;

/** Servicio de integración con HeyGen: crea el avatar y la voz del clon de un docente y genera los videos con ese clon. */
public interface HeygenService {

    /** Estado de un video en HeyGen. */
    record Video(String estado, String url, Double duracionSegundos, String error) {

        /** Indica si el video ya está listo para descargar. */
        public boolean estaListo() {
            return "completed".equals(estado);
        }

        /** Indica si la generación falló. */
        public boolean fallo() {
            return "failed".equals(estado);
        }
    }

    /**
     * Crea el avatar de un docente a partir de su foto y espera a que HeyGen termine de procesarlo.
     *
     * @param nombre nombre del avatar
     * @param imagen contenido de la foto (JPG o PNG)
     * @param archivo nombre del archivo, con su extensión
     * @return el identificador del avatar ({@code avatar_id})
     * @throws IllegalArgumentException si HeyGen rechaza la foto (por ejemplo, es insuficiente) o no se puede crear
     */
    String crearAvatarConFoto(String nombre, byte[] imagen, String archivo);

    /**
     * Crea la voz de un docente a partir de una muestra de audio y espera a que HeyGen la active.
     *
     * @param nombre nombre de la voz
     * @param audio contenido del audio (MP3, WAV o M4A)
     * @param archivo nombre del archivo, con su extensión
     * @return el identificador de la voz ({@code voice_id})
     * @throws IllegalArgumentException si HeyGen rechaza la muestra o no se puede crear
     */
    String crearVozConAudio(String nombre, byte[] audio, String archivo);

    /**
     * Pide a HeyGen un video en el que el clon dice un guion.
     *
     * @param avatarId avatar del docente
     * @param voiceId voz del docente
     * @param titulo título del video
     * @param guion lo que dice el clon
     * @return el identificador del video en HeyGen
     * @throws IllegalArgumentException si HeyGen rechaza el pedido
     */
    String crearVideo(String avatarId, String voiceId, String titulo, String guion);

    /**
     * Consulta el estado de un video.
     *
     * @param idVideo identificador del video en HeyGen
     * @return el estado del video
     */
    Video consultarVideo(String idVideo);

    /**
     * Descarga el archivo de un video terminado.
     *
     * @param url dirección de descarga que informa HeyGen
     * @return el contenido del video
     */
    byte[] descargar(String url);
}
