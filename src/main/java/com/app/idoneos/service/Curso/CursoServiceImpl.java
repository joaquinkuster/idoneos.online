package com.app.idoneos.service.Curso;

import com.app.idoneos.model.Categoria;
import com.app.idoneos.model.Curso;
import com.app.idoneos.model.Usuario;
import com.app.idoneos.repository.CursoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CursoServiceImpl implements CursoService {

    @Autowired
    private CursoRepository cursoRepository;

    @Override
    public Curso guardar(Curso curso) {
        return cursoRepository.save(curso);
    }

    @Override
    public Optional<Curso> buscarPorId(Integer id) {
        return cursoRepository.findById(id).filter(c -> !c.getBaja());
    }

    @Override
    public List<Curso> obtenerTodo() {
        return cursoRepository.findByBajaFalse();
    }

    @Override
    public List<Curso> obtenerPublicados() {
        return cursoRepository.findByBajaFalseAndPublicadoTrue();
    }

    @Override
    public List<Curso> obtenerPorCategoria(Categoria categoria) {
        return cursoRepository.findByCategoriaAndBajaFalseAndPublicadoTrue(categoria);
    }

    @Override
    public List<Curso> obtenerPorDocente(Usuario docente) {
        return cursoRepository.findByDocenteId(docente.getId());
    }

    @Override
    public List<Curso> buscarPorNombre(String query) {
        return cursoRepository.findByNombreContainingIgnoreCaseAndBajaFalseAndPublicadoTrue(query);
    }

    @Override
    public Curso modificar(Curso curso) {
        return cursoRepository.save(curso);
    }

    @Override
    public void borrar(Curso curso) {
        curso.setBaja(true);
        cursoRepository.save(curso);
    }

    @Override
    public boolean existePorId(Integer id) {
        return cursoRepository.existsById(id) && buscarPorId(id).isPresent();
    }
}
