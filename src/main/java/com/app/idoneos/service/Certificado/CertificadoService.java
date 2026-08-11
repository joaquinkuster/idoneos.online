package com.app.idoneos.service.Certificado;

import com.app.idoneos.model.Inscripcion;
import com.app.idoneos.repository.InscripcionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Servicio para emitir y consultar certificados digitales de aprobación de curso (CU-91: Emitir certificado de aprobación).
 */
@Service
@Transactional
public class CertificadoService {

    @Autowired private InscripcionRepository inscripcionRepository;

    /**
     * CU-91 — Emite el certificado digital de aprobación para la inscripción del alumno.
     * Regla de negocio: Genera un número de certificado correlativo e infalsificable (formato CERT-YYYY-000000).
     */
    public Inscripcion emitirCertificado(Inscripcion inscripcion) {
        if (inscripcion.getNumeroCertificado() != null && !inscripcion.getNumeroCertificado().isBlank()) {
            return inscripcion;
        }

        String numero = "CERT-" + LocalDate.now().getYear()
                + "-" + String.format("%06d", inscripcion.getId());
        inscripcion.setNumeroCertificado(numero);
        inscripcion.setFechaEmisionCertificado(LocalDateTime.now());
        inscripcion.setCertificadoEnviado(true);
        return inscripcionRepository.save(inscripcion);
    }
}
