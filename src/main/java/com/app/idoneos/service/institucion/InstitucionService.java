package com.app.idoneos.service.institucion;

/** Servicio de los datos institucionales (razón social, CUIT, domicilio, logo y contacto) que el administrador configura en Parámetros. */
public interface InstitucionService {

    /** Datos institucionales. */
    record Institucion(String razonSocial, String cuit, String domicilio, String logo, String email, String telefono, String cbu) {

        /** Nombre comercial: la razón social sin la forma societaria (S.A.S., S.A., S.R.L.). */
        public String nombreComercial() {
            return razonSocial.replaceAll("(?i)\\s+(S\\.?A\\.?S\\.?|S\\.?A\\.?|S\\.?R\\.?L\\.?)$", "").trim();
        }

        /** Primera palabra del nombre comercial (se muestra en tipografía serif). */
        public String nombreSerif() {
            String nombre = nombreComercial();
            int corte = nombre.indexOf(' ');
            return corte < 0 ? nombre : nombre.substring(0, corte);
        }

        /** Resto del nombre comercial (se muestra en negrita). */
        public String nombreNegrita() {
            String nombre = nombreComercial();
            int corte = nombre.indexOf(' ');
            return corte < 0 ? "" : nombre.substring(corte + 1);
        }
    }

    /**
     * Obtiene los datos institucionales vigentes. Los que no están cargados toman un valor por defecto.
     *
     * @return los datos institucionales
     */
    Institucion obtener();

    /** Descarta los datos guardados en memoria: se vuelven a leer en el próximo pedido (se llama al guardar la configuración). */
    void invalidar();
}
