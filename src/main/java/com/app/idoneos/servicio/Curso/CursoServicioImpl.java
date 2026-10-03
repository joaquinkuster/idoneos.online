package com.app.idoneos.servicio.Curso;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Categoria;
import com.app.idoneos.modelo.Curso;
import com.app.idoneos.modelo.Nivel;
import com.app.idoneos.repositorio.CursoRepositorio;
import com.app.idoneos.servicio.CrudServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link Curso}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link CursoServicio}.
 */
@Service
public class CursoServicioImpl implements CursoServicio, CrudServicio<Curso> {

    @Autowired
    private CursoRepositorio cursoRepositorio;

    /**
     * Guarda el curso en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public Curso guardar(Curso entidad) {
        return cursoRepositorio.save(entidad);
    }

    /**
     * Busca el curso por su identificador. Solo devuelve registros activos (no dados de baja).
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<Curso> buscarPorId(Integer id) {
        return cursoRepositorio.findById(id)
                .filter(entidad -> !entidad.esInactivo()); // Solo devuelve registros activos
    }

    /**
     * Obtiene todos los registros. Solo devuelve registros activos.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<Curso> obtenerTodo() {
        return cursoRepositorio.findByBajaFalse();
    }

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public Curso modificar(Curso entidad) {
        return cursoRepositorio.save(entidad);
    }

    /**
     * Da de baja el registro (baja lógica), en lugar de eliminarlo de la base de datos.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(Curso entidad) {
        entidad.marcarInactivo(); // Baja lógica
        cursoRepositorio.save(entidad);
    }

    /**
     * Verifica si existe un registro con el identificador dado y está activo.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return cursoRepositorio.existsById(id) &&
                buscarPorId(id).isPresent(); // Solo cuenta registros activos
    }

    /**
     * Busca los registros vigentes de curso asociados a nivel.
     *
     * @param nivel el registro de Nivel asociado
     * @return una lista de Curso asociados al Nivel indicado
     */
    @Override
    public List<Curso> buscarPorNivel(Nivel nivel) {
        return cursoRepositorio.findByNivelAndBajaFalse(nivel);
    }

    /**
     * Busca los registros vigentes de curso asociados a categoria.
     *
     * @param categoria el registro de Categoria asociado
     * @return una lista de Curso asociados al Categoria indicado
     */
    @Override
    public List<Curso> buscarPorCategoria(Categoria categoria) {
        return cursoRepositorio.findByCategoriaAndBajaFalse(categoria);
    }

    /**
     * Busca el curso por su atributo único 'nombre'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param nombre el valor de 'nombre' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    @Override
    public Optional<Curso> buscarPorNombre(String nombre) {
        return cursoRepositorio.findByNombre(nombre);
    }
}
