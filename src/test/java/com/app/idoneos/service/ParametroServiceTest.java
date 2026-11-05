package com.app.idoneos.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.app.idoneos.model.Administrador;
import com.app.idoneos.model.Parametro;
import com.app.idoneos.repository.AdministradorRepository;
import com.app.idoneos.repository.ParametroRepository;
import com.app.idoneos.service.parametro.ParametroServiceImpl;

/**
 * Pruebas de los parámetros del sistema: lectura, registro y cifrado de los parámetros sensibles (la contraseña de
 * MODO), sobre los datos de la semilla. Cada prueba se ejecuta en una transacción que se revierte al finalizar.
 */
@SpringBootTest
@Transactional
class ParametroServiceTest {

    @Autowired
    private ParametroServiceImpl parametroService;

    @Autowired
    private ParametroRepository parametroRepository;

    @Autowired
    private AdministradorRepository administradorRepository;

    private Administrador administrador() {
        return administradorRepository.findAll().get(0);
    }

    @Test
    @DisplayName("La semilla carga los parámetros de MODO y su contraseña queda cifrada en la base")
    void laSemillaCargaLosParametrosDeModo() {
        for (String clave : new String[] { Parametro.MODO_USUARIO, Parametro.MODO_CLAVE, Parametro.MODO_URL_API,
                Parametro.MODO_URL_CHECKOUT, Parametro.MODO_NOMBRE_COMERCIO, Parametro.MODO_PROCESSOR_CODE,
                Parametro.MODO_CC_CODE, Parametro.MODO_MONTO_MINIMO }) {
            assertTrue(parametroService.obtenerTexto(clave).filter(valor -> !valor.isBlank()).isPresent(), clave);
        }
        assertTrue(parametroService.obtenerTexto(Parametro.MODO_URL_PUBLICA).isPresent()); // existe, vacío
        String guardado = parametroRepository.findByClave(Parametro.MODO_CLAVE).orElseThrow().getValor();
        assertTrue(guardado.startsWith("cifrado:"));
        assertNotEquals(guardado, parametroService.obtenerTexto(Parametro.MODO_CLAVE).orElseThrow());
    }

    @Test
    @DisplayName("Un parámetro sensible se guarda cifrado y se lee descifrado")
    void guardaCifradoYLeeDescifrado() {
        parametroService.registrarTexto(administrador(), Parametro.MODO_CLAVE, "mi-clave-secreta");

        String guardado = parametroRepository.findByClave(Parametro.MODO_CLAVE).orElseThrow().getValor();
        assertTrue(guardado.startsWith("cifrado:"));
        assertFalse(guardado.contains("mi-clave-secreta"));
        assertEquals("mi-clave-secreta", parametroService.obtenerTexto(Parametro.MODO_CLAVE).orElseThrow());
    }

    @Test
    @DisplayName("El cifrado es aleatorio: la misma contraseña guardada dos veces no da el mismo texto")
    void elCifradoEsAleatorio() {
        parametroService.registrarTexto(administrador(), Parametro.MODO_CLAVE, "igual");
        String primero = parametroRepository.findByClave(Parametro.MODO_CLAVE).orElseThrow().getValor();
        parametroService.registrarTexto(administrador(), Parametro.MODO_CLAVE, "igual");
        String segundo = parametroRepository.findByClave(Parametro.MODO_CLAVE).orElseThrow().getValor();

        assertNotEquals(primero, segundo);
        assertEquals("igual", parametroService.obtenerTexto(Parametro.MODO_CLAVE).orElseThrow());
    }

    @Test
    @DisplayName("Los parámetros comunes se guardan tal cual y registrar uno existente lo reemplaza")
    void parametrosComunes() {
        parametroService.registrarTexto(administrador(), Parametro.MODO_USUARIO, "cuenta-real");
        assertEquals("cuenta-real", parametroRepository.findByClave(Parametro.MODO_USUARIO).orElseThrow().getValor());
        long cantidad = parametroRepository.count();

        parametroService.registrarTexto(administrador(), Parametro.MODO_USUARIO, "otra-cuenta");

        assertEquals("otra-cuenta", parametroService.obtenerTexto(Parametro.MODO_USUARIO).orElseThrow());
        assertEquals(cantidad, parametroRepository.count()); // se reemplazó, no se duplicó
        assertTrue(parametroRepository.findByClave(Parametro.MODO_USUARIO).orElseThrow().getUltimaModificacion() != null);
    }

    @Test
    @DisplayName("Un parámetro sensible cargado a mano sin cifrar se lee tal cual")
    void parametroSensibleSinCifrar() {
        Parametro parametro = parametroRepository.findByClave(Parametro.MODO_CLAVE).orElseThrow();
        parametro.setValor("clave-en-texto-plano");

        assertEquals("clave-en-texto-plano", parametroService.obtenerTexto(Parametro.MODO_CLAVE).orElseThrow());
    }

    @Test
    @DisplayName("Si el valor cifrado no se puede descifrar (cambió la clave de cifrado), se informa")
    void valorCifradoIlegible() {
        Parametro parametro = parametroRepository.findByClave(Parametro.MODO_CLAVE).orElseThrow();
        parametro.setValor("cifrado:abcdef0123456789");

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> parametroService.obtenerTexto(Parametro.MODO_CLAVE));
        assertTrue(error.getMessage().contains("clave de cifrado"));
    }

    @Test
    @DisplayName("Un parámetro inexistente no tiene texto; la clave y el valor son obligatorios al registrar")
    void inexistenteYValidaciones() {
        assertTrue(parametroService.obtenerTexto("no.existe").isEmpty());
        assertThrows(IllegalArgumentException.class, () -> parametroService.registrarTexto(administrador(), " ", "x"));
        assertThrows(IllegalArgumentException.class,
                () -> parametroService.registrarTexto(administrador(), Parametro.MODO_USUARIO, null));
    }

    @Test
    @DisplayName("Los parámetros numéricos con decimales aceptan punto o coma y, si no son un número, usan el valor por defecto")
    void parametrosDecimales() {
        assertEquals(30f, parametroService.obtenerDecimal(Parametro.MODO_MONTO_MINIMO, 99f));
        parametroService.registrarTexto(administrador(), Parametro.MODO_MONTO_MINIMO, "0,01");
        assertEquals(0.01f, parametroService.obtenerDecimal(Parametro.MODO_MONTO_MINIMO, 99f));
        parametroService.registrarTexto(administrador(), Parametro.MODO_MONTO_MINIMO, "abc");
        assertEquals(99f, parametroService.obtenerDecimal(Parametro.MODO_MONTO_MINIMO, 99f));
        assertEquals(7f, parametroService.obtenerDecimal("no.existe", 7f));
    }
}
