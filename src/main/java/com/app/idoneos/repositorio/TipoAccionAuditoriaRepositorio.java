package com.app.idoneos.repositorio;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.TipoAccionAuditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para acceder y gestionar la entidad {@link TipoAccionAuditoria} en la base de datos.
 * Extiende de {@link JpaRepository}, proporcionando métodos CRUD básicos para la entidad {@link TipoAccionAuditoria}.
 */
@Repository
public interface TipoAccionAuditoriaRepositorio extends JpaRepository<TipoAccionAuditoria, Integer> {

    /**
     * Busca el tipo de acción auditada por su atributo único 'nombre'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param nombre el valor de 'nombre' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<TipoAccionAuditoria> findByNombre(String nombre);
}
