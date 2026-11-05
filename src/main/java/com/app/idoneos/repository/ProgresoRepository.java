package com.app.idoneos.repository;

import com.app.idoneos.model.Inscripcion;
import com.app.idoneos.model.Progreso;
import com.app.idoneos.model.Tema;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para acceder y gestionar la entidad {@link Progreso} en la base de datos.
 * Extiende de {@link JpaRepository}, proporcionando métodos CRUD básicos para la entidad {@link Progreso}.
 */
@Repository
public interface ProgresoRepository extends JpaRepository<Progreso, Integer> {

    /**
     * Busca los registros de progreso asociados a tema.
     *
     * @param tema el registro de Tema asociado
     * @return una lista de Progreso asociados al Tema indicado
     */
    List<Progreso> findByTema(Tema tema);

    /**
     * Busca los registros de progreso asociados a inscripcion.
     *
     * @param inscripcion el registro de Inscripcion asociado
     * @return una lista de Progreso asociados al Inscripcion indicado
     */
    List<Progreso> findByInscripcion(Inscripcion inscripcion);

    /**
     * Busca el progreso a partir de la combinación única de sus relaciones. Incluye los registros dados de baja.
     *
     * @param inscripcion el registro de Inscripcion asociado
     * @param tema el registro de Tema asociado
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<Progreso> findByInscripcionAndTema(Inscripcion inscripcion, Tema tema);
}
