package com.app.idoneos.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.idoneos.model.Sesion;
import com.app.idoneos.model.Usuario;

/** Repositorio de Sesion. */
@Repository
public interface SesionRepository extends JpaRepository<Sesion, Integer> {

    /** Busca la sesión por su atributo único 'token'. */
    Optional<Sesion> findByToken(String token);

    /** Cuenta las sesiones de un usuario que vencen después del momento dado, es decir las que siguen vigentes. */
    long countByUsuarioAndFechaFinAfter(Usuario usuario, LocalDateTime momento);

    /** Busca las sesiones vigentes de un usuario, de la más reciente a la más antigua. */
    List<Sesion> findByUsuarioAndFechaFinAfterOrderByFechaInicioDesc(Usuario usuario, LocalDateTime momento);

    /** Busca las sesiones vigentes de todos los usuarios, de la más reciente a la más antigua. */
    List<Sesion> findByFechaFinAfterOrderByFechaInicioDesc(LocalDateTime momento);

    /** Borra las sesiones que vencieron antes del momento dado. */
    void deleteByFechaFinBefore(LocalDateTime momento);

    /** Borra todas las sesiones de un usuario. */
    void deleteByUsuario(Usuario usuario);
}
