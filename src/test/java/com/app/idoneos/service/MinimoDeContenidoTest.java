package com.app.idoneos.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.app.idoneos.model.TemaRecurso;
import com.app.idoneos.model.Programa;
import com.app.idoneos.model.TemaCronograma;
import com.app.idoneos.repository.ProgramaRepository;
import com.app.idoneos.service.recurso.RecursoService;
import com.app.idoneos.service.parametro.ParametroService;

/** Pruebas del mínimo de contenido de un programa con cohortes en curso. */
@SpringBootTest
@Transactional
class MinimoDeContenidoTest {

    @Autowired
    private ProgramaRepository programaRepository;

    @Autowired
    private RecursoService materialService;

    @Autowired
    private ParametroService parametroService;

    @Autowired
    private com.app.idoneos.service.programa.ProgramaService programaService;

    private Programa programa() {
        return programaRepository.findAll().stream()
                .filter(p -> !p.getBaja() && p.getCurso().getNombre().equals("Mercado de Capitales Argentino")
                        && p.tieneCohortesActivas())
                .findFirst().orElseThrow();
    }

    private List<TemaRecurso> materialesPublicados(Programa programa) {
        return programa.getCronogramaOrdenado().stream().map(TemaCronograma::getTema)
                .flatMap(u -> u.getMateriales().stream()).filter(m -> !m.getBaja() && !m.getOculto()).toList();
    }

    @Test
    @DisplayName("Un tema cuenta solo si tiene material publicado y al menos una autoevaluación visible")
    void unaTemaCuentaConMaterialYAutoevaluacion() {
        Programa programa = programa();
        int cuentan = programa.getCantidadTemasConMaterialPublicado();
        assertTrue(cuentan >= 2);

        // Sin los materiales de un tema, ese tema deja de contar. Sin sus temas, tampoco
        TemaCronograma primera = programa.getCronogramaOrdenado().get(0);
        Set<Integer> materialesDeLaPrimera = primera.getTema().getMateriales().stream().map(TemaRecurso::getIdTemaRecurso)
                .collect(java.util.stream.Collectors.toSet());
        assertEquals(cuentan - 1, programa.cantidadTemasConMaterialPublicadoSin(materialesDeLaPrimera, Set.of()));
        assertEquals(cuentan - 1, programa.cantidadTemasConMaterialPublicadoSin(Set.of(), Set.of(primera.getTema().getIdTema())));
    }

    @Test
    @DisplayName("No se puede dar de baja el material que deja al programa con cohortes en curso por debajo del mínimo")
    void noSePuedeDarDeBajaMaterialPorDebajoDelMinimo() {
        Programa programa = programa();
        int minimo = parametroService.obtenerEntero("programa.minimo_temas_con_material", 1);
        List<TemaRecurso> materiales = materialesPublicados(programa);

        // Dar de baja todos los materiales deja al programa sin temas con material
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> materialService.darDeBajaVarios(materiales.stream().map(TemaRecurso::getIdTemaRecurso).toList(), null));
        assertTrue(error.getMessage().startsWith("Error! No se puede dar de baja el recurso «"), error.getMessage());
        assertTrue(error.getMessage().contains("»: es el único material publicado del tema «"), error.getMessage());
        assertTrue(error.getMessage().endsWith("» y su curso tiene cohortes activas: el diseño del curso quedaría incompleto. Registrá otro antes de darlo de baja."),
                error.getMessage());
        assertEquals(materiales.size(), materialesPublicados(programa).size()); // todo o nada: no se dio de baja ninguno
    }

    @Test
    @DisplayName("Quitar temas del cronograma de un programa con cohortes en curso no puede dejarlo con menos del mínimo")
    void noSePuedenQuitarTemasPorDebajoDelMinimo() {
        Programa programa = programa();
        int minimo = parametroService.obtenerEntero("programa.minimo_temas_con_material", 1);
        Set<Integer> todas = programa.getCronogramaOrdenado().stream().map(c -> c.getTema().getIdTema())
                .collect(java.util.stream.Collectors.toSet());

        // Sin ningún tema queda por debajo. Sin ninguna quitada, no
        var bloqueo = programaService.verificarMinimoDeTemas(programa, todas);
        assertTrue(bloqueo.isPresent());
        assertEquals(0, bloqueo.get().quedaria());
        assertEquals(minimo, bloqueo.get().minimo());
        assertTrue(programaService.verificarMinimoDeTemas(programa, Set.of()).isEmpty());
    }

    @Test
    @DisplayName("Con el programa justo en el mínimo, no se puede dar de baja el material de un tema que cuenta")
    void justoEnElMinimoNoSePuedeQuitarOtraTema() {
        Programa programa = programa();
        int minimo = parametroService.obtenerEntero("programa.minimo_temas_con_material", 1);
        org.junit.jupiter.api.Assumptions.assumeTrue(programa.getCantidadTemasConMaterialPublicado() == minimo,
                "la semilla deja al programa justo en el mínimo");

        TemaCronograma cuenta = programa.getCronogramaOrdenado().stream()
                .filter(c -> !c.getTema().getAutoevaluacionesPublicadas().isEmpty()
                        && c.getTema().getMateriales().stream().anyMatch(m -> !m.getBaja() && !m.getOculto()))
                .findFirst().orElseThrow();
        List<Integer> ids = cuenta.getTema().getMateriales().stream().filter(m -> !m.getBaja() && !m.getOculto())
                .map(TemaRecurso::getIdTemaRecurso).toList();
        assertThrows(IllegalArgumentException.class, () -> materialService.darDeBajaVarios(ids, null));
    }

    @Test
    @DisplayName("El material de un tema que no cuenta para el mínimo tampoco se da de baja si deja incompleto el diseño del curso")
    void elMaterialDeUnTemaQueNoCuentaParaElMinimoNoSeDaDeBajaSiDejaIncompletoElDiseno() {
        Programa programa = programa();
        // Un tema con material pero sin autoevaluación visible no cuenta para el mínimo
        TemaCronograma noCuenta = programa.getCronogramaOrdenado().stream()
                .filter(c -> c.getTema().getMateriales().stream().anyMatch(m -> !m.getBaja() && !m.getOculto()))
                .reduce((primero, ultimo) -> ultimo).orElseThrow();
        noCuenta.getTema().getAutoevaluaciones().forEach(autoevaluacion -> autoevaluacion.setOculto(true));

        int antes = programa.getCantidadTemasConMaterialPublicado();
        List<Integer> ids = noCuenta.getTema().getMateriales().stream().filter(m -> !m.getBaja() && !m.getOculto())
                .map(TemaRecurso::getIdTemaRecurso).toList();
        // El mínimo no lo impide, pero el tema se quedaría sin su único material o clase grabada: el diseño quedaría incompleto
        String error = assertThrows(IllegalArgumentException.class, () -> materialService.darDeBajaVarios(ids, null)).getMessage();
        assertTrue(error.contains("el diseño del curso quedaría incompleto"), error);
        assertEquals(antes, programa.getCantidadTemasConMaterialPublicado());
    }
}
