package com.app.idoneos.service.IA;

import com.app.idoneos.exception.ExcepcionRecursoNoEncontrado;
import com.app.idoneos.exception.ExcepcionValidacion;
import com.app.idoneos.model.ClaseClonIA;
import com.app.idoneos.model.Docente;
import com.app.idoneos.model.Unidad;
import com.app.idoneos.repository.ClaseClonIARepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementación de servicios para la generación de clases mediante Clon IA (CU-71 a CU-74).
 */
@Service
@Transactional
public class ClaseClonIAServiceImpl implements ClaseClonIAService {

    @Autowired private ClaseClonIARepository claseClonIARepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<ClaseClonIA> buscarPorId(Integer id) {
        return claseClonIARepository.findById(id).filter(c -> !c.getBaja());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClaseClonIA> obtenerPorUnidad(Unidad unidad) {
        return claseClonIARepository.findByUnidad(unidad).stream().filter(c -> !c.getBaja()).toList();
    }

    /**
     * CU-72 — Generar clase con Clon IA.
     * Reglas de negocio:
     * - El docente debe poseer consentimiento firmado para Clon IA (fechaConsentimientoClon != null) (Excepción CU-72, paso 4).
     * - El guión de clase es obligatorio (Excepción CU-72, paso 5).
     * - La unidad temática asociada es obligatoria.
     */
    @Override
    public ClaseClonIA generarClaseClonIA(ClaseClonIA claseClon, Docente docente) {
        if (docente == null || docente.getFechaConsentimientoClon() == null) {
            throw new ExcepcionValidacion("CU-72 Excepción paso 4: El docente debe contar con el consentimiento de Clon IA firmado.");
        }
        if (claseClon.getGuion() == null || claseClon.getGuion().trim().isEmpty()) {
            throw new ExcepcionValidacion("CU-72 Excepción paso 5: El guión textual para el avatar IA es obligatorio.");
        }
        if (claseClon.getUnidad() == null) {
            throw new ExcepcionValidacion("CU-72 Excepción paso 4: La unidad temática asociada es obligatoria.");
        }

        claseClon.setBaja(false);
        claseClon.setFechaGeneracion(LocalDateTime.now());
        return claseClonIARepository.save(claseClon);
    }

    /**
     * CU-73 — Modificar clase con Clon IA.
     */
    @Override
    public ClaseClonIA modificarClaseClonIA(ClaseClonIA claseClon) {
        ClaseClonIA existente = claseClonIARepository.findById(claseClon.getId())
                .orElseThrow(() -> new ExcepcionRecursoNoEncontrado("Clase Clon IA", "id", claseClon.getId()));

        if (existente.getBaja()) {
            throw new ExcepcionValidacion("CU-73 Precondición: No se puede modificar una clase dada de baja.");
        }

        if (claseClon.getGuion() == null || claseClon.getGuion().trim().isEmpty()) {
            throw new ExcepcionValidacion("CU-73 Excepción paso 4: El guión textual no puede quedar vacío.");
        }

        existente.setGuion(claseClon.getGuion().trim());
        if (claseClon.getTitulo() != null) {
            existente.setTitulo(claseClon.getTitulo().trim());
        }
        return claseClonIARepository.save(existente);
    }

    /**
     * CU-74 — Eliminar clase con Clon IA (Baja Lógica).
     */
    @Override
    public void darDeBajaClaseClonIA(int claseClonId) {
        ClaseClonIA claseClon = claseClonIARepository.findById(claseClonId)
                .orElseThrow(() -> new ExcepcionRecursoNoEncontrado("Clase Clon IA", "id", claseClonId));

        if (claseClon.getBaja()) {
            throw new ExcepcionValidacion("CU-74 Excepción: La clase ya se encuentra dada de baja.");
        }

        claseClon.setBaja(true);
        claseClonIARepository.save(claseClon);
    }
}
