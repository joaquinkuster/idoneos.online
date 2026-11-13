package com.app.idoneos.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.app.idoneos.model.ClaseEnVivo;
import com.app.idoneos.model.Cohorte;
import com.app.idoneos.model.Docente;
import com.app.idoneos.model.EstadoClaseEnVivo;
import com.app.idoneos.model.Tema;
import com.app.idoneos.repository.ClaseEnVivoRepository;
import com.app.idoneos.repository.CohorteRepository;
import com.app.idoneos.repository.DocenteRepository;
import com.app.idoneos.repository.EstadoClaseEnVivoRepository;
import com.app.idoneos.repository.TemaRepository;
import com.app.idoneos.service.claseenvivo.ClaseEnVivoService;
import com.app.idoneos.service.claseenvivo.ClaseEnVivoService.SolicitudClaseEnVivo;

/** Pruebas del ABM de clases en vivo con los datos de la semilla. */
@SpringBootTest
@Transactional
class ClaseEnVivoServiceTest {

    private static final String CURSO = "Mercado de Capitales Argentino";

    @Autowired
    private ClaseEnVivoService claseEnVivoService;

    @Autowired
    private ClaseEnVivoRepository claseEnVivoRepository;

    @Autowired
    private EstadoClaseEnVivoRepository estadoRepository;

    @Autowired
    private CohorteRepository cohorteRepository;

    @Autowired
    private TemaRepository temaRepository;

    @Autowired
    private DocenteRepository docenteRepository;

    private Docente docente(String correo) {
        return docenteRepository.findAll().stream().filter(d -> d.getUsuario().getCorreo().equals(correo)).findFirst()
                .orElseThrow();
    }

    private Docente fausto() {
        return docente("fausto.spotorno@idoneos.online");
    }

    /** La cohorte en dictado del curso: la que tiene las clases de la semilla. */
    private Cohorte enDictado() {
        LocalDateTime ahora = LocalDateTime.now();
        return cohorteRepository.findAll().stream()
                .filter(c -> c.getCurso().getNombre().equals(CURSO) && !c.getBaja() && c.getFechaInicioDictado() != null
                        && c.getFechaInicioDictado().isBefore(ahora) && c.getFechaFinDictado() != null
                        && c.getFechaFinDictado().isAfter(ahora))
                .findFirst().orElseThrow();
    }

    private Tema tema() {
        return temaRepository.findAll().stream().filter(t -> t.getCurso().getNombre().equals(CURSO)).findFirst().orElseThrow();
    }

    private ClaseEnVivo claseConEstado(String estado) {
        return claseEnVivoRepository.findAll().stream().filter(c -> c.getEstadoClaseEnVivo().getNombre().equals(estado))
                .findFirst().orElseThrow();
    }

    private String dentroDeDias(int dias, int hora) {
        return LocalDateTime.now().plusDays(dias).withHour(hora).withMinute(0).withSecond(0).withNano(0).toString()
                .substring(0, 16);
    }

    /** Arma la solicitud con el inicio indicado y el cierre a los minutos de duración indicados. */
    private SolicitudClaseEnVivo solicitud(String titulo, String inicio, String duracion) {
        String cierre = inicio.isBlank() ? "" : LocalDateTime.parse(inicio).plusMinutes(Long.parseLong(duracion)).toString().substring(0, 16);
        return new SolicitudClaseEnVivo(enDictado().getIdCohorte(), tema().getIdTema(), titulo, inicio, cierre);
    }

    @Test
    @DisplayName("El docente programa una clase: queda Programada, asociada a su cohorte y a su participación")
    void programaUnaClase() {
        ClaseEnVivo clase = claseEnVivoService.registrar(fausto(), solicitud("Clase de prueba", dentroDeDias(10, 10), "60"));

        assertEquals(EstadoClaseEnVivo.PROGRAMADA, clase.getEstadoClaseEnVivo().getNombre());
        assertEquals(enDictado().getIdCohorte(), clase.getCohorte().getIdCohorte());
        assertTrue(clase.esDeDocente(fausto()));
    }

