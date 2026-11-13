package com.app.idoneos.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import com.app.idoneos.model.Alumno;
import com.app.idoneos.model.ClaseEnVivo;
import com.app.idoneos.model.Docente;
import com.app.idoneos.model.EstadoClaseEnVivo;
import com.app.idoneos.repository.AlumnoRepository;
import com.app.idoneos.repository.ClaseEnVivoRepository;
import com.app.idoneos.repository.DocenteRepository;
import com.app.idoneos.service.claseenvivo.ClaseEnVivoService;
import com.app.idoneos.service.streaming.StreamingService;
import com.app.idoneos.service.streaming.StreamingService.DatosDeConexion;
import com.app.idoneos.service.streaming.StreamingService.EstadoDeSenal;

/** Pruebas de la transmisión de las clases en vivo, con el servidor de medios simulado. */
@SpringBootTest
@Transactional
class ClaseEnVivoTransmisionTest {

    @Autowired
    private ClaseEnVivoService claseEnVivoService;

    @Autowired
    private ClaseEnVivoRepository claseEnVivoRepository;

    @Autowired
    private DocenteRepository docenteRepository;

    @Autowired
    private AlumnoRepository alumnoRepository;

    @MockitoBean
    private StreamingService streamingService;

    @TempDir
    Path carpeta;

    private Docente fausto() {
        return docenteRepository.findAll().stream()
                .filter(d -> d.getUsuario().getCorreo().equals("fausto.spotorno@idoneos.online")).findFirst().orElseThrow();
    }

    private Alumno alumno(String correo) {
        return alumnoRepository.findAll().stream().filter(a -> a.getUsuario().getCorreo().equals(correo)).findFirst().orElseThrow();
    }

    private ClaseEnVivo clase(String estado) {
        return claseEnVivoRepository.findAll().stream().filter(c -> c.getEstadoClaseEnVivo().getNombre().equals(estado))
                .findFirst().orElseThrow();
    }

