package com.app.idoneos.repositorio;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Recurso;
import com.app.idoneos.modelo.TemaRecurso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para acceder y gestionar la entidad {@link TemaRecurso} en la base de datos.
 */
@Repository
public interface TemaRecursoRepositorio extends JpaRepository<TemaRecurso, Integer> {

    /**
     * Busca los recursos activos de un tema.
     *
     * @param tema el tema
     * @return los recursos activos del tema
     */
    List<TemaRecurso> findByTemaAndBajaFalse(com.app.idoneos.modelo.Tema tema);

    /**
     * Busca los recursos que incorporó una participación docente y siguen activos.
     *
     * @param docenteCurso la participación
     * @return los recursos activos
     */
    List<TemaRecurso> findByDocenteCursoAndBajaFalse(com.app.idoneos.modelo.DocenteCurso docenteCurso);

    /**
     * Busca los usos activos de un recurso.
     *
     * @param recurso el recurso
     * @return los usos activos
     */
    List<TemaRecurso> findByRecursoAndBajaFalse(Recurso recurso);

    /**
     * Busca los registros activos.
     *
     * @return los registros activos
     */
    List<TemaRecurso> findByBajaFalse();
}
