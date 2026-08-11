package com.app.idoneos.service.Certificado;

import com.app.idoneos.model.Inscripcion;
import com.app.idoneos.repository.InscripcionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Servicio para emitir y consultar certificados de finalización de curso directamente en Inscripcion.
 */
@Service
public class CertificadoService {

    @Autowired private InscripcionRepository inscripcionRepository;

    /**
     * Emite el certificado para una inscripción si no tiene uno aún.
     * El número tiene formato: CERT-{año}-{id_inscripcion:06d}
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
