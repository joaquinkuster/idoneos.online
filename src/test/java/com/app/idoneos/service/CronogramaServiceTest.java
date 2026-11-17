package com.app.idoneos.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.app.idoneos.model.Curso;
import com.app.idoneos.model.Programa;
import com.app.idoneos.model.TemaCronograma;
import com.app.idoneos.repository.CursoRepository;
import com.app.idoneos.service.temacronograma.TemaCronogramaService;

/** Pruebas de las reglas del cronograma de un programa, sobre los datos de la semilla. */
@SpringBootTest
@Transactional
class CronogramaServiceTest {

    @Autowired
    private TemaCronogramaService cronogramaService;

    @Autowired
    private CursoRepository cursoRepository;

    private Curso curso(String nombre) {
        return cursoRepository.findAll().stream().filter(c -> c.getNombre().equals(nombre)).findFirst().orElseThrow();
    }

    private Curso fiscal() {
        return curso("Planificación Fiscal Corporativa");
    }

    private Curso mercado() {
        return curso("Mercado de Capitales Argentino");
    }

    private String mensaje(org.junit.jupiter.api.function.Executable accion) {
        return assertThrows(IllegalArgumentException.class, accion).getMessage();
    }

    @Test
    @DisplayName("CU-29: reordena los temas y cambia sus días mientras sumen la duración del curso")
    void reordenaYCambiaLosDias() {
        Curso fiscal = fiscal();
        Programa programa = fiscal.getProgramaVigente();
        List<TemaCronograma> antes = programa.getCronogramaOrdenado();
        int primero = antes.get(0).getTema().getIdTema();
        int segundo = antes.get(1).getTema().getIdTema();
        int total = fiscal.getDuracionDias();

        cronogramaService.modificar(fiscal.getDocenteTitular(), programa.getIdPrograma(), List.of(segundo, primero),
                List.of("10", String.valueOf(total - 10)));

        List<TemaCronograma> despues = programa.getCronogramaOrdenado();
        assertEquals(segundo, despues.get(0).getTema().getIdTema());
        assertEquals(10, despues.get(0).getDuracionDias());
        assertEquals(total, programa.getDuracionTotalDias());
    }

    @Test
    @DisplayName("CU-29: los días deben sumar exactamente la duración del curso y ser enteros mayores a cero")
    void validaLosDias() {
        Curso fiscal = fiscal();
        Programa programa = fiscal.getProgramaVigente();
        List<Integer> temas = programa.getCronogramaOrdenado().stream().map(c -> c.getTema().getIdTema()).toList();

        assertTrue(mensaje(() -> cronogramaService.modificar(fiscal.getDocenteTitular(), programa.getIdPrograma(), temas, List.of("1", "1")))
                .contains("el curso dura"));
        assertThrows(IllegalArgumentException.class, () -> cronogramaService.modificar(fiscal.getDocenteTitular(),
                programa.getIdPrograma(), temas, List.of("30", "30")));
        assertThrows(IllegalArgumentException.class, () -> cronogramaService.modificar(fiscal.getDocenteTitular(),
                programa.getIdPrograma(), temas, List.of("x", "y")));
    }

    @Test
    @DisplayName("CU-29: un programa con cohortes publicadas no modifica su cronograma")
    void conCohortePublicada() {
        Curso mercado = mercado();
        Programa programa = mercado.getProgramaVigente();
        List<Integer> temas = programa.getCronogramaOrdenado().stream().map(c -> c.getTema().getIdTema()).toList();
        List<String> dias = programa.getCronogramaOrdenado().stream().map(c -> String.valueOf(c.getDuracionDias())).toList();

        assertTrue(mensaje(() -> cronogramaService.modificar(mercado.getDocenteTitular(), programa.getIdPrograma(), temas, dias))
                .contains("cohortes publicadas"));
    }

    @Test
    @DisplayName("CU-29: el docente debe participar en el curso del programa")
    void elDocenteDebeParticipar() {
        Curso fiscal = fiscal();
        Programa programa = fiscal.getProgramaVigente();
        List<Integer> temas = programa.getCronogramaOrdenado().stream().map(c -> c.getTema().getIdTema()).toList();

        assertTrue(mensaje(() -> cronogramaService.modificar(mercado().getDocenteTitular(), programa.getIdPrograma(), temas, List.of("21", "21")))
                .contains("No participás en este curso"));
    }
}
