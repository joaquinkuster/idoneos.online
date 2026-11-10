package com.app.idoneos.repository;

import com.app.idoneos.model.EstadoPago;
import com.app.idoneos.model.Inscripcion;
import com.app.idoneos.model.MetodoPago;
import com.app.idoneos.model.Pago;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repositorio de Pago. */
@Repository
public interface PagoRepository extends JpaRepository<Pago, Integer> {

    /** Busca los registros de pago asociados a estadoPago. */
    List<Pago> findByEstadoPago(EstadoPago estadoPago);

    /** Busca los registros de pago asociados a inscripcion. */
    List<Pago> findByInscripcion(Inscripcion inscripcion);

    /** Busca el pago por su atributo único 'codigoReferencia'. */
    Optional<Pago> findByCodigoReferencia(String codigoReferencia);

    /** Busca el pago con ese número de comprobante entre los que se hicieron con el mismo medio de pago. */
    Optional<Pago> findFirstByNumeroComprobanteAndMetodoPago(String numeroComprobante, com.app.idoneos.model.MetodoPago metodoPago);

    /** Busca el pago por su atributo único 'idIntencionExterna'. */
    Optional<Pago> findByIdIntencionExterna(String idIntencionExterna);

    /** Busca el pago por su atributo único 'idSolicitudPago'. */
    Optional<Pago> findByIdSolicitudPago(String idSolicitudPago);
}
