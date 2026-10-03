package com.app.idoneos.servicio.Comprobante;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Comprobante;
import com.app.idoneos.modelo.Pago;
import com.app.idoneos.repositorio.ComprobanteRepositorio;
import com.app.idoneos.servicio.CrudServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link Comprobante}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link ComprobanteServicio}.
 */
@Service
public class ComprobanteServicioImpl implements ComprobanteServicio, CrudServicio<Comprobante> {

    @Autowired
    private ComprobanteRepositorio comprobanteRepositorio;

    /**
     * Guarda el comprobante en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public Comprobante guardar(Comprobante entidad) {
        return comprobanteRepositorio.save(entidad);
    }

    /**
     * Busca el comprobante por su identificador. Devuelve el registro si existe.
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<Comprobante> buscarPorId(Integer id) {
        return comprobanteRepositorio.findById(id);
    }

    /**
     * Obtiene todos los registros. Devuelve todos los registros.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<Comprobante> obtenerTodo() {
        return comprobanteRepositorio.findAll();
    }

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public Comprobante modificar(Comprobante entidad) {
        return comprobanteRepositorio.save(entidad);
    }

    /**
     * Elimina el registro de la base de datos. Esta entidad no tiene baja lógica.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(Comprobante entidad) {
        comprobanteRepositorio.delete(entidad);
    }

    /**
     * Verifica si existe un registro con el identificador dado.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return comprobanteRepositorio.existsById(id);
    }

    /**
     * Busca los registros de comprobante asociados a pago.
     *
     * @param pago el registro de Pago asociado
     * @return una lista de Comprobante asociados al Pago indicado
     */
    @Override
    public List<Comprobante> buscarPorPago(Pago pago) {
        return comprobanteRepositorio.findByPago(pago);
    }

    /**
     * Busca el comprobante por su atributo único 'numero'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param numero el valor de 'numero' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    @Override
    public Optional<Comprobante> buscarPorNumero(String numero) {
        return comprobanteRepositorio.findByNumero(numero);
    }
}
