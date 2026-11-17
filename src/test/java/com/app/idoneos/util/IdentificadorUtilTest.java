package com.app.idoneos.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.app.idoneos.util.IdentificadorUtil.Tipo;

@DisplayName("Utilidad de los identificadores de un material bibliográfico")
class IdentificadorUtilTest {

    @Test
    @DisplayName("Reconoce un ISBN de diez o trece cifras, con o sin guiones y espacios")
    void reconoceElIsbn() {
        assertEquals(Tipo.ISBN, IdentificadorUtil.tipoDe("978-987-00-0000-7"));
        assertEquals(Tipo.ISBN, IdentificadorUtil.tipoDe("9789870000007"));
        assertEquals(Tipo.ISBN, IdentificadorUtil.tipoDe("978 987 00 0000 7"));
        assertEquals(Tipo.ISBN, IdentificadorUtil.tipoDe("0-306-40615-2"));
        assertEquals(Tipo.ISBN, IdentificadorUtil.tipoDe("080442957x"));
    }

    @Test
    @DisplayName("Reconoce un ISSN de cuatro cifras, un guion y cuatro más")
    void reconoceElIssn() {
        assertEquals(Tipo.ISSN, IdentificadorUtil.tipoDe("0378-5955"));
        assertEquals(Tipo.ISSN, IdentificadorUtil.tipoDe("2049-363x"));
    }

    @Test
    @DisplayName("Reconoce un DOI que empieza con 10. y lleva una barra")
    void reconoceElDoi() {
        assertEquals(Tipo.DOI, IdentificadorUtil.tipoDe("10.1000/xyz123"));
        assertEquals(Tipo.DOI, IdentificadorUtil.tipoDe("10.1016/j.jss.2020.110000"));
    }

    @Test
    @DisplayName("No reconoce lo que no es un ISBN, un ISSN ni un DOI")
    void rechazaLoDemas() {
        assertNull(IdentificadorUtil.tipoDe(null));
        assertNull(IdentificadorUtil.tipoDe(""));
        assertNull(IdentificadorUtil.tipoDe("12345"));
        assertNull(IdentificadorUtil.tipoDe("hola"));
        assertNull(IdentificadorUtil.tipoDe("10.12/corto"));
        assertNull(IdentificadorUtil.tipoDe("978-987-00-0000"));
    }

    @Test
    @DisplayName("El ISSN lo comparten los artículos de una revista, y el ISBN y el DOI no se repiten")
    void soloElIssnSePuedeRepetir() {
        assertFalse(IdentificadorUtil.debeSerUnico(Tipo.ISSN));
        assertTrue(IdentificadorUtil.debeSerUnico(Tipo.ISBN));
        assertTrue(IdentificadorUtil.debeSerUnico(Tipo.DOI));
    }

    @Test
    @DisplayName("Normaliza quitando guiones y espacios y pasando a mayúscula")
    void normaliza() {
        assertEquals("9789870000007", IdentificadorUtil.normalizar(" 978-987-00-0000-7 "));
        assertEquals("080442957X", IdentificadorUtil.normalizar("0-8044-2957-x"));
    }
}
