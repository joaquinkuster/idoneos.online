package com.app.idoneos.utilidades;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import com.app.idoneos.modelo.Categoria;
import com.app.idoneos.modelo.Descuento;
import com.app.idoneos.repositorio.CategoriaRepositorio;
import com.app.idoneos.repositorio.DescuentoRepositorio;

/**
 * Pruebas de la tercera validación (base de datos): se guarda directo por el repositorio, salteando el front y el
 * back, y se comprueba que la base rechaza el dato inválido indicando la restricción que se incumplió.
 * Cada rechazo va en su propio test porque la transacción queda inutilizable después del error.
 */
@SpringBootTest
@Transactional
class RestriccionBaseDatosTest {

    @Autowired
    private DescuentoRepositorio descuentoRepositorio;

    @Autowired
    private CategoriaRepositorio categoriaRepositorio;

    private Descuento descuento(String nombre, float porcentaje, int limite, int usada) {
        LocalDateTime hoy = LocalDateTime.now();
        return new Descuento(nombre, 0, porcentaje, hoy.minusDays(1), hoy.plusDays(30), limite, usada);
    }

    private void verificarRechazo(Runnable guardado, String restriccion) {
        DataIntegrityViolationException e = assertThrows(DataIntegrityViolationException.class, guardado::run);
        assertTrue(e.getMessage().toLowerCase().contains(restriccion),
                "Debía fallar por " + restriccion + " pero falló con: " + e.getMessage());
    }

    @Test
    @DisplayName("Un descuento válido se guarda sin problemas")
    void descuentoValidoSeGuarda() {
        assertNotNull(descuentoRepositorio.saveAndFlush(descuento("Descuento de prueba", 10, 5, 5)));
    }

    @Test
    @DisplayName("La base rechaza un porcentaje fuera de 1 a 100")
    void rechazaPorcentajeInvalido() {
        verificarRechazo(() -> descuentoRepositorio.saveAndFlush(descuento("Porcentaje inválido", 150, 5, 0)),
                "ck_descuento_porcentaje");
    }

    @Test
    @DisplayName("La base rechaza una cantidad usada mayor al límite del descuento")
    void rechazaUsoMayorAlLimite() {
        verificarRechazo(() -> descuentoRepositorio.saveAndFlush(descuento("Uso excedido", 10, 5, 6)),
                "ck_descuento_uso");
    }

    @Test
    @DisplayName("La base rechaza un nombre con solo espacios")
    void rechazaNombreVacio() {
        verificarRechazo(() -> categoriaRepositorio.saveAndFlush(new Categoria("   ")), "ck_categoria_nombre");
    }

    @Test
    @DisplayName("La base rechaza un nombre repetido (restricción UNIQUE)")
    void rechazaNombreRepetido() {
        categoriaRepositorio.saveAndFlush(new Categoria("Categoría de prueba"));
        assertThrows(DataIntegrityViolationException.class,
                () -> categoriaRepositorio.saveAndFlush(new Categoria("Categoría de prueba")));
    }
}
