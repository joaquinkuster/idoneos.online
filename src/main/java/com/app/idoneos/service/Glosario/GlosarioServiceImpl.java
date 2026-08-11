package com.app.idoneos.service.Glosario;

import com.app.idoneos.exception.ExcepcionRecursoNoEncontrado;
import com.app.idoneos.exception.ExcepcionValidacion;
import com.app.idoneos.model.TerminoGlosario;
import com.app.idoneos.model.Unidad;
import com.app.idoneos.repository.TerminoGlosarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementación de servicios para la gestión de términos del glosario técnico (CU-29 a CU-32).
 */
@Service
@Transactional
public class GlosarioServiceImpl implements GlosarioService {

    @Autowired private TerminoGlosarioRepository terminoGlosarioRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<TerminoGlosario> buscarPorId(Integer id) {
        return terminoGlosarioRepository.findById(id).filter(t -> !t.getBaja());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TerminoGlosario> obtenerTodo() {
        return terminoGlosarioRepository.findAll().stream().filter(t -> !t.getBaja()).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TerminoGlosario> obtenerPorUnidad(Unidad unidad) {
        return terminoGlosarioRepository.findByUnidad(unidad).stream().filter(t -> !t.getBaja()).toList();
    }

    /**
     * CU-30 — Registrar término de glosario.
     * Reglas de negocio:
     * - Término no nulo ni vacío (Excepción CU-30, paso 4).
     * - Definición no nula ni vacía (Excepción CU-30, paso 5).
     * - Unidad temática asociada obligatoria.
     */
    @Override
    public TerminoGlosario registrarTermino(TerminoGlosario termino) {
        if (termino.getTermino() == null || termino.getTermino().trim().isEmpty()) {
            throw new ExcepcionValidacion("CU-30 Excepción paso 4: El término de glosario es obligatorio.");
        }
        if (termino.getDefinicion() == null || termino.getDefinicion().trim().isEmpty()) {
            throw new ExcepcionValidacion("CU-30 Excepción paso 5: La definición del término es obligatoria.");
        }
        if (termino.getUnidad() == null) {
            throw new ExcepcionValidacion("CU-30 Excepción paso 4: La unidad temática asociada es obligatoria.");
        }

        termino.setBaja(false);
        return terminoGlosarioRepository.save(termino);
    }

    /**
     * CU-31 — Modificar término de glosario.
     */
    @Override
    public TerminoGlosario modificarTermino(TerminoGlosario termino) {
        TerminoGlosario existente = terminoGlosarioRepository.findById(termino.getId())
                .orElseThrow(() -> new ExcepcionRecursoNoEncontrado("Término de Glosario", "id", termino.getId()));

        if (existente.getBaja()) {
            throw new ExcepcionValidacion("CU-31 Precondición: No se puede modificar un término dado de baja.");
        }
        if (termino.getTermino() == null || termino.getTermino().trim().isEmpty()) {
            throw new ExcepcionValidacion("CU-31 Excepción paso 4: El término no puede quedar vacío.");
        }
        if (termino.getDefinicion() == null || termino.getDefinicion().trim().isEmpty()) {
            throw new ExcepcionValidacion("CU-31 Excepción paso 5: La definición no puede quedar vacía.");
        }

        existente.setTermino(termino.getTermino().trim());
        existente.setDefinicion(termino.getDefinicion().trim());
        return terminoGlosarioRepository.save(existente);
    }

    /**
     * CU-32 — Eliminar término de glosario (Baja Lógica).
     */
    @Override
    public void darDeBajaTermino(int terminoId) {
        TerminoGlosario termino = terminoGlosarioRepository.findById(terminoId)
                .orElseThrow(() -> new ExcepcionRecursoNoEncontrado("Término de Glosario", "id", terminoId));

        if (termino.getBaja()) {
            throw new ExcepcionValidacion("CU-32 Excepción: El término ya se encuentra dado de baja.");
        }

        termino.setBaja(true);
        terminoGlosarioRepository.save(termino);
    }

    @Override
    public TerminoGlosario guardar(TerminoGlosario termino) {
        return registrarTermino(termino);
    }

    @Override
    public TerminoGlosario modificar(TerminoGlosario termino) {
        return modificarTermino(termino);
    }

    @Override
    public void borrar(TerminoGlosario termino) {
        darDeBajaTermino(termino.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorId(Integer id) {
        return terminoGlosarioRepository.existsById(id) && buscarPorId(id).isPresent();
    }
}
