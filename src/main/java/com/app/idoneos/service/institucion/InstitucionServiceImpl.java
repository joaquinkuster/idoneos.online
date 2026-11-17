package com.app.idoneos.service.institucion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.idoneos.model.Parametro;
import com.app.idoneos.service.parametro.ParametroService;

/** Implementación de InstitucionService. Guarda los datos en memoria unos segundos: se muestran en todas las pantallas. */
@Service
public class InstitucionServiceImpl implements InstitucionService {

    /** Tiempo, en milisegundos, que se conservan los datos antes de volver a leerlos. */
    private static final long VIGENCIA = 30_000;

    /** Prefijo de la dirección del logo subido desde Parámetros. */
    private static final String PREFIJO_LOGO_SUBIDO = "/uploads/institucion/";

    @Autowired
    private ParametroService parametroService;

    @Autowired
    @org.springframework.beans.factory.annotation.Qualifier("directorioCursos")
    private java.nio.file.Path directorioCursos;

    private volatile Institucion guardada;
    private volatile long leidaEn;

    /** Obtiene los datos institucionales vigentes. */
    @Override
    public Institucion obtener() {
        Institucion actual = guardada;
        if (actual == null || System.currentTimeMillis() - leidaEn > VIGENCIA) {
            actual = leer();
            guardada = actual;
            leidaEn = System.currentTimeMillis();
        }
        return actual;
    }

    /** Lee el logo: el subido desde Parámetros está en el disco del servidor; el que viene con la aplicación, en sus recursos. */
    @Override
    public byte[] leerLogo() {
        String ruta = obtener().logo();
        try {
            if (ruta.startsWith(PREFIJO_LOGO_SUBIDO)) {
                java.nio.file.Path archivo = directorioCursos.resolve("institucion").resolve(ruta.substring(PREFIJO_LOGO_SUBIDO.length())).normalize();
                if (archivo.startsWith(directorioCursos.resolve("institucion").normalize()) && java.nio.file.Files.isRegularFile(archivo)) {
                    return java.nio.file.Files.readAllBytes(archivo);
                }
            } else if (ruta.startsWith("/")) {
                try (java.io.InputStream entrada = InstitucionServiceImpl.class.getResourceAsStream("/static" + ruta)) {
                    if (entrada != null) {
                        return entrada.readAllBytes();
                    }
                }
            }
            try (java.io.InputStream entrada = InstitucionServiceImpl.class.getResourceAsStream("/static/img/logos/image.png")) {
                return entrada == null ? null : entrada.readAllBytes();
            }
        } catch (java.io.IOException e) {
            return null;
        }
    }

    /** Descarta los datos guardados en memoria. */
    @Override
    public void invalidar() {
        guardada = null;
    }

    private Institucion leer() {
        return new Institucion(texto(Parametro.INSTITUCION_RAZON_SOCIAL, "Idóneos Online S.A.S."), texto(Parametro.INSTITUCION_CUIT, ""),
                texto(Parametro.INSTITUCION_DOMICILIO, ""), texto(Parametro.INSTITUCION_LOGO, "/img/logos/image.png"),
                texto(Parametro.INSTITUCION_EMAIL, ""), texto(Parametro.INSTITUCION_TELEFONO, ""), texto(Parametro.CBU_INSTITUCION, ""));
    }

    private String texto(String clave, String porDefecto) {
        try {
            return parametroService.obtenerTexto(clave).map(String::trim).filter(v -> !v.isEmpty()).orElse(porDefecto);
        } catch (RuntimeException e) {
            return porDefecto;
        }
    }
}
