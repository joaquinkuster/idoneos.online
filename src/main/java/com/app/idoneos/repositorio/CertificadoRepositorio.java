package com.app.idoneos.repositorio;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Certificado;
import com.app.idoneos.modelo.Inscripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para acceder y gestionar la entidad {@link Certificado} en la base de datos.
 * Extiende de {@link JpaRepository}, proporcionando métodos CRUD básicos para la entidad {@link Certificado}.
 */
@Repository
public interface CertificadoRepositorio extends JpaRepository<Certificado, Integer> {

    /**
     * Encuentra todos los registros vigentes (no anulados).
     *
     * @return Una lista de {@link Certificado} vigentes.
     */
    List<Certificado> findByAnuladoFalse();

    /**
     * Busca los registros vigentes de certificado asociados a inscripcion.
     *
     * @param inscripcion el registro de Inscripcion asociado
     * @return una lista de Certificado asociados al Inscripcion indicado
     */
    List<Certificado> findByInscripcionAndAnuladoFalse(Inscripcion inscripcion);

    /**
     * Busca el certificado por su atributo único 'numero'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param numero el valor de 'numero' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<Certificado> findByNumero(String numero);
}
