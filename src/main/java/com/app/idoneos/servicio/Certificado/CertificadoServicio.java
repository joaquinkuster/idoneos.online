package com.app.idoneos.servicio.Certificado;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Certificado;
import com.app.idoneos.modelo.Inscripcion;

/**
 * Servicio para gestionar las operaciones relacionadas con el certificado.
 */
public interface CertificadoServicio {

    // ---------------------------------------------------------------- Lecturas

    /**
     * Busca los registros vigentes de certificado asociados a inscripcion.
     *
     * @param inscripcion el registro de Inscripcion asociado
     * @return una lista de Certificado asociados al Inscripcion indicado
     */
    List<Certificado> buscarPorInscripcion(Inscripcion inscripcion);

    /**
     * Busca el certificado por su atributo único 'numero'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param numero el valor de 'numero' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<Certificado> buscarPorNumero(String numero);

    // ---------------------------------------------------------------- Modificaciones

    /**
     * Corrige el nombre y/o el DNI del alumno registrados en un certificado emitido de una inscripción activa
     * (CU-48), ante un error de tipeo.
     *
     * @param idCertificado el certificado
     * @param nombre el nombre completo del alumno
     * @param dni el DNI del alumno (7 u 8 dígitos)
     * @return el certificado corregido
     * @throws IllegalArgumentException si falta algún dato, el DNI no es válido, el certificado está anulado o la
     *         inscripción está dada de baja
     */
    Certificado corregirDatos(Integer idCertificado, String nombre, String dni);
}
