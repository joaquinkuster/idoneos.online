package com.app.idoneos.servicio;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.app.idoneos.modelo.Autoevaluacion;
import com.app.idoneos.modelo.Docente;
import com.app.idoneos.modelo.Programa;
import com.app.idoneos.modelo.Tema;
import com.app.idoneos.modelo.TemaCronograma;
import com.app.idoneos.repositorio.CursoRepositorio;
import com.app.idoneos.repositorio.DocenteRepositorio;
import com.app.idoneos.servicio.Cohorte.CohorteServicio;
import com.app.idoneos.servicio.TemaCronograma.TemaCronogramaServicio;

/**
 * Pruebas de la exigencia de autoevaluaciones en los programas: CU-12 (registrar una cohorte) y CU-25 (modificar el
 * cronograma de un programa con cohortes en curso) piden que cada tema tenga al menos una autoevaluación visible.
 * "Macroeconomía de Coyuntura" tiene un programa de tres temas con una autoevaluación cada una y una cohorte abierta.
 */
@SpringBootTest
@Transactional
class ReglasDeAutoevaluacionPorProgramaServicioTest {

    @Autowired
    private CohorteServicio cohorteServicio;

    @Autowired
    private TemaCronogramaServicio cronogramaServicio;

    @Autowired
    private CursoRepositorio cursoRepositorio;

    @Autowired
    private DocenteRepositorio docenteRepositorio;

    private Programa macro() {
        return cursoRepositorio.findAll().stream().filter(c -> c.getNombre().equals("Macroeconomía de Coyuntura"))
                .findFirst().orElseThrow().getProgramaVigente();
    }

    private Tema tipoDeCambio() {
        return macro().getCronogramaOrdenado().get(1).getTema();
    }

    private void sacarLasAutoevaluaciones(Tema tema) {
        tema.getAutoevaluacionesPublicadas().forEach(Autoevaluacion::marcarInactivo);
    }

    private void registrarCohorte(Programa programa) {
        LocalDate hoy = LocalDate.now();
        cohorteServicio.registrarCohorte(programa.getIdPrograma(), hoy.plusDays(1), hoy.plusDays(30), null, 70, null);
    }

    private Docente fausto() {
        return docenteRepositorio.findAll().stream()
                .filter(d -> d.getUsuario().getCorreo().equals("fausto.spotorno@idoneos.online")).findFirst().orElseThrow();
    }

    @Test
    @DisplayName("CU-12: si algún tema del programa no tiene autoevaluación visible, la cohorte queda pendiente")
    void registrarCohorte() {
        assertDoesNotThrow(() -> registrarCohorte(macro()));
        assertTrue(cohorteServicio.faltantesDeContenido(macro()).isEmpty());

        Tema tema = tipoDeCambio();
        sacarLasAutoevaluaciones(tema);
        assertTrue(macro().getTemasSinAutoevaluacion().contains(tema));
        assertTrue(cohorteServicio.faltantesDeContenido(macro()).stream().anyMatch(f -> f.contains("«Tipo de cambio»")));

        // Como el contenido quedó incompleto, la inscripción debe empezar con el plazo mínimo
        String error = assertThrows(IllegalArgumentException.class, () -> registrarCohorte(macro())).getMessage();
        assertTrue(error.contains("posterior a hoy"), error);

        LocalDate inicio = LocalDate.now().plusDays(cohorteServicio.plazoMinimoParaCompletarContenidoDias());
        var pendiente = cohorteServicio.registrarCohorte(macro().getIdPrograma(), inicio, inicio.plusDays(30), null, 70, null);
        assertTrue(pendiente.esPendiente());
    }

    @Test
    @DisplayName("El programa informa qué le falta para tener el contenido completo")
    void faltantesDeContenido() {
        assertTrue(macro().getFaltantesDeContenido(1).isEmpty());
        assertTrue(macro().tieneContenidoCompleto(1));

        sacarLasAutoevaluaciones(tipoDeCambio());

        List<String> faltantes = macro().getFaltantesDeContenido(1);
        assertTrue(faltantes.stream().anyMatch(f -> f.contains("Faltan autoevaluaciones visibles en: «Tipo de cambio»")), faltantes.toString());
        assertTrue(!macro().tieneContenidoCompleto(1));
        // un mínimo de temas que el programa no alcanza también se informa
        assertTrue(macro().getFaltantesDeContenido(10).stream().anyMatch(f -> f.contains("requeridos")));
    }

    @Test
    @DisplayName("El programa no está completo si no asigna toda la duración del curso o si faltan datos del curso")
    void faltantesPorDuracionYDatosDelCurso() {
        Programa programa = macro();
        assertTrue(programa.estaDuracionAsignadaCompleta());

        // con más duración en el curso que en los temas, falta asignar
        programa.getCurso().setDuracionDias(programa.getDuracionTotalDias() + 7);
        assertTrue(!programa.estaDuracionAsignadaCompleta());
        assertTrue(programa.getFaltantesDeContenido(1).stream().anyMatch(f -> f.contains("duración asignada")),
                programa.getFaltantesDeContenido(1).toString());
        programa.getCurso().setDuracionDias(programa.getDuracionTotalDias());

        // y si el docente no completó el nivel o los objetivos
        programa.getCurso().setObjetivos(null);
        programa.getCurso().setNivel(null);
        assertTrue(programa.getFaltantesDeContenido(1).stream()
                .anyMatch(f -> f.equals("Faltan completar los datos del curso: nivel, objetivos")),
                programa.getFaltantesDeContenido(1).toString());
    }

    @Test
    @DisplayName("CU-25: con cohortes en curso el cronograma no puede incluir un tema sin autoevaluación visible")
    void modificarCronograma() {
        Programa programa = macro();
        List<TemaCronograma> cronograma = programa.getCronogramaOrdenado();
        List<Integer> ids = cronograma.stream().map(c -> c.getTema().getIdTema()).toList();
        List<String> semanas = cronograma.stream().map(c -> String.valueOf(c.getDuracionDias())).toList();

        assertDoesNotThrow(() -> cronogramaServicio.modificarCronograma(fausto(), programa.getIdPrograma(), ids, semanas));

        sacarLasAutoevaluaciones(tipoDeCambio());
        String error = assertThrows(IllegalArgumentException.class,
                () -> cronogramaServicio.modificarCronograma(fausto(), programa.getIdPrograma(), ids, semanas)).getMessage();
        assertTrue(error.contains("cohortes en curso"), error);
        assertTrue(error.contains("«Tipo de cambio»"), error);

        // Si se quita del cronograma el tema sin autoevaluación, la modificación es válida
        List<Integer> sinLaTema = ids.stream().filter(id -> id != tipoDeCambio().getIdTema()).toList();
        assertDoesNotThrow(() -> cronogramaServicio.modificarCronograma(fausto(), programa.getIdPrograma(), sinLaTema,
                sinLaTema.stream().map(id -> "2").toList()));
    }
}