    @Test
    @DisplayName("Rechaza la clase con fecha pasada, fuera del dictado de la cohorte o superpuesta con otra del docente")
    void rechazaLasFechasInvalidas() {
        ClaseEnVivo programada = claseConEstado(EstadoClaseEnVivo.PROGRAMADA);
        String superpuesta = programada.getFechaHoraInicio().plusMinutes(30).toString().substring(0, 16);

        assertThrows(IllegalArgumentException.class,
                () -> claseEnVivoService.registrar(fausto(), solicitud("Pasada", dentroDeDias(-1, 10), "60")));
        assertThrows(IllegalArgumentException.class,
                () -> claseEnVivoService.registrar(fausto(), solicitud("Fuera del dictado", dentroDeDias(400, 10), "60")));
        IllegalArgumentException conflicto = assertThrows(IllegalArgumentException.class,
                () -> claseEnVivoService.registrar(fausto(), solicitud("Superpuesta", superpuesta, "60")));
        assertTrue(conflicto.getMessage().contains("se superpone"));
        assertThrows(IllegalArgumentException.class,
                () -> claseEnVivoService.registrar(fausto(), solicitud("", dentroDeDias(10, 10), "60")));
        // El cierre no puede ser anterior ni igual al inicio
        assertThrows(IllegalArgumentException.class,
                () -> claseEnVivoService.registrar(fausto(), solicitud("Sin duración", dentroDeDias(10, 10), "0")));
    }

