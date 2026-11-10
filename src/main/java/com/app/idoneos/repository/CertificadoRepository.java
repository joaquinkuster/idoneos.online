package com.app.idoneos.repository;

import com.app.idoneos.model.Alumno;
import com.app.idoneos.model.Certificado;
import com.app.idoneos.model.Inscripcion;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repositorio de Certificado. */
@Repository
public interface CertificadoRepository extends JpaRepository<Certificado, Integer> {

    /** Encuentra todos los registros vigentes (no anulados). */
    List<Certificado> findByAnuladoFalse();

    /** Busca los registros vigentes de certificado asociados a inscripcion. */
    List<Certificado> findByInscripcionAndAnuladoFalse(Inscripcion inscripcion);

    /** Busca el certificado por su atributo único 'numero'. */
    Optional<Certificado> findByNumero(String numero);

    /** Busca los certificados vigentes (no anulados) de un alumno, del más reciente al más antiguo. */
    List<Certificado> findByInscripcionAlumnoAndAnuladoFalseOrderByFechaEmisionDesc(Alumno alumno);
}
