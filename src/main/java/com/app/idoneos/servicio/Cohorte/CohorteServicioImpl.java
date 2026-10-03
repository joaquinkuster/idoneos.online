package com.app.idoneos.servicio.Cohorte;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Cohorte;
import com.app.idoneos.modelo.Programa;
import com.app.idoneos.repositorio.CohorteRepositorio;
import com.app.idoneos.servicio.CrudServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link Cohorte}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link CohorteServicio}.
 */
@Service
public class CohorteServicioImpl implements CohorteServicio, CrudServicio<Cohorte> {

    @Autowired
    private CohorteRepositorio cohorteRepositorio;

    /**
     * Guarda la cohorte en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public Cohorte guardar(Cohorte entidad) {
        return cohorteRepositorio.save(entidad);
    }

    /**
     * Busca la cohorte por su identificador. Solo devuelve registros activos (no dados de baja).
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<Cohorte> buscarPorId(Integer id) {
        return cohorteRepositorio.findById(id)
                .filter(entidad -> !entidad.esInactivo()); // Solo devuelve registros activos
    }

    /**
     * Obtiene todos los registros. Solo devuelve registros activos.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<Cohorte> obtenerTodo() {
        return cohorteRepositorio.findByBajaFalse();
    }

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public Cohorte modificar(Cohorte entidad) {
        return cohorteRepositorio.save(entidad);
    }

    /**
     * Da de baja el registro (baja lógica), en lugar de eliminarlo de la base de datos.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(Cohorte entidad) {
        entidad.marcarInactivo(); // Baja lógica
        cohorteRepositorio.save(entidad);
    }

    /**
     * Verifica si existe un registro con el identificador dado y está activo.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return cohorteRepositorio.existsById(id) &&
                buscarPorId(id).isPresent(); // Solo cuenta registros activos
    }

    /**
     * Busca los registros vigentes de cohorte asociados a programa.
     *
     * @param programa el registro de Programa asociado
     * @return una lista de Cohorte asociados al Programa indicado
     */
    @Override
    public List<Cohorte> buscarPorPrograma(Programa programa) {
        return cohorteRepositorio.findByProgramaAndBajaFalse(programa);
    }
}
