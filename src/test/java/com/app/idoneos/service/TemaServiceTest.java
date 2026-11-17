package com.app.idoneos.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.app.idoneos.model.Curso;
import com.app.idoneos.model.Tema;
import com.app.idoneos.repository.CursoRepository;
import com.app.idoneos.service.tema.TemaService;

/** Pruebas del alta, la modificación y la baja de temas, sobre los datos de la semilla. */
@SpringBootTest
@Transactional
class TemaServiceTest {

    @Autowired
    private TemaService temaService;

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
    @DisplayName("CU-23: busca los temas de un curso y los de un programa por título")
    void busca() {
        Curso fiscal = fiscal();

        assertEquals(2, temaService.buscarPorCurso(fiscal).size());
        assertEquals(1, temaService.buscarConFiltros(fiscal.getProgramaVigente(), "IVA").size());
    }

    @Test
    @DisplayName("CU-24: registra un tema del curso, sin asignarlo a ningún programa")
    void registra() {
        Curso fiscal = fiscal();

        Tema tema = temaService.registrar(fiscal.getDocenteTitular(), fiscal.getIdCurso(), "Retenciones", "Régimen de retenciones",
                "Contenido de retenciones");

        assertEquals("Retenciones", tema.getTitulo());
        assertFalse(tema.esInactivo());
        assertEquals(3, temaService.buscarPorCurso(fiscal).size());
    }

    @Test
    @DisplayName("CU-24: el título es obligatorio, no se repite y el docente debe participar en el curso")
    void validaAlRegistrar() {
        Curso fiscal = fiscal();

        assertThrows(IllegalArgumentException.class,
                () -> temaService.registrar(fiscal.getDocenteTitular(), fiscal.getIdCurso(), " ", null, "Contenido"));
        assertThrows(IllegalArgumentException.class, () -> temaService.registrar(fiscal.getDocenteTitular(), fiscal.getIdCurso(),
                "IVA y regímenes especiales", null, "Contenido"));
        assertTrue(mensaje(() -> temaService.registrar(mercado().getDocenteTitular(), fiscal.getIdCurso(), "Ajeno", null, "Contenido"))
                .contains("No participás en este curso"));
    }

    @Test
    @DisplayName("CU-25: modifica un tema de un curso sin cohortes publicadas y rechaza el de un curso con cohortes publicadas")
    void modifica() {
        Curso fiscal = fiscal();
        Curso mercado = mercado();
        Tema deFiscal = temaService.buscarPorCurso(fiscal).get(0);
        Tema deMercado = temaService.buscarPorCurso(mercado).get(0);

        Tema modificado = temaService.modificar(deFiscal.getIdTema(), fiscal.getDocenteTitular(), "Título nuevo", null, "Contenido nuevo");

        assertEquals("Título nuevo", modificado.getTitulo());
        assertTrue(mensaje(() -> temaService.modificar(deMercado.getIdTema(), mercado.getDocenteTitular(), "Otro", null, "Contenido"))
                .contains("cohortes publicadas"));
    }

    @Test
    @DisplayName("CU-26: da de baja un tema que no está en ningún cronograma y rechaza uno que ya lo integra")
    void daDeBaja() {
        Curso fiscal = fiscal();
        Tema nuevo = temaService.registrar(fiscal.getDocenteTitular(), fiscal.getIdCurso(), "Para dar de baja", null, "Contenido");
        Tema delCronograma = fiscal.getProgramaVigente().getCronogramaOrdenado().get(0).getTema();

        temaService.darDeBajaPorCurso(nuevo.getIdTema(), fiscal.getDocenteTitular());

        assertTrue(nuevo.esInactivo());
        assertThrows(IllegalArgumentException.class, () -> temaService.darDeBajaPorCurso(delCronograma.getIdTema(), fiscal.getDocenteTitular()));
        assertFalse(delCronograma.esInactivo());
    }
}
