package com.app.idoneos.servicio.Parametro;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Administrador;
import com.app.idoneos.modelo.Parametro;
import com.app.idoneos.repositorio.ParametroRepositorio;
import com.app.idoneos.servicio.CrudServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link Parametro}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link ParametroServicio}.
 */
@Service
public class ParametroServicioImpl implements ParametroServicio, CrudServicio<Parametro> {

    @Autowired
    private ParametroRepositorio parametroRepositorio;

    /**
     * Guarda el parámetro en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public Parametro guardar(Parametro entidad) {
        return parametroRepositorio.save(entidad);
    }

    /**
     * Busca el parámetro por su identificador. Devuelve el registro si existe.
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<Parametro> buscarPorId(Integer id) {
        return parametroRepositorio.findById(id);
    }

    /**
     * Obtiene todos los registros. Devuelve todos los registros.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<Parametro> obtenerTodo() {
        return parametroRepositorio.findAll();
    }

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public Parametro modificar(Parametro entidad) {
        return parametroRepositorio.save(entidad);
    }

    /**
     * Elimina el registro de la base de datos. Esta entidad no tiene baja lógica.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(Parametro entidad) {
        parametroRepositorio.delete(entidad);
    }

    /**
     * Verifica si existe un registro con el identificador dado.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return parametroRepositorio.existsById(id);
    }

    /**
     * Busca los registros de parametro asociados a administrador.
     *
     * @param administrador el registro de Administrador asociado
     * @return una lista de Parametro asociados al Administrador indicado
     */
    @Override
    public List<Parametro> buscarPorAdministrador(Administrador administrador) {
        return parametroRepositorio.findByAdministrador(administrador);
    }

    /**
     * Busca el parámetro por su atributo único 'clave'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param clave el valor de 'clave' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    @Override
    public Optional<Parametro> buscarPorClave(String clave) {
        return parametroRepositorio.findByClave(clave);
    }
}
