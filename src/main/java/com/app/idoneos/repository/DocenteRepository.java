package com.app.idoneos.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.app.idoneos.model.Docente;
import com.app.idoneos.model.RolUsuario;

/**
 * Repositorio para acceder y gestionar la entidad {@link Docente} en la base de datos.
 * Extiende de {@link JpaRepository}, proporcionando métodos CRUD básicos para la entidad {@link Docente}.
 */
@Repository
public interface DocenteRepository extends JpaRepository<Docente, Integer> {





    /**
     * Encuentra los docentes activos y habilitados: habilitados para dictar, con su usuario vigente
     * y con el rol de docente vigente.
     *
     * @return Una lista de {@link Docente} habilitados.
     */
    @Query("SELECT d FROM Docente d WHERE d.habilitado = true AND d.usuario.baja = false AND EXISTS "
            + "(SELECT ru FROM RolUsuario ru WHERE ru.usuario = d.usuario AND ru.baja = false AND ru.rol.nombre = 'Docente')")
    List<Docente> buscarPorHabilitado();
}
