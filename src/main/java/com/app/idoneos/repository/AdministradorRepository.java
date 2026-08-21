package com.app.idoneos.repository;

import com.app.idoneos.model.Administrador;
import com.app.idoneos.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdministradorRepository extends JpaRepository<Administrador, Integer> {
    Optional<Administrador> findByUsuario(Usuario usuario);
}
