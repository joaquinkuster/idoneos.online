package com.app.idoneos.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.app.idoneos.model.Curso;
import com.app.idoneos.service.curso.CursoServiceImpl;
import com.app.idoneos.service.estadistica.EstadisticaService.Barra;
import com.app.idoneos.service.estadistica.EstadisticaService.InformeAlumnos;
import com.app.idoneos.service.estadistica.EstadisticaService.InformeIngresos;
import com.app.idoneos.service.estadistica.EstadisticaService.Tablero;
import com.app.idoneos.service.estadistica.EstadisticaServiceImpl;

/** Pruebas de los informes y del tablero de estadísticas, sobre los datos de la semilla. */
@SpringBootTest
@Transactional
class EstadisticaServiceTest {

    private static final LocalDate DESDE = LocalDate.now().minusYears(1);
    private static final LocalDate HASTA = LocalDate.now();

    @Autowired
    private EstadisticaServiceImpl estadisticaService;

    @Autowired
    private CursoServiceImpl cursoService;

    private Curso curso() {
        return cursoService.buscarPorNombre("Macroeconomía de Coyuntura").orElseThrow();
    }

    @Test
    @DisplayName("CU-115: el tablero es coherente: 30 días, un pico real y un promedio que sale de la suma")
    void tableroCoherente() {
        Tablero tablero = estadisticaService.tablero();

        assertEquals(30, tablero.etiquetasDias().size());
        assertEquals(30, tablero.inscripcionesPorDia().length);
        assertEquals(Arrays.stream(tablero.inscripcionesPorDia()).max().orElse(0), tablero.picoInscripciones());
        assertEquals(Arrays.stream(tablero.inscripcionesPorDia()).sum() / 30.0, tablero.promedioDiario(), 0.0001);
        assertTrue(tablero.inscripcionesVigentes() >= tablero.alumnosActivos());
        assertTrue(tablero.cursosActivos() > 0);
        assertTrue(tablero.ingresosMes() >= 0 && tablero.ingresosMesAnterior() >= 0);
    }

    @Test
    @DisplayName("CU-115: el ranking de los cursos con más inscriptos tiene hasta cinco y va de mayor a menor")
    void rankingOrdenado() {
        List<Barra> ranking = estadisticaService.tablero().top5();

        assertTrue(!ranking.isEmpty() && ranking.size() <= 5);
        for (int i = 1; i < ranking.size(); i++) {
            assertTrue(ranking.get(i - 1).valor() >= ranking.get(i).valor());
        }
    }

    @Test
    @DisplayName("CU-115: la variación de los ingresos no se calcula si el mes anterior no tuvo ingresos")
    void variacionSinMesAnterior() {
        Tablero sinMesAnterior = new Tablero(1, 1, 1, 500, 0, List.of(), new int[0], 0, 0, null, List.of());
        Tablero conMesAnterior = new Tablero(1, 1, 1, 150, 100, List.of(), new int[0], 0, 0, null, List.of());

        assertNull(sinMesAnterior.variacionIngresos());
        assertEquals(50.0, conMesAnterior.variacionIngresos(), 0.0001);
    }

    @Test
    @DisplayName("CU-113: el informe de alumnos suma las inscripciones del curso y calcula el abandono")
    void informeDeAlumnos() {
        InformeAlumnos informe = estadisticaService.informeAlumnos(curso(), DESDE, HASTA);

        assertTrue(informe.bajas() <= informe.total());
        assertEquals(informe.total() - informe.bajas(), informe.retenidas());
        assertTrue(informe.porcentajeAbandono() >= 0 && informe.porcentajeAbandono() <= 100);
        assertNotNull(informe.evolucion());
        assertTrue(informe.porCurso().stream().anyMatch(Barra::seleccionada));
    }

    @Test
    @DisplayName("CU-114: el informe de ingresos nunca muestra un bruto menor al neto")
    void informeDeIngresos() {
        InformeIngresos informe = estadisticaService.informeIngresos(curso(), DESDE, HASTA);

        assertTrue(informe.bruto() >= informe.neto());
        assertEquals(informe.bruto() - informe.neto(), informe.descuentos(), 0.0001);
        assertTrue(informe.pagos() >= 0);
        assertNotNull(informe.evolucion());
    }

    @Test
    @DisplayName("CU-113 y CU-114: los informes exigen el curso y un rango de fechas válido")
    void validaLosDatosDelInforme() {
        Curso curso = curso();

        assertThrows(IllegalArgumentException.class, () -> estadisticaService.informeAlumnos(null, DESDE, HASTA));
        assertThrows(IllegalArgumentException.class, () -> estadisticaService.informeAlumnos(curso, null, HASTA));
        assertThrows(IllegalArgumentException.class, () -> estadisticaService.informeIngresos(curso, DESDE, null));
        assertThrows(IllegalArgumentException.class, () -> estadisticaService.informeIngresos(curso, HASTA, DESDE));
        assertThrows(IllegalArgumentException.class,
                () -> estadisticaService.informeAlumnos(curso, LocalDate.now().minusYears(6), HASTA));
    }
}
