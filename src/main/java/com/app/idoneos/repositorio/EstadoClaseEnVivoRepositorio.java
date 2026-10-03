package com.app.idoneos.repositorio;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.EstadoClaseEnVivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para acceder y gestionar la entidad {@link EstadoClaseEnVivo} en la base de datos.
 * Extiende de {@link JpaRepository}, proporcionando métodos CRUD básicos para la entidad {@link EstadoClaseEnVivo}.
 */
@Repository
public interface EstadoClaseEnVivoRepositorio extends JpaRepository<EstadoClaseEnVivo, Integer> {

    /**
     * Busca el estado de la clase en vivo por su atributo único 'nombre'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param nombre el valor de 'nombre' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<EstadoClaseEnVivo> findByNombre(String nombre);
}
