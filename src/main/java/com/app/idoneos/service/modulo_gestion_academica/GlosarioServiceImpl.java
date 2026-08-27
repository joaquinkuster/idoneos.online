package com.app.idoneos.service.modulo_gestion_academica;

import com.app.idoneos.exception.*;
import com.app.idoneos.model.*;
import com.app.idoneos.repository.modulo_gestion_academica.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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
        return terminoGlosarioRepository.findByUnidadAndBajaFalse(unidad);
    }

    @Override
    public TerminoGlosario registrarTermino(TerminoGlosario termino) {
        if (termino.getTermino() == null || termino.getTermino().trim().isEmpty()) {
            throw new ExcepcionValidacion("CU-32 Excepción: El término de glosario es obligatorio.");
        }
        if (termino.getDefinicion() == null || termino.getDefinicion().trim().isEmpty()) {
            throw new ExcepcionValidacion("CU-32 Excepción: La definición del término es obligatoria.");
        }
        if (termino.getUnidad() == null) {
            throw new ExcepcionValidacion("CU-32 Excepción: La unidad temática asociada es obligatoria.");
        }

        termino.setBaja(false);
        return terminoGlosarioRepository.save(termino);
    }

    @Override
    public TerminoGlosario modificarTermino(TerminoGlosario termino) {
        TerminoGlosario existente = terminoGlosarioRepository.findById(termino.getId())
                .orElseThrow(() -> new ExcepcionRecursoNoEncontrado("Término de Glosario", "id", termino.getId()));

        if (existente.getBaja()) {
            throw new ExcepcionValidacion("CU-33 Precondición: No se puede modificar un término dado de baja.");
        }
        if (termino.getTermino() == null || termino.getTermino().trim().isEmpty()) {
            throw new ExcepcionValidacion("CU-33 Excepción: El término no puede quedar vacío.");
        }
        if (termino.getDefinicion() == null || termino.getDefinicion().trim().isEmpty()) {
            throw new ExcepcionValidacion("CU-33 Excepción: La definición no puede quedar vacía.");
        }

        existente.setTermino(termino.getTermino().trim());
        existente.setDefinicion(termino.getDefinicion().trim());
        return terminoGlosarioRepository.save(existente);
    }

    @Override
    public void darDeBajaTermino(int terminoId) {
        darDeBaja(terminoId);
    }

    @Override
    public void darDeBaja(Integer id) {
        TerminoGlosario existente = terminoGlosarioRepository.findById(id)
                .orElseThrow(() -> new ExcepcionRecursoNoEncontrado("Término de Glosario", "id", id));

        if (existente.getBaja()) {
            throw new ExcepcionValidacion("El término ya se encuentra dado de baja.");
        }

        existente.setBaja(true);
        terminoGlosarioRepository.save(existente);
    }

    @Override
    public TerminoGlosario guardar(TerminoGlosario entidad) {
        return registrarTermino(entidad);
    }

    @Override
    public TerminoGlosario modificar(TerminoGlosario entidad) {
        return modificarTermino(entidad);
    }

    @Override
    public void borrar(TerminoGlosario entidad) {
        darDeBaja(entidad.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorId(Integer id) {
        return terminoGlosarioRepository.existsById(id) && buscarPorId(id).isPresent();
    }
}
