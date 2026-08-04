package com.app.idoneos.service.Certificado;

import com.app.idoneos.model.*;
import com.app.idoneos.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Servicio para emitir y consultar certificados de finalización de curso.
 */
@Service
public class CertificadoService {

    @Autowired private CertificadoRepository certificadoRepository;
    @Autowired private InscripcionRepository inscripcionRepository;

    /**
     * Emite el certificado para una inscripción si no tiene uno aún.
     * El número tiene formato: CERT-{año}-{id_inscripcion:06d}
     */
    public Certificado emitirCertificado(Inscripcion inscripcion) {
        // No emitir dos veces
        Optional<Certificado> existente = certificadoRepository.findByInscripcion(inscripcion);
        if (existente.isPresent()) return existente.get();

        String numero = "CERT-" + LocalDate.now().getYear()
                + "-" + String.format("%06d", inscripcion.getId());
        Certificado cert = new Certificado(numero, inscripcion);
        return certificadoRepository.save(cert);
    }

    public Optional<Certificado> buscarPorInscripcion(Inscripcion inscripcion) {
        return certificadoRepository.findByInscripcion(inscripcion);
    }

    public Optional<Certificado> buscarPorId(Integer id) {
        return certificadoRepository.findById(id);
    }
}
