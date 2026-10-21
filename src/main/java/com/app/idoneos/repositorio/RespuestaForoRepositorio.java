package com.app.idoneos.repositorio;

import java.util.List;
import com.app.idoneos.modelo.ConsultaForo;
import com.app.idoneos.modelo.DocenteCurso;
import com.app.idoneos.modelo.RespuestaForo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para acceder y gestionar la entidad {@link RespuestaForo} en la base de datos.
 * Extiende de {@link JpaRepository}, proporcionando métodos CRUD básicos para la entidad {@link RespuestaForo}.
 */
@Repository
public interface RespuestaForoRepositorio extends JpaRepository<RespuestaForo, Integer> {

    /**
     * Encuentra todos los registros activos (no dados de baja).
     *
     * @return Una lista de {@link RespuestaForo} activos.
     */
    List<RespuestaForo> findByBajaFalse();

    /**
     * Busca los registros vigentes de respuestaForo asociados a docenteCurso.
     *
     * @param docenteCurso el registro de DocenteCurso asociado
     * @return una lista de RespuestaForo asociados al DocenteCurso indicado
     */
    List<RespuestaForo> findByDocenteCursoAndBajaFalse(DocenteCurso docenteCurso);

    /**
     * Busca los registros vigentes de respuestaForo asociados a consultaForo.
     *
     * @param consulta el registro de ConsultaForo asociado
     * @return una lista de RespuestaForo asociados al ConsultaForo indicado
     */
    List<RespuestaForo> findByConsultaAndBajaFalse(ConsultaForo consulta);
}
