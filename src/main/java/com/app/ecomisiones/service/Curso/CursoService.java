package com.app.ecomisiones.service.Curso;

import com.app.ecomisiones.model.Categoria;
import com.app.ecomisiones.model.Curso;
import com.app.ecomisiones.model.Usuario;
import com.app.ecomisiones.service.CrudService;

import java.util.List;

public interface CursoService extends CrudService<Curso> {

    List<Curso> obtenerPublicados();

    List<Curso> obtenerPorCategoria(Categoria categoria);

    List<Curso> obtenerPorDocente(Usuario docente);

    List<Curso> buscarPorNombre(String query);
}
