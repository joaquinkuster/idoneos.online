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
import com.app.idoneos.modelo.Unidad;
import com.app.idoneos.modelo.UnidadCronograma;
import com.app.idoneos.repositorio.CursoRepositorio;
import com.app.idoneos.repositorio.DocenteRepositorio;
import com.app.idoneos.servicio.Cohorte.CohorteServicio;
import com.app.idoneos.servicio.UnidadCronograma.UnidadCronogramaServicio;

/**
 * Pruebas de la exigencia de autoevaluaciones en los programas: CU-12 (registrar una cohorte) y CU-25 (modificar el
 * cronograma de un programa con cohortes en curso) piden que cada unidad tenga al menos una autoevaluación visible.
 * "Macroeconomía de Coyuntura" tiene un programa de tres unidades con una autoevaluación cada una y una cohorte abierta.
 */
@SpringBootTest
@Transactional
class ReglasDeAutoevaluacionPorProgramaServicioTest {

    @Autowired
    private CohorteServicio cohorteServicio;

    @Autowired
    private UnidadCronogramaServicio cronogramaServicio;

    @Autowired
    private CursoRepositorio cursoRepositorio;

    @Autowired
    private DocenteRepositorio docenteRepositorio;

    private Programa macro() {
        return cursoRepositorio.findAll().stream().filter(c -> c.getNombre().equals("Macroeconomía de Coyuntura"))
                .findFirst().orElseThrow().getProgramaVigente();
    }

    private Unidad tipoDeCambio() {
        return macro().getCronogramaOrdenado().get(1).getUnidad();
    }

    private void sacarLasAutoevaluaciones(Unidad unidad) {
        unidad.getAutoevaluacionesPublicadas().forEach(Autoevaluacion::marcarInactivo);
    }

    private void registrarCohorte(Programa programa) {
        LocalDate hoy = LocalDate.now();
        cohorteServicio.registrarCohorte(programa.getIdPrograma(), hoy.plusDays(1), hoy.plusDays(30), null, null, 10, null);
    }

    private Docente fausto() {
        return docenteRepositorio.findAll().stream()
                .filter(d -> d.getUsuario().getCorreo().equals("fausto.spotorno@idoneos.online")).findFirst().orElseThrow();
    }

    @Test
    @DisplayName("CU-12: no se registra una cohorte si alguna unidad del programa no tiene autoevaluación visible")
    void registrarCohorte() {
        assertDoesNotThrow(() -> registrarCohorte(macro()));

        Unidad unidad = tipoDeCambio();
        sacarLasAutoevaluaciones(unidad);
        assertTrue(macro().getUnidadesSinAutoevaluacion().contains(unidad));

        String error = assertThrows(IllegalArgumentException.class, () -> registrarCohorte(macro())).getMessage();
        assertTrue(error.contains("al menos una autoevaluación visible"), error);
        assertTrue(error.contains("«Tipo de cambio»"), error);
    }

    @Test
    @DisplayName("CU-25: con cohortes en curso el cronograma no puede incluir una unidad sin autoevaluación visible")
    void modificarCronograma() {
        Programa programa = macro();
        List<UnidadCronograma> cronograma = programa.getCronogramaOrdenado();
        List<Integer> ids = cronograma.stream().map(c -> c.getUnidad().getIdUnidad()).toList();
        List<String> semanas = cronograma.stream().map(c -> String.valueOf(c.getSemanasDuracion())).toList();

        assertDoesNotThrow(() -> cronogramaServicio.modificarCronograma(fausto(), programa.getIdPrograma(), ids, semanas));

        sacarLasAutoevaluaciones(tipoDeCambio());
        String error = assertThrows(IllegalArgumentException.class,
                () -> cronogramaServicio.modificarCronograma(fausto(), programa.getIdPrograma(), ids, semanas)).getMessage();
        assertTrue(error.contains("cohortes en curso"), error);
        assertTrue(error.contains("«Tipo de cambio»"), error);

        // Si se quita del cronograma la unidad sin autoevaluación, la modificación es válida
        List<Integer> sinLaUnidad = ids.stream().filter(id -> id != tipoDeCambio().getIdUnidad()).toList();
        assertDoesNotThrow(() -> cronogramaServicio.modificarCronograma(fausto(), programa.getIdPrograma(), sinLaUnidad,
                sinLaUnidad.stream().map(id -> "2").toList()));
    }
}
