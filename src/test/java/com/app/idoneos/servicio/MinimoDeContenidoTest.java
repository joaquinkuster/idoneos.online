package com.app.idoneos.servicio;

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

import com.app.idoneos.modelo.TemaRecurso;
import com.app.idoneos.modelo.Programa;
import com.app.idoneos.modelo.TemaCronograma;
import com.app.idoneos.repositorio.ProgramaRepositorio;
import com.app.idoneos.servicio.Recurso.RecursoServicio;
import com.app.idoneos.servicio.Parametro.ParametroServicio;

/**
 * Pruebas del mínimo de contenido de un programa con cohortes en curso: un tema cuenta si tiene material publicado y al
 * menos una autoevaluación visible, y el programa debe conservar el mínimo (parámetro de la semilla: 2) mientras tenga
 * cohortes en curso, aunque no tengan inscripciones. Se usa el programa de «Mercado de Capitales Argentino», con tres
 * temas con material y autoevaluación.
 */
@SpringBootTest
@Transactional
class MinimoDeContenidoTest {

    @Autowired
    private ProgramaRepositorio programaRepositorio;

    @Autowired
    private RecursoServicio materialServicio;

    @Autowired
    private ParametroServicio parametroServicio;

    @Autowired
    private com.app.idoneos.servicio.Programa.MinimoDeContenidoValidador validador;

    private Programa programa() {
        return programaRepositorio.findAll().stream()
                .filter(p -> !p.getBaja() && p.getCurso().getNombre().equals("Mercado de Capitales Argentino")
                        && p.tieneCohortesEnCurso())
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

        // Sin los materiales de un tema, ese tema deja de contar; sin sus temas, tampoco
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
        int minimo = parametroServicio.obtenerEntero("programa.minimo_temas_con_material", 1);
        List<TemaRecurso> materiales = materialesPublicados(programa);

        // Dar de baja todos los materiales deja al programa sin temas con material
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> materialServicio.darDeBajaVarios(materiales.stream().map(TemaRecurso::getIdTemaRecurso).toList(), null));
        assertTrue(error.getMessage().startsWith("Error! No se puede dar de baja el material «"), error.getMessage());
        assertTrue(error.getMessage().contains("»: es el único visible del tema «"), error.getMessage());
        assertTrue(error.getMessage().endsWith("» y su programa tiene cohortes en curso. Registrá otro material antes."),
                error.getMessage());
        assertEquals(materiales.size(), materialesPublicados(programa).size()); // todo o nada: no se dio de baja ninguno
    }

    @Test
    @DisplayName("Quitar temas del cronograma de un programa con cohortes en curso no puede dejarlo con menos del mínimo")
    void noSePuedenQuitarTemasPorDebajoDelMinimo() {
        Programa programa = programa();
        int minimo = parametroServicio.obtenerEntero("programa.minimo_temas_con_material", 1);
        Set<Integer> todas = programa.getCronogramaOrdenado().stream().map(c -> c.getTema().getIdTema())
                .collect(java.util.stream.Collectors.toSet());

        // Sin ningún tema queda por debajo; sin ninguna quitada, no
        var bloqueo = validador.verificarTemas(programa, todas);
        assertTrue(bloqueo.isPresent());
        assertEquals(0, bloqueo.get().quedaria());
        assertEquals(minimo, bloqueo.get().minimo());
        assertTrue(validador.verificarTemas(programa, Set.of()).isEmpty());
    }

    @Test
    @DisplayName("Con el programa justo en el mínimo, no se puede dar de baja el material de un tema que cuenta")
    void justoEnElMinimoNoSePuedeQuitarOtraTema() {
        Programa programa = programa();
        int minimo = parametroServicio.obtenerEntero("programa.minimo_temas_con_material", 1);
        org.junit.jupiter.api.Assumptions.assumeTrue(programa.getCantidadTemasConMaterialPublicado() == minimo,
                "la semilla deja al programa justo en el mínimo");

        TemaCronograma cuenta = programa.getCronogramaOrdenado().stream()
                .filter(c -> !c.getTema().getAutoevaluacionesPublicadas().isEmpty()
                        && c.getTema().getMateriales().stream().anyMatch(m -> !m.getBaja() && !m.getOculto()))
                .findFirst().orElseThrow();
        List<Integer> ids = cuenta.getTema().getMateriales().stream().filter(m -> !m.getBaja() && !m.getOculto())
                .map(TemaRecurso::getIdTemaRecurso).toList();
        assertThrows(IllegalArgumentException.class, () -> materialServicio.darDeBajaVarios(ids, null));
    }

    @Test
    @DisplayName("El material de un tema que no cuenta para el mínimo se puede dar de baja")
    void elMaterialDeUnaTemaQueNoCuentaSePuedeDarDeBaja() {
        Programa programa = programa();
        TemaCronograma noCuenta = programa.getCronogramaOrdenado().stream()
                .filter(c -> c.getTema().getAutoevaluacionesPublicadas().isEmpty()
                        && c.getTema().getMateriales().stream().anyMatch(m -> !m.getBaja() && !m.getOculto()))
                .findFirst().orElse(null);
        org.junit.jupiter.api.Assumptions.assumeTrue(noCuenta != null, "la semilla tiene un tema con material y sin autoevaluación");

        int antes = programa.getCantidadTemasConMaterialPublicado();
        List<Integer> ids = noCuenta.getTema().getMateriales().stream().filter(m -> !m.getBaja() && !m.getOculto())
                .map(TemaRecurso::getIdTemaRecurso).toList();
        materialServicio.darDeBajaVarios(ids, null);
        assertEquals(antes, programa.getCantidadTemasConMaterialPublicado());
    }
}
