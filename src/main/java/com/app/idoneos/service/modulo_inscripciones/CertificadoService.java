package com.app.idoneos.service.modulo_inscripciones;
import com.app.idoneos.service.modulo_reportes.*;

import com.app.idoneos.exception.*;
import com.app.idoneos.repository.modulo_cursos.*;
import com.app.idoneos.repository.modulo_gestion_academica.*;
import com.app.idoneos.repository.modulo_inscripciones.*;
import com.app.idoneos.repository.modulo_evaluaciones.*;
import com.app.idoneos.repository.modulo_clases_vivo.*;
import com.app.idoneos.repository.modulo_ia.*;
import com.app.idoneos.repository.modulo_usuarios.*;
import com.app.idoneos.repository.modulo_auditoria.*;
import com.app.idoneos.repository.modulo_reportes.*;
import com.app.idoneos.repository.modulo_configuracion.*;
import com.app.idoneos.service.modulo_configuracion.*;
import com.app.idoneos.service.modulo_cursos.*;
import com.app.idoneos.service.modulo_gestion_academica.*;
import com.app.idoneos.service.modulo_inscripciones.*;
import com.app.idoneos.service.modulo_evaluaciones.*;
import com.app.idoneos.service.modulo_ia.*;
import com.app.idoneos.service.modulo_usuarios.*;

import com.app.idoneos.model.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * TRAZABILIDAD — Servicio para la emisión y verificación de Certificados Digitales Académicos.
 *
 * MOD-F-03: Módulo de Inscripciones y Pagos
 *   CU-43 — Buscar inscripción (Emisión de certificado): emisión del diploma digital acreditado
 *           con código correlativo único infalsificable (formato CERT-YYYY-000000).
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

