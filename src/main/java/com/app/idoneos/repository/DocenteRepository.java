package com.app.idoneos.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.app.idoneos.model.Docente;
import com.app.idoneos.model.RolUsuario;

/** Repositorio de Docente. */
@Repository
public interface DocenteRepository extends JpaRepository<Docente, Integer> {

    /** Encuentra los docentes activos y habilitados: habilitados para dictar. */
    @Query("SELECT d FROM Docente d WHERE d.habilitado = true AND d.usuario.baja = false AND EXISTS "
            + "(SELECT ru FROM RolUsuario ru WHERE ru.usuario = d.usuario AND ru.baja = false AND ru.rol.nombre = 'Docente')")
    List<Docente> buscarPorHabilitado();
}
