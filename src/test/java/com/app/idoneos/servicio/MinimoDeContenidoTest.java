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

import com.app.idoneos.modelo.Material;
import com.app.idoneos.modelo.Programa;
import com.app.idoneos.modelo.UnidadCronograma;
import com.app.idoneos.repositorio.ProgramaRepositorio;
import com.app.idoneos.servicio.Material.MaterialServicio;
import com.app.idoneos.servicio.Parametro.ParametroServicio;

/**
 * Pruebas del mínimo de contenido de un programa con cohortes en curso: una unidad cuenta si tiene material publicado y al
 * menos una autoevaluación visible, y el programa debe conservar el mínimo (parámetro de la semilla: 2) mientras tenga
 * cohortes en curso, aunque no tengan inscripciones. Se usa el programa de «Mercado de Capitales Argentino», con tres
 * unidades con material y autoevaluación.
 */
@SpringBootTest
@Transactional
class MinimoDeContenidoTest {

    @Autowired
    private ProgramaRepositorio programaRepositorio;

    @Autowired
    private MaterialServicio materialServicio;

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

    private List<Material> materialesPublicados(Programa programa) {
        return programa.getCronogramaOrdenado().stream().map(UnidadCronograma::getUnidad)
                .flatMap(u -> u.getMateriales().stream()).filter(m -> !m.getBaja() && !m.getOculto()).toList();
    }

    @Test
    @DisplayName("Una unidad cuenta solo si tiene material publicado y al menos una autoevaluación visible")
    void unaUnidadCuentaConMaterialYAutoevaluacion() {
        Programa programa = programa();
        int cuentan = programa.getCantidadUnidadesConMaterialPublicado();
        assertTrue(cuentan >= 2);

        // Sin los materiales de una unidad, esa unidad deja de contar; sin sus unidades, tampoco
        UnidadCronograma primera = programa.getCronogramaOrdenado().get(0);
        Set<Integer> materialesDeLaPrimera = primera.getUnidad().getMateriales().stream().map(Material::getIdMaterial)
                .collect(java.util.stream.Collectors.toSet());
        assertEquals(cuentan - 1, programa.cantidadUnidadesConMaterialPublicadoSin(materialesDeLaPrimera, Set.of()));
        assertEquals(cuentan - 1, programa.cantidadUnidadesConMaterialPublicadoSin(Set.of(), Set.of(primera.getUnidad().getIdUnidad())));
    }

    @Test
    @DisplayName("No se puede dar de baja el material que deja al programa con cohortes en curso por debajo del mínimo")
    void noSePuedeDarDeBajaMaterialPorDebajoDelMinimo() {
        Programa programa = programa();
        int minimo = parametroServicio.obtenerEntero("programa.minimo_unidades_con_material", 1);
        List<Material> materiales = materialesPublicados(programa);

        // Dar de baja todos los materiales deja al programa sin unidades con material
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> materialServicio.darDeBajaVarios(materiales.stream().map(Material::getIdMaterial).toList(), null));
        assertTrue(error.getMessage().startsWith("Error! No se puede dar de baja el material «"), error.getMessage());
        assertTrue(error.getMessage().contains("»: es el único visible de la unidad «"), error.getMessage());
        assertTrue(error.getMessage().endsWith("» y su programa tiene cohortes en curso. Registrá otro material antes."),
                error.getMessage());
        assertEquals(materiales.size(), materialesPublicados(programa).size()); // todo o nada: no se dio de baja ninguno
    }

    @Test
    @DisplayName("Quitar unidades del cronograma de un programa con cohortes en curso no puede dejarlo con menos del mínimo")
    void noSePuedenQuitarUnidadesPorDebajoDelMinimo() {
        Programa programa = programa();
        int minimo = parametroServicio.obtenerEntero("programa.minimo_unidades_con_material", 1);
        Set<Integer> todas = programa.getCronogramaOrdenado().stream().map(c -> c.getUnidad().getIdUnidad())
                .collect(java.util.stream.Collectors.toSet());

        // Sin ninguna unidad queda por debajo; sin ninguna quitada, no
        var bloqueo = validador.verificarUnidades(programa, todas);
        assertTrue(bloqueo.isPresent());
        assertEquals(0, bloqueo.get().quedaria());
        assertEquals(minimo, bloqueo.get().minimo());
        assertTrue(validador.verificarUnidades(programa, Set.of()).isEmpty());
    }

    @Test
    @DisplayName("Con el programa justo en el mínimo, no se puede dar de baja el material de una unidad que cuenta")
    void justoEnElMinimoNoSePuedeQuitarOtraUnidad() {
        Programa programa = programa();
        int minimo = parametroServicio.obtenerEntero("programa.minimo_unidades_con_material", 1);
        org.junit.jupiter.api.Assumptions.assumeTrue(programa.getCantidadUnidadesConMaterialPublicado() == minimo,
                "la semilla deja al programa justo en el mínimo");

        UnidadCronograma cuenta = programa.getCronogramaOrdenado().stream()
                .filter(c -> !c.getUnidad().getAutoevaluacionesPublicadas().isEmpty()
                        && c.getUnidad().getMateriales().stream().anyMatch(m -> !m.getBaja() && !m.getOculto()))
                .findFirst().orElseThrow();
        List<Integer> ids = cuenta.getUnidad().getMateriales().stream().filter(m -> !m.getBaja() && !m.getOculto())
                .map(Material::getIdMaterial).toList();
        assertThrows(IllegalArgumentException.class, () -> materialServicio.darDeBajaVarios(ids, null));
    }

    @Test
    @DisplayName("El material de una unidad que no cuenta para el mínimo se puede dar de baja")
    void elMaterialDeUnaUnidadQueNoCuentaSePuedeDarDeBaja() {
        Programa programa = programa();
        UnidadCronograma noCuenta = programa.getCronogramaOrdenado().stream()
                .filter(c -> c.getUnidad().getAutoevaluacionesPublicadas().isEmpty()
                        && c.getUnidad().getMateriales().stream().anyMatch(m -> !m.getBaja() && !m.getOculto()))
                .findFirst().orElse(null);
        org.junit.jupiter.api.Assumptions.assumeTrue(noCuenta != null, "la semilla tiene una unidad con material y sin autoevaluación");

        int antes = programa.getCantidadUnidadesConMaterialPublicado();
        List<Integer> ids = noCuenta.getUnidad().getMateriales().stream().filter(m -> !m.getBaja() && !m.getOculto())
                .map(Material::getIdMaterial).toList();
        materialServicio.darDeBajaVarios(ids, null);
        assertEquals(antes, programa.getCantidadUnidadesConMaterialPublicado());
    }
}
