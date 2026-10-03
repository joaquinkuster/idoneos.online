package com.app.idoneos.repositorio;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Descuento;
import com.app.idoneos.modelo.EstadoPago;
import com.app.idoneos.modelo.Inscripcion;
import com.app.idoneos.modelo.MetodoPago;
import com.app.idoneos.modelo.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para acceder y gestionar la entidad {@link Pago} en la base de datos.
 * Extiende de {@link JpaRepository}, proporcionando métodos CRUD básicos para la entidad {@link Pago}.
 */
@Repository
public interface PagoRepositorio extends JpaRepository<Pago, Integer> {

    /**
     * Busca los registros de pago asociados a estadoPago.
     *
     * @param estadoPago el registro de EstadoPago asociado
     * @return una lista de Pago asociados al EstadoPago indicado
     */
    List<Pago> findByEstadoPago(EstadoPago estadoPago);

    /**
     * Busca los registros de pago asociados a metodoPago.
     *
     * @param metodoPago el registro de MetodoPago asociado
     * @return una lista de Pago asociados al MetodoPago indicado
     */
    List<Pago> findByMetodoPago(MetodoPago metodoPago);

    /**
     * Busca los registros de pago asociados a inscripcion.
     *
     * @param inscripcion el registro de Inscripcion asociado
     * @return una lista de Pago asociados al Inscripcion indicado
     */
    List<Pago> findByInscripcion(Inscripcion inscripcion);

    /**
     * Busca los registros de pago asociados a descuento.
     *
     * @param descuento el registro de Descuento asociado
     * @return una lista de Pago asociados al Descuento indicado
     */
    List<Pago> findByDescuento(Descuento descuento);

    /**
     * Busca el pago por su atributo único 'codigoReferencia'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param codigoReferencia el valor de 'codigoReferencia' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<Pago> findByCodigoReferencia(String codigoReferencia);

    /**
     * Busca el pago por su atributo único 'idIntencionExterna'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param idIntencionExterna el valor de 'idIntencionExterna' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<Pago> findByIdIntencionExterna(String idIntencionExterna);

    /**
     * Busca el pago por su atributo único 'idSolicitudPago'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param idSolicitudPago el valor de 'idSolicitudPago' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<Pago> findByIdSolicitudPago(String idSolicitudPago);
}
