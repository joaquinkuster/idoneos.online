package com.app.idoneos.repository.modulo_usuarios;
import com.app.idoneos.service.Reportes.*;

import com.app.idoneos.model.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocenteRepository extends JpaRepository<Docente, Integer> {

    /** Docentes con usuario activo — para listar en admin (CU-72). */
    @Query("SELECT d FROM Docente d WHERE d.usuario.baja = false")
    List<Docente> findActivos();

    /** Docentes habilitados para Clon IA (habilitado = true y usuario no dado de baja). */
    @Query("SELECT d FROM Docente d WHERE d.habilitado = true AND d.usuario.baja = false")
    List<Docente> findHabilitadosParaClonIA();
}

