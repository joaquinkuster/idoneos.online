package com.app.idoneos.servicio.TerminoGlosario;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.TerminoGlosario;
import com.app.idoneos.modelo.Unidad;
import com.app.idoneos.repositorio.TerminoGlosarioRepositorio;
import com.app.idoneos.servicio.CrudServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link TerminoGlosario}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link TerminoGlosarioServicio}.
 */
@Service
public class TerminoGlosarioServicioImpl implements TerminoGlosarioServicio, CrudServicio<TerminoGlosario> {

    @Autowired
    private TerminoGlosarioRepositorio terminoGlosarioRepositorio;

    /**
     * Guarda el término del glosario en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public TerminoGlosario guardar(TerminoGlosario entidad) {
        return terminoGlosarioRepositorio.save(entidad);
    }

    /**
     * Busca el término del glosario por su identificador. Solo devuelve registros activos (no dados de baja).
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<TerminoGlosario> buscarPorId(Integer id) {
        return terminoGlosarioRepositorio.findById(id)
                .filter(entidad -> !entidad.esInactivo()); // Solo devuelve registros activos
    }

    /**
     * Obtiene todos los registros. Solo devuelve registros activos.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<TerminoGlosario> obtenerTodo() {
        return terminoGlosarioRepositorio.findByBajaFalse();
    }

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public TerminoGlosario modificar(TerminoGlosario entidad) {
        return terminoGlosarioRepositorio.save(entidad);
    }

    /**
     * Da de baja el registro (baja lógica), en lugar de eliminarlo de la base de datos.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(TerminoGlosario entidad) {
        entidad.marcarInactivo(); // Baja lógica
        terminoGlosarioRepositorio.save(entidad);
    }

    /**
     * Verifica si existe un registro con el identificador dado y está activo.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return terminoGlosarioRepositorio.existsById(id) &&
                buscarPorId(id).isPresent(); // Solo cuenta registros activos
    }

    /**
     * Busca los registros vigentes de terminoGlosario asociados a unidad.
     *
     * @param unidad el registro de Unidad asociado
     * @return una lista de TerminoGlosario asociados al Unidad indicado
     */
    @Override
    public List<TerminoGlosario> buscarPorUnidad(Unidad unidad) {
        return terminoGlosarioRepositorio.findByUnidadAndBajaFalse(unidad);
    }
}