    /** Simula el archivo que graba el servidor de medios, y que al exportarlo se copie al destino. */
    private Path grabacion() throws IOException {
        Path archivo = carpeta.resolve("grabacion.mp4");
        Files.write(archivo, new byte[2048]);
        try {
            doAnswer(llamada -> {
                Files.copy(llamada.<Path>getArgument(0), llamada.<Path>getArgument(1),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                return null;
            }).when(streamingService).exportarGrabacion(any(), any());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return archivo;
    }

    @Test
    @DisplayName("El docente inicia la clase cuando llega su horario: queda En vivo y se guarda el resumen de la clave")
    void iniciaLaClase() {
        ClaseEnVivo programada = clase(EstadoClaseEnVivo.PROGRAMADA);
        programada.setFechaHoraInicio(LocalDateTime.now().plusMinutes(5));
        programada.setFechaHoraCierre(programada.getFechaHoraInicio().plusMinutes(60));
        when(streamingService.generarConexion(any())).thenReturn(new DatosDeConexion("rtmp://localhost/clase-1", "?user=docente&pass=abc", "resumen123"));

        DatosDeConexion datos = claseEnVivoService.iniciar(programada.getIdClaseEnVivo(), fausto());

        assertEquals("?user=docente&pass=abc", datos.clave());
        assertTrue(programada.esEnVivo());
        assertEquals("resumen123", programada.getClaveStream());
        assertEquals("rtmp://localhost/clase-1", programada.getUrlRtmp());
    }

    @Test
    @DisplayName("No se puede iniciar una clase antes de la anticipación, ni una que ya no está programada")
    void noIniciaFueraDeHorario() {
        ClaseEnVivo programada = clase(EstadoClaseEnVivo.PROGRAMADA);
        programada.setFechaHoraInicio(LocalDateTime.now().plusDays(3));
        programada.setFechaHoraCierre(programada.getFechaHoraInicio().plusMinutes(60));

        assertThrows(IllegalArgumentException.class, () -> claseEnVivoService.iniciar(programada.getIdClaseEnVivo(), fausto()));
        assertThrows(IllegalArgumentException.class,
                () -> claseEnVivoService.iniciar(clase(EstadoClaseEnVivo.FINALIZADA).getIdClaseEnVivo(), fausto()));
    }

    @Test
    @DisplayName("Al finalizar, la clase pasa a Finalizada al instante y la grabación se publica después, visible y con su vencimiento")
    void finalizaYPublicaLaGrabacion() throws IOException {
        ClaseEnVivo enVivo = clase(EstadoClaseEnVivo.EN_VIVO);
        Path archivo = grabacion();
        when(streamingService.buscarGrabaciones(any())).thenReturn(List.of(archivo));

        ClaseEnVivo finalizada = claseEnVivoService.finalizar(enVivo.getIdClaseEnVivo(), fausto());

        verify(streamingService).cortarSenal(any());
        assertTrue(finalizada.esFinalizada());
        // El alumno se entera enseguida: la grabación todavía no se procesó
        assertNull(finalizada.getGrabacion());

        claseEnVivoService.publicarGrabacion(enVivo.getIdClaseEnVivo());

        assertNotNull(finalizada.getGrabacion());
        assertTrue(finalizada.tieneGrabacionDisponible());
        assertNotNull(finalizada.getPublicacionDeGrabacion());
        assertFalse(finalizada.getPublicacionDeGrabacion().getOculto());
        assertEquals(LocalDate.now().plusMonths(4), finalizada.getFechaVencimientoGrabacion());
        assertEquals(enVivo.getTema().getIdTema(), finalizada.getPublicacionDeGrabacion().getTema().getIdTema());
    }

    @Test
    @DisplayName("Si el servidor no generó la grabación, la clase igual queda finalizada")
    void finalizaSinGrabacion() {
        ClaseEnVivo enVivo = clase(EstadoClaseEnVivo.EN_VIVO);
        when(streamingService.buscarGrabaciones(any())).thenReturn(List.of());

        ClaseEnVivo finalizada = claseEnVivoService.finalizar(enVivo.getIdClaseEnVivo(), fausto());
        claseEnVivoService.publicarGrabacion(enVivo.getIdClaseEnVivo());

        assertTrue(finalizada.esFinalizada());
        assertNull(finalizada.getGrabacion());
    }

    @Test
    @DisplayName("La clase sin señal se cierra sola: Finalizada si llegó a grabarse, Cancelada si nunca llegó señal")
    void cierraLaClaseSinSenal() throws IOException {
        ClaseEnVivo enVivo = clase(EstadoClaseEnVivo.EN_VIVO);
        enVivo.setUltimaModificacion(LocalDateTime.now().minusMinutes(30));
        when(streamingService.consultarSenal(any())).thenReturn(Optional.empty());
        assertEquals(0, claseEnVivoService.cerrarTransmisionesSinSenal());
        assertTrue(enVivo.esEnVivo());

        when(streamingService.consultarSenal(any())).thenReturn(Optional.of(new EstadoDeSenal(false, 0)));
        when(streamingService.buscarGrabaciones(any())).thenReturn(List.of());
        assertEquals(1, claseEnVivoService.cerrarTransmisionesSinSenal());
        assertTrue(enVivo.esCancelada());

        ClaseEnVivo otra = clase(EstadoClaseEnVivo.PROGRAMADA);
        otra.setFechaHoraInicio(LocalDateTime.now().plusMinutes(5));
        otra.setFechaHoraCierre(otra.getFechaHoraInicio().plusMinutes(60));
        when(streamingService.generarConexion(any())).thenReturn(new DatosDeConexion("rtmp://x", "?k", "r2"));
        claseEnVivoService.iniciar(otra.getIdClaseEnVivo(), fausto());
        otra.setUltimaModificacion(LocalDateTime.now().minusMinutes(30));
        Path archivo = grabacion();
        when(streamingService.buscarGrabaciones(any())).thenReturn(List.of(archivo));
        assertEquals(1, claseEnVivoService.cerrarTransmisionesSinSenal());
        assertTrue(otra.esFinalizada());
        claseEnVivoService.publicarGrabacion(otra.getIdClaseEnVivo());
        assertNotNull(otra.getGrabacion());
    }

    @Test
    @DisplayName("Una grabación vencida se da de baja y deja de estar disponible")
    void vencimientoDeLaGrabacion() throws IOException {
        ClaseEnVivo enVivo = clase(EstadoClaseEnVivo.EN_VIVO);
        Path archivo = grabacion();
        when(streamingService.buscarGrabaciones(any())).thenReturn(List.of(archivo));
        ClaseEnVivo finalizada = claseEnVivoService.finalizar(enVivo.getIdClaseEnVivo(), fausto());
        claseEnVivoService.publicarGrabacion(enVivo.getIdClaseEnVivo());
        finalizada.setFechaVencimientoGrabacion(LocalDate.now().minusDays(1));

        assertEquals(1, claseEnVivoService.vencerGrabaciones());

        assertFalse(finalizada.tieneGrabacionDisponible());
        assertNull(finalizada.getPublicacionDeGrabacion());
    }

    @Test
    @DisplayName("Solo ingresa a la transmisión el alumno con inscripción habilitada en la cohorte, y mientras la clase está en vivo")
    void ingresoDelAlumno() {
        ClaseEnVivo enVivo = clase(EstadoClaseEnVivo.EN_VIVO);

        assertEquals(enVivo.getIdClaseEnVivo(),
                claseEnVivoService.autorizarIngreso(enVivo.getIdClaseEnVivo(), alumno("valentina.ruiz@correo.com")).getIdClaseEnVivo());
        assertThrows(IllegalArgumentException.class,
                () -> claseEnVivoService.autorizarIngreso(enVivo.getIdClaseEnVivo(), alumno("lucia.fernandez@correo.com")));
        assertThrows(IllegalArgumentException.class, () -> claseEnVivoService.autorizarIngreso(
                clase(EstadoClaseEnVivo.PROGRAMADA).getIdClaseEnVivo(), alumno("valentina.ruiz@correo.com")));
    }
}
