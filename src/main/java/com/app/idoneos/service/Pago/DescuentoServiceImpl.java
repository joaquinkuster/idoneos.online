package com.app.idoneos.service.Pago;

import com.app.idoneos.exception.ExcepcionRecursoNoEncontrado;
import com.app.idoneos.exception.ExcepcionValidacion;
import com.app.idoneos.model.Descuento;
import com.app.idoneos.repository.DescuentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * TRAZABILIDAD — Implementación de servicios para la gestión comercial de Descuentos y Promociones.
 *
 * MOD-F-03: Módulo de Inscripciones y Pagos
 *   CU-49 — Buscar descuento: consulta de promociones vigentes por nombre y validez temporal.
 *   CU-50 — Registrar descuento: alta de bonificación con control de vigencia, límite y cursos requeridos.
 *   CU-51 — Modificar descuento: edición de parámetros de descuento.
 *   CU-52 — Dar de baja descuento: deshabilitación o baja lógica de la promoción.
 */
@Service
@Transactional
public class DescuentoServiceImpl implements DescuentoService {

    @Autowired private DescuentoRepository descuentoRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<Descuento> buscarPorId(Integer id) {
        return descuentoRepository.findById(id).filter(d -> !d.getBaja());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Descuento> buscarPorCodigo(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) return Optional.empty();
        return descuentoRepository.findAll().stream()
                .filter(d -> !d.getBaja() && codigo.trim().equalsIgnoreCase(d.getNombre()))
                .findFirst();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Descuento> obtenerTodo() {
        return descuentoRepository.findAll().stream().filter(d -> !d.getBaja()).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Descuento> obtenerDescuentosVigentes() {
        return descuentoRepository.findAll().stream()
                .filter(d -> !d.getBaja() && d.estaVigente())
                .toList();
    }

    /**
     * CU-48 — Registrar descuento.
     * Reglas de negocio:
     * - Porcentaje entre 1% y 100% (Excepción CU-48, paso 4).
     * - Fechas de inicio y fin obligatorias y coherentes (Excepción CU-48, paso 5).
     */
    @Override
    public Descuento registrarDescuento(Descuento descuento) {
        if (descuento.getPorcentaje() <= 0 || descuento.getPorcentaje() > 100) {
            throw new ExcepcionValidacion("CU-48 Excepción paso 4: El porcentaje de descuento debe estar entre 1% y 100%.");
        }
        if (descuento.getVigenciaDesde() != null && descuento.getVigenciaHasta() != null &&
            !descuento.getVigenciaHasta().isAfter(descuento.getVigenciaDesde())) {
            throw new ExcepcionValidacion("CU-48 Excepción paso 5: La fecha de fin de vigencia debe ser posterior a la fecha inicial.");
        }

        descuento.setBaja(false);
        return descuentoRepository.save(descuento);
    }

    /**
     * CU-49 — Modificar descuento.
     */
    @Override
    public Descuento modificarDescuento(Descuento descuento) {
        Descuento existente = descuentoRepository.findById(descuento.getId())
                .orElseThrow(() -> new ExcepcionRecursoNoEncontrado("Descuento", "id", descuento.getId()));

        if (existente.getBaja()) {
            throw new ExcepcionValidacion("CU-49 Precondición: No se puede modificar un descuento dado de baja.");
        }

        if (descuento.getPorcentaje() <= 0 || descuento.getPorcentaje() > 100) {
            throw new ExcepcionValidacion("CU-49 Excepción paso 4: El porcentaje de descuento debe estar entre 1% y 100%.");
        }

        existente.setNombre(descuento.getNombre());
        existente.setPorcentaje(descuento.getPorcentaje());
        existente.setVigenciaDesde(descuento.getVigenciaDesde());
        existente.setVigenciaHasta(descuento.getVigenciaHasta());
        return descuentoRepository.save(existente);
    }

    /**
     * CU-50 — Eliminar descuento (Baja Lógica).
     */
    @Override
    public void darDeBajaDescuento(int descuentoId) {
        Descuento descuento = descuentoRepository.findById(descuentoId)
                .orElseThrow(() -> new ExcepcionRecursoNoEncontrado("Descuento", "id", descuentoId));

        if (descuento.getBaja()) {
            throw new ExcepcionValidacion("CU-50 Excepción: El descuento ya se encuentra dado de baja.");
        }

        descuento.setBaja(true);
        descuentoRepository.save(descuento);
    }

    @Override
    public Descuento guardar(Descuento descuento) {
        return registrarDescuento(descuento);
    }

    @Override
    public Descuento modificar(Descuento descuento) {
        return modificarDescuento(descuento);
    }

    @Override
    public void borrar(Descuento descuento) {
        darDeBajaDescuento(descuento.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorId(Integer id) {
        return descuentoRepository.existsById(id) && buscarPorId(id).isPresent();
    }
}
