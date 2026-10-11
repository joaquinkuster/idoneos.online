package com.app.idoneos.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias de las reglas que calculan {@link Material} y {@link TipoMaterial}.
 */
class MaterialTest {

    private Material material(String tipo, String ruta) {
        Material material = new Material(null, new TipoMaterial(tipo), null, "Material");
        material.setRutaArchivo(ruta);
        return material;
    }

    @Test
    @DisplayName("Un material está publicado si no está dado de baja ni oculto")
    void publicado() {
        Material material = material(TipoMaterial.GRABACION, null);
        assertTrue(material.estaPublicado());

        material.setOculto(true);
        assertFalse(material.estaPublicado());

        material.setOculto(false);
        material.marcarInactivo();
        assertFalse(material.estaPublicado());
    }

    @Test
    @DisplayName("Distingue un archivo del servidor de un enlace externo")
    void archivoOEnlace() {
        assertTrue(material(TipoMaterial.GRABACION, "curso1/unidad2/material3.mp4").tieneArchivo());
        assertFalse(material(TipoMaterial.GRABACION, "curso1/unidad2/material3.mp4").esEnlaceExterno());
        assertTrue(material(TipoMaterial.BIBLIOGRAFIA, "https://sitio.com/libro.pdf").esEnlaceExterno());
        assertFalse(material(TipoMaterial.BIBLIOGRAFIA, "https://sitio.com/libro.pdf").tieneArchivo());
        assertFalse(material(TipoMaterial.BIBLIOGRAFIA, null).tieneArchivo());
    }

    @Test
    @DisplayName("Solo grabación, bibliografía y presentación se suben manualmente; el resumen lo genera la IA")
    void tiposQueSeSubenAMano() {
        assertTrue(new TipoMaterial(TipoMaterial.GRABACION).permiteSubidaManual());
        assertTrue(new TipoMaterial(TipoMaterial.BIBLIOGRAFIA).permiteSubidaManual());
        assertTrue(new TipoMaterial(TipoMaterial.PRESENTACION).permiteSubidaManual());
        assertFalse(new TipoMaterial(TipoMaterial.RESUMEN).permiteSubidaManual());
    }

    @Test
    @DisplayName("Cada tipo define sus formatos, su tamaño máximo y si admite enlace externo")
    void formatosYLimites() {
        TipoMaterial grabacion = new TipoMaterial(TipoMaterial.GRABACION);
        TipoMaterial presentacion = new TipoMaterial(TipoMaterial.PRESENTACION);
        TipoMaterial bibliografia = new TipoMaterial(TipoMaterial.BIBLIOGRAFIA);

        assertEquals(List.of("mp4", "webm"), grabacion.getExtensionesPermitidas());
        assertEquals(List.of("pdf"), presentacion.getExtensionesPermitidas());
        assertEquals(List.of("pdf"), bibliografia.getExtensionesPermitidas());
        assertEquals(500L * 1024 * 1024, grabacion.getTamanioMaximo());
        assertEquals(50L * 1024 * 1024, presentacion.getTamanioMaximo());
        assertTrue(bibliografia.permiteEnlaceExterno());
        assertFalse(grabacion.permiteEnlaceExterno());
    }

    @Test
    @DisplayName("La descripción de subida de cada tipo indica los formatos, el tamaño máximo y si admite enlace")
    void descripcionDeSubida() {
        String grabacion = new TipoMaterial(TipoMaterial.GRABACION).getDescripcionDeSubida();
        String bibliografia = new TipoMaterial(TipoMaterial.BIBLIOGRAFIA).getDescripcionDeSubida();

        assertTrue(grabacion.contains("MP4, WEBM") && grabacion.contains("Máximo 500 MB"));
        assertFalse(grabacion.contains("enlace"));
        assertTrue(bibliografia.contains("Formatos: PDF.") && bibliografia.contains("Máximo 50 MB"));
        assertTrue(bibliografia.contains("enlace externo"));
    }

    @Test
    @DisplayName("El formato de vista depende del archivo: video, pdf, enlace o vacío (los formatos que ya no se admiten)")
    void formatoDeVista() {
        assertEquals("video", material(TipoMaterial.GRABACION, "curso1/unidad2/material3.mp4").getFormatoDeVista());
        assertEquals("video", material(TipoMaterial.GRABACION, "curso1/unidad2/material3.webm").getFormatoDeVista());
        assertEquals("pdf", material(TipoMaterial.PRESENTACION, "curso1/unidad2/material3.PDF").getFormatoDeVista());
        assertEquals("vacio", material(TipoMaterial.PRESENTACION, "curso1/unidad2/material3.pptx").getFormatoDeVista());
        assertEquals("vacio", material(TipoMaterial.BIBLIOGRAFIA, "curso1/unidad2/material3.docx").getFormatoDeVista());
        assertEquals("enlace", material(TipoMaterial.BIBLIOGRAFIA, "https://sitio.com/libro").getFormatoDeVista());
        assertEquals("vacio", material(TipoMaterial.BIBLIOGRAFIA, null).getFormatoDeVista());
    }
}
