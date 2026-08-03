package com.app.ecomisiones.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.app.ecomisiones.model.RolUsuario;
import com.app.ecomisiones.model.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByCorreoAndBajaFalse(String correo);

    List<Usuario> findByRolAndBajaFalse(RolUsuario rol);

    List<Usuario> findByBajaFalse();

    boolean existsByCorreo(String correo);
}
