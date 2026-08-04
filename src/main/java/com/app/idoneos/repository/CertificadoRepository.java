package com.app.idoneos.repository;

import com.app.idoneos.model.Certificado;
import com.app.idoneos.model.Inscripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CertificadoRepository extends JpaRepository<Certificado, Integer> {
    Optional<Certificado> findByInscripcion(Inscripcion inscripcion);
}
