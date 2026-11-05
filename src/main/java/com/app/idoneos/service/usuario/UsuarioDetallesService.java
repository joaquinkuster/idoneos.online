package com.app.idoneos.service.usuario;

import com.app.idoneos.model.Usuario;
import com.app.idoneos.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Servicio que implementa la interfaz {@link UserDetailsService} para cargar los detalles del usuario
 * basándose en el nombre de usuario (correo electrónico). Es utilizado por Spring Security
 * para autenticar al usuario y cargar su información.
 */
@Service
public class UsuarioDetallesService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // ---------------------------------------------------------------- Lecturas

    /**
     * Carga el usuario basándose en el nombre de usuario (correo electrónico).
     *
     * @param username El nombre de usuario (correo electrónico) que se desea autenticar.
     * @return Los detalles del usuario cargado.
     * @throws UsernameNotFoundException Si el usuario no es encontrado o está dado de baja.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByCorreo(username)
                .filter(u -> !u.esInactivo())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado o dado de baja"));
        return usuario;
    }
}
