package com.app.ecomisiones.repository;

import com.app.ecomisiones.model.Certificado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CertificadoRepository extends JpaRepository<Certificado, Integer> {
}
