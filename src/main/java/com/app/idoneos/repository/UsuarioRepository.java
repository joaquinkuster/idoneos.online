package com.app.idoneos.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.app.idoneos.model.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByCorreoAndBajaFalse(String correo);

    /**
     * Buscar usuarios por rol derivado del subtipo JPA.
     * Como 'rol' es @Transient (se infiere del subtipo Alumno/Docente/Administrador),
     * se usa la relación inversa para filtrar por existencia del subtipo.
     * Usado en UsuarioServiceImpl para listar por rol.
     */
    @Query("SELECT u FROM Usuario u WHERE u.baja = false AND EXISTS (SELECT a FROM Alumno a WHERE a.usuario = u)")
    List<Usuario> findAlumnosActivos();

    @Query("SELECT u FROM Usuario u WHERE u.baja = false AND EXISTS (SELECT d FROM Docente d WHERE d.usuario = u)")
    List<Usuario> findDocentesActivos();

    @Query("SELECT u FROM Usuario u WHERE u.baja = false AND EXISTS (SELECT ad FROM Administrador ad WHERE ad.usuario = u)")
    List<Usuario> findAdministradoresActivos();

    List<Usuario> findByBajaFalse();

    boolean existsByCorreo(String correo);
}

