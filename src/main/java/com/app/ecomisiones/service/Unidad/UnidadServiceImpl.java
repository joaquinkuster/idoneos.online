package com.app.ecomisiones.service.Unidad;

import com.app.ecomisiones.model.Curso;
import com.app.ecomisiones.model.Unidad;
import com.app.ecomisiones.repository.UnidadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UnidadServiceImpl implements UnidadService {

    @Autowired
    private UnidadRepository unidadRepository;

    @Override
    public Unidad guardar(Unidad unidad) {
        return unidadRepository.save(unidad);
    }

    @Override
    public Optional<Unidad> buscarPorId(Integer id) {
        return unidadRepository.findById(id).filter(u -> !u.getBaja());
    }

    @Override
    public List<Unidad> obtenerTodo() {
        return unidadRepository.findAll();
    }

    @Override
    public List<Unidad> obtenerPorCurso(Curso curso) {
        return unidadRepository.findByCursoAndBajaFalseOrderByNumeroOrdenAsc(curso);
    }

    @Override
    public int contarUnidadesPorCurso(Curso curso) {
        return unidadRepository.findByCursoAndBajaFalseOrderByNumeroOrdenAsc(curso).size();
    }

    @Override
    public Unidad modificar(Unidad unidad) {
        return unidadRepository.save(unidad);
    }

    @Override
    public void borrar(Unidad unidad) {
        unidad.setBaja(true);
        unidadRepository.save(unidad);
    }

    @Override
    public boolean existePorId(Integer id) {
        return unidadRepository.existsById(id) && buscarPorId(id).isPresent();
    }
}
