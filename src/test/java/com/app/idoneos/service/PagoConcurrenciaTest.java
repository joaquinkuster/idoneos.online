package com.app.idoneos.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionTemplate;

import com.app.idoneos.model.Alumno;
import com.app.idoneos.model.Inscripcion;
import com.app.idoneos.repository.CohorteRepository;
import com.app.idoneos.service.inscripcion.InscripcionServiceImpl;
import com.app.idoneos.service.pago.PagoServiceImpl;
import com.app.idoneos.service.pasarelapago.PasarelaPagoService;
import com.app.idoneos.support.PasarelaPagoMockeada;

/**
 * Prueba de concurrencia del cupo: dos alumnos inician el pago a la vez por el último lugar de la cohorte y solo uno lo
 * consigue. No es transaccional (cada hilo necesita su propia transacción confirmada), por eso reinicia el contexto al terminar.
 */
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class PagoConcurrenciaTest {

    private static final String LUCIA = "lucia.fernandez@correo.com";
    private static final String VALENTINA = "valentina.ruiz@correo.com";

    @Autowired
    private PagoServiceImpl pagoService;

    @Autowired
    private InscripcionServiceImpl inscripcionService;

    @Autowired
    private CohorteRepository cohorteRepository;

    @Autowired
    private TransactionTemplate transaccion;

    @MockitoBean
    private PasarelaPagoService pasarelaMock;

    @Test
    @DisplayName("Cupo: dos pagos simultáneos por el último lugar de la cohorte, solo uno lo consigue")
    void dosPagosPorUnSoloLugar() throws Exception {
        new PasarelaPagoMockeada(pasarelaMock).dejarPendiente();

        // Lucía ya está pendiente en una cohorte; Valentina se inscribe a la misma y queda un solo lugar libre
        Integer[] datos = transaccion.execute(estado -> {
            Inscripcion lucia = inscripcionService.buscarConFiltros(null, LUCIA, com.app.idoneos.model.EstadoInscripcion.PENDIENTE_DE_PAGO).get(0);
            Alumno valentina = inscripcionService.buscarConFiltros(null, VALENTINA, null).get(0).getAlumno();
            Inscripcion otra = inscripcionService.inscribir(valentina, lucia.getCohorte().getIdCohorte(), true);
            lucia.getCohorte().setCupoMaximo(lucia.getCohorte().getCuposOcupados() + 1);
            cohorteRepository.save(lucia.getCohorte());
            return new Integer[] { lucia.getIdInscripcion(), lucia.getAlumno().getIdAlumno(), otra.getIdInscripcion(),
                    valentina.getIdAlumno() };
        });

        CountDownLatch salida = new CountDownLatch(1);
        ExecutorService hilos = Executors.newFixedThreadPool(2);
        List<Future<Boolean>> resultados = new ArrayList<>();
        resultados.add(hilos.submit(iniciar(salida, datos[0], datos[1])));
        resultados.add(hilos.submit(iniciar(salida, datos[2], datos[3])));
        salida.countDown(); // los dos arrancan a la vez

        int conseguidos = 0;
        for (Future<Boolean> resultado : resultados) {
            if (resultado.get()) {
                conseguidos++;
            }
        }
        hilos.shutdown();

        assertEquals(1, conseguidos, "solo uno de los dos pagos puede reservar el último lugar");
    }

    /** Inicia el pago de una inscripción en su propia transacción; devuelve si lo logró (false si no había lugar). */
    private Callable<Boolean> iniciar(CountDownLatch salida, Integer idInscripcion, Integer idAlumno) {
        return () -> {
            salida.await();
            try {
                transaccion.execute(estado -> {
                    Alumno alumno = inscripcionService.buscarConFiltros(null, null, null).stream()
                            .filter(i -> i.getAlumno().getIdAlumno() == idAlumno).findFirst().orElseThrow().getAlumno();
                    return pagoService.iniciar(idInscripcion, alumno);
                });
                return true;
            } catch (IllegalArgumentException sinLugar) {
                return false;
            }
        };
    }
}
