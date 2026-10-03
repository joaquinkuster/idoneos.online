package com.app.idoneos.repositorio;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Administrador;
import com.app.idoneos.modelo.Parametro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para acceder y gestionar la entidad {@link Parametro} en la base de datos.
 * Extiende de {@link JpaRepository}, proporcionando métodos CRUD básicos para la entidad {@link Parametro}.
 */
@Repository
public interface ParametroRepositorio extends JpaRepository<Parametro, Integer> {

    /**
     * Busca los registros de parametro asociados a administrador.
     *
     * @param administrador el registro de Administrador asociado
     * @return una lista de Parametro asociados al Administrador indicado
     */
    List<Parametro> findByAdministrador(Administrador administrador);

    /**
     * Busca el parámetro por su atributo único 'clave'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param clave el valor de 'clave' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<Parametro> findByClave(String clave);
}