    @Test
    @DisplayName("El aviso de fuera del dictado muestra las fechas del período, sin las horas")
    void elAvisoDelDictadoNoMuestraLasHoras() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> claseEnVivoService.registrar(fausto(), solicitud("Fuera del dictado", dentroDeDias(400, 10), "60")));

        assertTrue(error.getMessage().contains("período de dictado"));
        assertTrue(error.getMessage().matches(".*del \\d{2}/\\d{2}/\\d{4} al \\d{2}/\\d{2}/\\d{4}\\)\\..*"));
    }

    @Test
    @DisplayName("No se programan clases para una cohorte con la cursada finalizada")
    void noProgramaEnCohortesFinalizadas() {
        Cohorte finalizada = cohorteRepository.findAll().stream()
                .filter(c -> c.getCurso().getNombre().equals(CURSO) && !c.getBaja() && !c.getEstado().equals("En dictado")
                        && (c.getEstado().equals("Cursada finalizada") || c.getEstado().equals("Dictado finalizado")))
                .findFirst().orElseThrow();
        SolicitudClaseEnVivo solicitud = new SolicitudClaseEnVivo(finalizada.getIdCohorte(), tema().getIdTema(), "Clase",
                dentroDeDias(10, 10), dentroDeDias(10, 11));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> claseEnVivoService.registrar(fausto(), solicitud));

        assertTrue(error.getMessage().contains("inscripción próxima, abierta, pendiente de contenido o en dictado"));
    }

    @Test
    @DisplayName("Modifica una clase programada sin chocar con ella misma, y no la deja modificar a otro docente")
    void modificaUnaClaseProgramada() {
        ClaseEnVivo programada = claseConEstado(EstadoClaseEnVivo.PROGRAMADA);
        String mismoHorario = programada.getFechaHoraInicio().toString().substring(0, 16);

        ClaseEnVivo modificada = claseEnVivoService.modificar(programada.getIdClaseEnVivo(), fausto(),
                new SolicitudClaseEnVivo(null, null, "Título nuevo", mismoHorario,
                        programada.getFechaHoraInicio().plusMinutes(120).toString().substring(0, 16)));

        assertEquals("Título nuevo", modificada.getTitulo());
        assertEquals(120, modificada.getDuracion());
        Docente otro = docenteRepository.findAll().stream()
                .filter(d -> d.getIdDocente() != fausto().getIdDocente()).findFirst().orElseThrow();
        assertThrows(IllegalArgumentException.class, () -> claseEnVivoService.modificar(programada.getIdClaseEnVivo(), otro,
                new SolicitudClaseEnVivo(null, null, "Intruso", mismoHorario,
                        programada.getFechaHoraInicio().plusMinutes(60).toString().substring(0, 16))));
    }

    @Test
    @DisplayName("Cancela una clase programada: pasa al estado Cancelada y ya no se puede modificar ni cancelar")
    void cancelaUnaClaseProgramada() {
        ClaseEnVivo programada = claseConEstado(EstadoClaseEnVivo.PROGRAMADA);

        claseEnVivoService.cancelar(programada.getIdClaseEnVivo(), fausto());

        assertTrue(claseEnVivoRepository.findById(programada.getIdClaseEnVivo()).orElseThrow().esCancelada());
        assertThrows(IllegalArgumentException.class,
                () -> claseEnVivoService.cancelar(programada.getIdClaseEnVivo(), fausto()));
        assertThrows(IllegalArgumentException.class, () -> claseEnVivoService.cancelar(
                claseConEstado(EstadoClaseEnVivo.EN_VIVO).getIdClaseEnVivo(), fausto()));
    }

    @Test
    @DisplayName("La cancelación masiva es de todo o nada")
    void cancelacionMasivaTodoONada() {
        ClaseEnVivo programada = claseConEstado(EstadoClaseEnVivo.PROGRAMADA);
        ClaseEnVivo finalizada = claseConEstado(EstadoClaseEnVivo.FINALIZADA);

        assertThrows(IllegalArgumentException.class, () -> claseEnVivoService
                .cancelarVarias(List.of(programada.getIdClaseEnVivo(), finalizada.getIdClaseEnVivo()), fausto()));

        assertTrue(claseEnVivoRepository.findById(programada.getIdClaseEnVivo()).orElseThrow().esProgramada());
    }

    @Test
    @DisplayName("Cancela sola la clase programada que no se inició dentro de la tolerancia")
    void cancelaLasNoIniciadas() {
        ClaseEnVivo programada = claseConEstado(EstadoClaseEnVivo.PROGRAMADA);
        programada.setFechaHoraInicio(LocalDateTime.now().minusHours(2));
        programada.setFechaHoraCierre(programada.getFechaHoraInicio().plusMinutes(60));
        claseEnVivoRepository.save(programada);

        int canceladas = claseEnVivoService.cancelarNoIniciadas();

        assertEquals(1, canceladas);
        assertTrue(claseEnVivoRepository.findById(programada.getIdClaseEnVivo()).orElseThrow().esCancelada());
        assertTrue(claseConEstado(EstadoClaseEnVivo.EN_VIVO).esEnVivo());
    }

    @Test
    @DisplayName("Se puede dar de baja una clase en cualquier estado, y una clase dada de baja no se vuelve a dar de baja")
    void bajaDeCualquierClase() {
        ClaseEnVivo programada = claseConEstado(EstadoClaseEnVivo.PROGRAMADA);
        ClaseEnVivo finalizada = claseConEstado(EstadoClaseEnVivo.FINALIZADA);
        claseEnVivoService.cancelar(programada.getIdClaseEnVivo(), fausto());

        claseEnVivoService.darDeBajaVarias(List.of(programada.getIdClaseEnVivo(), finalizada.getIdClaseEnVivo()));

        assertTrue(claseEnVivoRepository.findById(programada.getIdClaseEnVivo()).orElseThrow().esInactivo());
        assertTrue(claseEnVivoRepository.findById(finalizada.getIdClaseEnVivo()).orElseThrow().esInactivo());
        assertThrows(IllegalArgumentException.class, () -> claseEnVivoService.darDeBaja(finalizada.getIdClaseEnVivo()));
    }

    @Test
    @DisplayName("El listado no incluye las clases dadas de baja")
    void elListadoNoIncluyeLasBajas() {
        ClaseEnVivo finalizada = claseConEstado(EstadoClaseEnVivo.FINALIZADA);
        Integer curso = finalizada.getCohorte().getCurso().getIdCurso();
        claseEnVivoService.darDeBaja(finalizada.getIdClaseEnVivo());

        assertFalse(claseEnVivoService.buscarConFiltros(curso, false, null).stream()
                .anyMatch(c -> c.getIdClaseEnVivo() == finalizada.getIdClaseEnVivo()));
    }
}
