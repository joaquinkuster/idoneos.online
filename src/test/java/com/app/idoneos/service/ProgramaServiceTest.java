package com.app.idoneos.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import com.app.idoneos.repository.CursoRepository;
import com.app.idoneos.service.programa.ProgramaService;

/** Pruebas del alta, la modificación y la baja de programas, sobre los datos de la semilla. */
@SpringBootTest
@Transactional
class ProgramaServiceTest {

    @Autowired
    private ProgramaService programaService;

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
    @DisplayName("CU-19: busca los programas de un curso por nombre")
    void busca() {
        assertFalse(programaService.buscarPorCurso(mercado()).isEmpty());
        assertFalse(programaService.buscarConFiltros(mercado(), "2026", null).isEmpty());
        assertTrue(programaService.buscarConFiltros(mercado(), "inexistente", null).isEmpty());
    }

    @Test
    @DisplayName("CU-20: registra un programa copiando el cronograma del anterior y guarda cuál fue su origen")
    void registraCopiandoElCronograma() {
        Curso fiscal = fiscal();
        Programa origen = fiscal.getProgramaVigente();

        Programa nuevo = programaService.registrar(fiscal.getDocenteTitular(), fiscal.getIdCurso(), origen.getIdPrograma(),
                "Programa 2027", null, null, null);

        assertEquals("Programa 2027", nuevo.getNombre());
        assertEquals(origen.getIdPrograma(), nuevo.getProgramaAnterior().getIdPrograma());
        assertEquals(origen.getCantidadTemas(), nuevo.getCantidadTemas());
    }

    @Test
    @DisplayName("CU-20: no registra un programa sin cronograma ni a un docente que no participa en el curso")
    void validaAlRegistrar() {
        Curso fiscal = fiscal();

        assertTrue(mensaje(() -> programaService.registrar(fiscal.getDocenteTitular(), fiscal.getIdCurso(), null, "Vacío", null, null, null))
                .contains("no tiene cronograma"));
        assertTrue(mensaje(() -> programaService.registrar(mercado().getDocenteTitular(), fiscal.getIdCurso(), null, "Ajeno", null, null, null))
                .contains("No participás en este curso"));
    }

    @Test
    @DisplayName("CU-21: modifica el nombre y la descripción de un programa sin cohortes publicadas")
    void modifica() {
        Curso fiscal = fiscal();
        Programa programa = fiscal.getProgramaVigente();

        Programa modificado = programaService.modificar(programa.getIdPrograma(), fiscal.getDocenteTitular(), "Programa renovado",
                "Cambios de la nueva edición");

        assertEquals("Programa renovado", modificado.getNombre());
        assertEquals("Cambios de la nueva edición", modificado.getDescripcion());
    }

    @Test
    @DisplayName("CU-21: el nombre es obligatorio y un programa con cohortes publicadas no se modifica")
    void validaAlModificar() {
        Curso fiscal = fiscal();
        Curso mercado = mercado();

        assertThrows(IllegalArgumentException.class,
                () -> programaService.modificar(fiscal.getProgramaVigente().getIdPrograma(), fiscal.getDocenteTitular(), " ", null));
        assertTrue(mensaje(() -> programaService.modificar(mercado.getProgramaVigente().getIdPrograma(), mercado.getDocenteTitular(),
                "Otro nombre", null)).contains("cohortes publicadas"));
    }

    @Test
    @DisplayName("CU-22: da de baja un programa sin cohortes y rechaza uno con cohortes activas")
    void daDeBaja() {
        Curso fiscal = fiscal();
        Curso mercado = mercado();

        programaService.darDeBaja(fiscal.getProgramaVigente().getIdPrograma(), fiscal.getDocenteTitular());
        assertTrue(mensaje(() -> programaService.darDeBaja(mercado.getProgramaVigente().getIdPrograma(), mercado.getDocenteTitular()))
                .contains("cohorte"));
    }

    @Test
    @DisplayName("CU-22: la baja de varios programas es todo o nada")
    void daDeBajaVarios() {
        Curso fiscal = fiscal();
        Programa deFiscal = fiscal.getProgramaVigente();
        Programa deMercado = mercado().getProgramaVigente();

        assertThrows(IllegalArgumentException.class, () -> programaService.darDeBajaVarios(
                List.of(deFiscal.getIdPrograma(), deMercado.getIdPrograma()), fiscal.getDocenteTitular()));
        assertFalse(deFiscal.esInactivo());
    }
}
