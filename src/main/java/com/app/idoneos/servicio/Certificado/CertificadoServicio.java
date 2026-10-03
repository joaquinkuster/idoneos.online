package com.app.idoneos.servicio.Certificado;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Certificado;
import com.app.idoneos.modelo.Inscripcion;

/**
 * Servicio para gestionar las operaciones relacionadas con el certificado.
 */
public interface CertificadoServicio {

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
}
