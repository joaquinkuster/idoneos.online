package com.app.idoneos.service.Curso;

import com.app.idoneos.model.Categoria;
import com.app.idoneos.model.Curso;
import com.app.idoneos.model.Usuario;
import com.app.idoneos.service.CrudService;

import java.util.List;

public interface CursoService extends CrudService<Curso> {

    List<Curso> obtenerPublicados();

    List<Curso> obtenerPorCategoria(Categoria categoria);

    List<Curso> obtenerPorDocente(Usuario docente);

    List<Curso> buscarPorNombre(String query);
}
