package com.app.idoneos.service.modulo_inscripciones;

import com.app.idoneos.exception.*;
import com.app.idoneos.model.*;
import com.app.idoneos.repository.modulo_inscripciones.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class DescuentoServiceImpl implements DescuentoService {

    @Autowired private DescuentoRepository descuentoRepository;

    @Override
    public Descuento registrarDescuento(String codigo, float porcentaje, String fechaInicio, String fechaFin, Integer cursoId) {
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new ExcepcionValidacion("CU-50 Excepción: El código de descuento es obligatorio.");
        }
        if (porcentaje <= 0 || porcentaje > 100) {
            throw new ExcepcionValidacion("CU-50 Excepción: El porcentaje debe estar entre 1% y 100%.");
        }

        LocalDateTime fIni = LocalDateTime.parse(fechaInicio.contains("T") ? fechaInicio : fechaInicio + "T00:00:00");
        LocalDateTime fFin = LocalDateTime.parse(fechaFin.contains("T") ? fechaFin : fechaFin + "T23:59:59");

        if (fFin.isBefore(fIni)) {
            throw new ExcepcionValidacion("CU-50 Excepción: La fecha de fin debe ser posterior a la fecha de inicio.");
        }

        Descuento d = new Descuento();
        d.setNombre(codigo.trim());
        d.setPorcentaje(porcentaje);
        d.setVigenciaDesde(fIni);
        d.setVigenciaHasta(fFin);
        d.setCantidadLimite(100);
        d.setCantidadUsada(0);
        d.setCursosRequeridos(0);
        d.setBaja(false);
        d.setFechaCreacion(LocalDateTime.now());
        return descuentoRepository.save(d);
    }

    @Override
    public Descuento modificarDescuento(Integer id, String codigo, float porcentaje, String fechaInicio, String fechaFin, Integer cursoId) {
        Descuento d = descuentoRepository.findById(id)
                .orElseThrow(() -> new ExcepcionRecursoNoEncontrado("Descuento no encontrado con ID: " + id));

        if (d.getBaja()) {
            throw new ExcepcionNegocio("CU-51 Excepción: No se puede modificar un descuento dado de baja.");
        }

        if (codigo == null || codigo.trim().isEmpty()) {
            throw new ExcepcionValidacion("CU-51 Excepción: El código de descuento no puede estar vacío.");
        }
        if (porcentaje <= 0 || porcentaje > 100) {
            throw new ExcepcionValidacion("CU-51 Excepción: El porcentaje debe estar entre 1% y 100%.");
        }

        LocalDateTime fIni = LocalDateTime.parse(fechaInicio.contains("T") ? fechaInicio : fechaInicio + "T00:00:00");
        LocalDateTime fFin = LocalDateTime.parse(fechaFin.contains("T") ? fechaFin : fechaFin + "T23:59:59");

        if (fFin.isBefore(fIni)) {
            throw new ExcepcionValidacion("CU-51 Excepción: La fecha de fin debe ser posterior a la de inicio.");
        }

        d.setNombre(codigo.trim());
        d.setPorcentaje(porcentaje);
        d.setVigenciaDesde(fIni);
        d.setVigenciaHasta(fFin);
        d.setUltimaModificacion(LocalDateTime.now());
        return descuentoRepository.save(d);
    }

    @Override
    public void darDeBajaDescuento(Integer id) {
        Descuento d = descuentoRepository.findById(id)
                .orElseThrow(() -> new ExcepcionRecursoNoEncontrado("Descuento no encontrado con ID: " + id));

        if (d.getBaja()) {
            throw new ExcepcionNegocio("El descuento ya se encuentra dado de baja.");
        }

        d.setBaja(true);
        d.setUltimaModificacion(LocalDateTime.now());
        descuentoRepository.save(d);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Descuento> buscarPorId(Integer id) {
        return descuentoRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Descuento> obtenerTodos() {
        return descuentoRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Descuento> buscarDescuentosConFiltros(String codigo, Boolean vigente) {
        List<Descuento> todos = descuentoRepository.findAll();
        return todos.stream()
                .filter(d -> codigo == null || codigo.isBlank() || d.getNombre().toLowerCase().contains(codigo.toLowerCase()))
                .filter(d -> vigente == null || (vigente ? d.estaVigente() : !d.estaVigente()))
                .collect(Collectors.toList());
    }
}
