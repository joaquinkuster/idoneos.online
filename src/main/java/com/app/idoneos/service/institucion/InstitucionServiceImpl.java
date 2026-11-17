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

    @Autowired
    private ParametroService parametroService;

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
