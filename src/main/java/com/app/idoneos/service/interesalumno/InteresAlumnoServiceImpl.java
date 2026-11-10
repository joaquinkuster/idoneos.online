package com.app.idoneos.service.interesalumno;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.idoneos.model.Alumno;
import com.app.idoneos.model.Categoria;
import com.app.idoneos.model.InteresAlumno;
import com.app.idoneos.repository.AlumnoRepository;
import com.app.idoneos.repository.InteresAlumnoRepository;
import com.app.idoneos.service.categoria.CategoriaService;

/** Implementación de InteresAlumnoService. */
@Service
public class InteresAlumnoServiceImpl implements InteresAlumnoService {

    @Autowired
    private InteresAlumnoRepository interesAlumnoRepository;

    @Autowired
    private AlumnoRepository alumnoRepository;

    @Autowired
    private CategoriaService categoriaService;

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    @Override
    public List<Categoria> buscarCategorias(Alumno alumno) {
        return interesAlumnoRepository.findByAlumno(alumno).stream().map(InteresAlumno::getCategoria)
                .sorted(Comparator.comparing(Categoria::getNombre, String.CASE_INSENSITIVE_ORDER)).toList();
    }

    @Override
    public boolean debePreguntar(Alumno alumno) {
        return interesAlumnoRepository.findByAlumno(alumno).isEmpty();
    }

    @Override
    public List<Categoria> buscarCategoriasElegibles() {
        return categoriaService.buscarConFiltros(null, false, "nombre").stream()
                .sorted(Comparator.comparing(Categoria::getNombre, String.CASE_INSENSITIVE_ORDER)).toList();
    }

    // ----------------------------------------------------------------------
    // Modificaciones
    // ----------------------------------------------------------------------

    @Override
    @Transactional
    public void guardar(int idAlumno, List<Integer> idsCategorias) {
        if (idsCategorias == null || idsCategorias.isEmpty()) {
            throw new IllegalArgumentException("Error! Elegí al menos una categoría.");
        }
        Alumno alumno = alumnoRepository.findById(idAlumno)
                .orElseThrow(() -> new IllegalArgumentException("Error! El alumno no existe."));
        Set<Integer> elegidas = new HashSet<>(idsCategorias);
        List<Categoria> categorias = buscarCategoriasElegibles().stream()
                .filter(categoria -> elegidas.contains(categoria.getIdCategoria())).toList();
        if (categorias.size() != elegidas.size()) {
            throw new IllegalArgumentException("Error! Alguna de las categorías elegidas no está disponible.");
        }
        interesAlumnoRepository.deleteByAlumno(alumno);
        interesAlumnoRepository.flush();
        categorias.forEach(categoria -> interesAlumnoRepository.save(new InteresAlumno(alumno, categoria)));
    }
}
