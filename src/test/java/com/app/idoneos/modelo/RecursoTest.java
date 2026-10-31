package com.app.idoneos.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias de las reglas de {@link Recurso}, {@link TemaRecurso} y {@link TipoRecurso}.
 */
class RecursoTest {

    private Recurso grabacion(String ruta) {
        Recurso recurso = new Recurso(null, "Clase");
        recurso.setGrabacion(new Grabacion(recurso, ruta));
        return recurso;
    }

    private Recurso enlace(String url) {
        Recurso recurso = new Recurso(null, "Sitio");
        recurso.setEnlace(new Enlace(recurso, url));
        return recurso;
    }

    private Recurso lectura(String categoria, String ruta) {
        Recurso recurso = new Recurso(null, "Diapositivas");
        recurso.setMaterialLectura(new MaterialLectura(recurso, new CategoriaMaterialLectura(categoria), ruta, "Autor"));
        return recurso;
    }

    private TemaRecurso material(Recurso recurso) {
        return new TemaRecurso(null, recurso, null, 1);
    }

    @Test
    @DisplayName("El tipo de un recurso sale de la tabla que guarda sus datos")
    void tipo() {
        assertEquals("Grabación", grabacion("a.mp4").getTipo());
        assertEquals("Enlace", enlace("https://sitio.com").getTipo());
        assertEquals("Resumen", lectura(CategoriaMaterialLectura.RESUMEN, "r.pdf").getTipo());
        assertEquals("Autor", lectura(CategoriaMaterialLectura.RESUMEN, "r.pdf").getAutor());

        Recurso libro = new Recurso(null, "Libro");
        libro.setBibliografia(new Bibliografia(libro, "978-0-00-000000-0", "Autor"));
        assertEquals("Bibliografía", libro.getTipo());
    }

    @Test
    @DisplayName("Un material está publicado si ni él ni su recurso están dados de baja ni ocultos")
    void publicado() {
        Recurso recurso = grabacion("a.mp4");
        TemaRecurso material = material(recurso);
        assertTrue(material.estaPublicado());

        material.setOculto(true);
        assertFalse(material.estaPublicado());

        material.setOculto(false);
        recurso.marcarInactivo();
        assertFalse(material.estaPublicado());
    }

    @Test
    @DisplayName("Distingue un archivo de la plataforma de un enlace externo")
    void archivoOEnlace() {
        assertTrue(grabacion("curso1/tema2/recurso3.mp4").tieneArchivo());
        assertFalse(grabacion("curso1/tema2/recurso3.mp4").esEnlaceExterno());
        assertTrue(enlace("https://sitio.com/libro.pdf").esEnlaceExterno());
        assertFalse(enlace("https://sitio.com/libro.pdf").tieneArchivo());
    }

    @Test
    @DisplayName("El formato de vista depende del archivo: video, pdf, enlace o vacío")
    void formatoDeVista() {
        assertEquals("video", material(grabacion("curso1/tema2/recurso3.mp4")).getFormatoDeVista());
        assertEquals("video", material(grabacion("curso1/tema2/recurso3.webm")).getFormatoDeVista());
        assertEquals("pdf", material(lectura(CategoriaMaterialLectura.PRESENTACION, "curso1/tema2/recurso3.PDF")).getFormatoDeVista());
        assertEquals("vacio", material(lectura(CategoriaMaterialLectura.PRESENTACION, "curso1/tema2/recurso3.pptx")).getFormatoDeVista());
        assertEquals("enlace", material(enlace("https://sitio.com/libro")).getFormatoDeVista());
        assertEquals("vacio", material(new Recurso(null, "Vacío")).getFormatoDeVista());
    }

    @Test
    @DisplayName("Una grabación es de la modalidad En vivo si está ligada a una clase en vivo, y si no, Grabada; un video con clon, Clon de IA")
    void modalidad() {
        Recurso recurso = grabacion("a.mp4");
        assertEquals(Modalidad.GRABADA, recurso.getModalidadAsociada());

        ClaseEnVivo clase = new ClaseEnVivo();
        clase.setGrabacion(recurso.getGrabacion());
        recurso.getGrabacion().getClasesEnVivo().add(clase);
        assertEquals(Modalidad.EN_VIVO, recurso.getModalidadAsociada());

        Recurso clon = new Recurso(null, "Video");
        clon.setVideoClon(new VideoClon(clon, new EstadoGeneracion(EstadoGeneracion.GENERADO), "Guion"));
        assertEquals(Modalidad.CLON_IA, clon.getModalidadAsociada());
        assertTrue(clon.getGeneradoPorIa());

        assertNull(enlace("https://sitio.com").getModalidadAsociada());
    }

    @Test
    @DisplayName("Cada tipo define si se sube a mano, sus formatos y su tamaño máximo")
    void tipos() {
        assertTrue(TipoRecurso.GRABACION.permiteSubidaManual());
        assertTrue(TipoRecurso.PRESENTACION.permiteSubidaManual());
        assertTrue(TipoRecurso.BIBLIOGRAFIA.permiteSubidaManual());
        assertTrue(TipoRecurso.ENLACE.permiteSubidaManual());
        assertFalse(TipoRecurso.RESUMEN.permiteSubidaManual());
        assertFalse(TipoRecurso.VIDEO_CLON.permiteSubidaManual());
        assertEquals(List.of("mp4", "webm"), TipoRecurso.GRABACION.getExtensionesPermitidas());
        assertEquals(List.of("pdf"), TipoRecurso.PRESENTACION.getExtensionesPermitidas());
        assertEquals(500L * 1024 * 1024, TipoRecurso.GRABACION.getTamanioMaximo());
        assertEquals(50L * 1024 * 1024, TipoRecurso.BIBLIOGRAFIA.getTamanioMaximo());
        assertEquals(TipoRecurso.GRABACION, TipoRecurso.deTexto("Grabación"));
        assertEquals(TipoRecurso.PRESENTACION, TipoRecurso.deTexto("presentacion"));
        assertTrue(TipoRecurso.GRABACION.getDescripcionDeSubida().contains("MP4, WEBM"));
    }
}
