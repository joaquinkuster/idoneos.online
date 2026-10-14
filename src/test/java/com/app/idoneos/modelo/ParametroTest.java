package com.app.idoneos.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de los parámetros sensibles: su valor no se muestra en pantalla.
 */
class ParametroTest {

    @Test
    @DisplayName("La contraseña de MODO es un parámetro sensible; el resto de los parámetros de MODO, no")
    void clavesSensibles() {
        assertTrue(Parametro.esClaveSensible(Parametro.MODO_CLAVE));
        assertFalse(Parametro.esClaveSensible(Parametro.MODO_USUARIO));
        assertFalse(Parametro.esClaveSensible(Parametro.CBU_INSTITUCION));
    }

    @Test
    @DisplayName("El valor de un parámetro sensible se muestra enmascarado; el de los demás, tal cual")
    void valorEnmascarado() {
        Parametro sensible = new Parametro(null, Parametro.MODO_CLAVE, "cifrado:abc");
        Parametro comun = new Parametro(null, Parametro.MODO_USUARIO, "mi-usuario");

        assertTrue(sensible.esSensible());
        assertEquals(Parametro.VALOR_ENMASCARADO, sensible.getValorEnmascarado());
        assertFalse(comun.esSensible());
        assertEquals("mi-usuario", comun.getValorEnmascarado());
    }
}
