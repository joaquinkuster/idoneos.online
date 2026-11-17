package com.app.idoneos.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.app.idoneos.model.Docente;
import com.app.idoneos.model.Usuario;
import com.app.idoneos.repository.DocenteRepository;
import com.app.idoneos.service.heygen.HeygenService;
import com.app.idoneos.util.ManejadorArchivosUtil;
import com.app.idoneos.util.RespuestaFormularioUtil;

/** Controlador del clon: el docente crea desde su perfil el avatar (con su foto) y la voz (con una muestra de audio) con los que se generan sus videos. */
@Controller
@RequestMapping("/clon")
public class ClonController {

    /** Tamaño máximo de la foto, en bytes. */
    private static final long FOTO_TAMANIO_MAXIMO = 10L * 1024 * 1024;

    /** Tamaño máximo de la muestra de audio, en bytes. */
    private static final long AUDIO_TAMANIO_MAXIMO = 30L * 1024 * 1024;

    private static final List<String> EXTENSIONES_AUDIO = List.of("mp3", "wav", "m4a");

    @Autowired
    private HeygenService heygenService;

    @Autowired
    private DocenteRepository docenteRepository;

    // ----------------------------------------------------------------------
    // Creaciones
    // ----------------------------------------------------------------------

    /** POST /clon/avatar: Crea el avatar del docente con su foto y guarda su identificador. */
    @PostMapping("/avatar")
    public ResponseEntity<Map<String, String>> crearAvatar(@RequestParam(value = "foto", required = false) MultipartFile foto,
            @RequestParam(value = "terminos", defaultValue = "false") boolean terminos, Authentication authentication) {
        try {
            Docente docente = docenteDe(authentication);
            if (!terminos) {
                throw new IllegalArgumentException("Error! Aceptá los términos y condiciones para crear tu clon.");
            }
            if (foto == null || foto.isEmpty()) {
                throw new IllegalArgumentException("Error! Elegí una foto o sacate una con la cámara.");
            }
            byte[] contenido = foto.getBytes();
            validarFoto(foto, contenido);
            String avatarId = heygenService.crearAvatarConFoto("Clon de " + docente.getUsuario().getNombreCompleto(), contenido, nombreSeguro(foto, "foto.jpg"));
            docente.setAvatarId(avatarId);
            docenteRepository.save(docente);
            return RespuestaFormularioUtil.armarRespuestaExitosa("Avatar creado con éxito.");
        } catch (IOException e) {
            return RespuestaFormularioUtil.armarRespuestaConError(new IllegalArgumentException("Error! No se pudo leer la foto. Intentá nuevamente."));
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    /** POST /clon/voz: Crea la voz del docente con una muestra de audio y guarda su identificador. */
    @PostMapping("/voz")
    public ResponseEntity<Map<String, String>> crearVoz(@RequestParam(value = "audio", required = false) MultipartFile audio, Authentication authentication) {
        try {
            Docente docente = docenteDe(authentication);
            if (audio == null || audio.isEmpty()) {
                throw new IllegalArgumentException("Error! Elegí un audio o grabá tu voz con el micrófono.");
            }
            String nombre = nombreSeguro(audio, "voz.wav");
            String extension = ManejadorArchivosUtil.obtenerExtension(nombre);
            if (extension == null || !EXTENSIONES_AUDIO.contains(extension)) {
                throw new IllegalArgumentException("Error! El audio debe ser MP3, WAV o M4A.");
            }
            if (audio.getSize() > AUDIO_TAMANIO_MAXIMO) {
                throw new IllegalArgumentException("Error! El audio no puede superar los 30 MB.");
            }
            String voiceId = heygenService.crearVozConAudio("Voz de " + docente.getUsuario().getNombreCompleto(), audio.getBytes(), nombre);
            docente.setVoiceId(voiceId);
            docenteRepository.save(docente);
            return RespuestaFormularioUtil.armarRespuestaExitosa("Clon creado con éxito.");
        } catch (IOException e) {
            return RespuestaFormularioUtil.armarRespuestaConError(new IllegalArgumentException("Error! No se pudo leer el audio. Intentá nuevamente."));
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    // ----------------------------------------------------------------------
    // Auxiliares
    // ----------------------------------------------------------------------

    /** Verifica que la foto sea un JPG o un PNG de tamaño permitido: se mira la extensión y los primeros bytes del archivo. */
    private static void validarFoto(MultipartFile foto, byte[] contenido) {
        String extension = ManejadorArchivosUtil.obtenerExtension(foto.getOriginalFilename());
        boolean png = contenido.length > 4 && (contenido[0] & 0xFF) == 0x89 && contenido[1] == 'P' && contenido[2] == 'N' && contenido[3] == 'G';
        boolean jpg = contenido.length > 3 && (contenido[0] & 0xFF) == 0xFF && (contenido[1] & 0xFF) == 0xD8 && (contenido[2] & 0xFF) == 0xFF;
        if (extension == null || !List.of("jpg", "jpeg", "png").contains(extension) || !(png || jpg)) {
            throw new IllegalArgumentException("Error! La foto debe ser un archivo JPG o PNG.");
        }
        if (contenido.length > FOTO_TAMANIO_MAXIMO) {
            throw new IllegalArgumentException("Error! La foto no puede superar los 10 MB.");
        }
    }

    /** Obtiene el docente que hace el pedido. */
    private Docente docenteDe(Authentication authentication) {
        Usuario actor = (Usuario) authentication.getPrincipal();
        if (actor.getDocente() == null || !actor.esDocenteActivo()) {
            throw new IllegalArgumentException("Error! El usuario no tiene el rol de docente.");
        }
        return docenteRepository.findById(actor.getDocente().getIdDocente())
                .orElseThrow(() -> new IllegalArgumentException("Error! El usuario no tiene el rol de docente."));
    }

    private static String nombreSeguro(MultipartFile archivo, String porDefecto) {
        String nombre = archivo.getOriginalFilename();
        return nombre == null || nombre.isBlank() ? porDefecto : nombre.replaceAll("[^A-Za-z0-9._-]", "_");
    }
}
