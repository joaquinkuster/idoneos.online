package com.app.idoneos.repositorio;

import java.util.List;
import com.app.idoneos.modelo.TerminoGlosario;
import com.app.idoneos.modelo.Tema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para acceder y gestionar la entidad {@link TerminoGlosario} en la base de datos.
 * Extiende de {@link JpaRepository}, proporcionando métodos CRUD básicos para la entidad {@link TerminoGlosario}.
 */
@Repository
public interface TerminoGlosarioRepositorio extends JpaRepository<TerminoGlosario, Integer> {

    /**
     * Encuentra todos los registros activos (no dados de baja).
     *
     * @return Una lista de {@link TerminoGlosario} activos.
     */
    List<TerminoGlosario> findByBajaFalse();

    /**
     * Busca los registros vigentes de terminoGlosario asociados a tema.
     *
     * @param tema el registro de Tema asociado
     * @return una lista de TerminoGlosario asociados al Tema indicado
     */
    List<TerminoGlosario> findByTemaAndBajaFalse(Tema tema);

    /**
     * Busca todos los términos de un tema, incluidos los dados de baja.
     *
     * @param tema el tema del glosario
     * @return la lista de términos
     */
    List<TerminoGlosario> findByTema(Tema tema);
}
