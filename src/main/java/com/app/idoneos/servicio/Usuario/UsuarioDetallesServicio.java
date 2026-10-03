package com.app.idoneos.servicio.Usuario;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.app.idoneos.modelo.Usuario;
import com.app.idoneos.repositorio.UsuarioRepositorio;

/**
 * Servicio que implementa la interfaz {@link UserDetailsService} para cargar los detalles del usuario
 * basándose en el nombre de usuario (correo electrónico). Es utilizado por Spring Security
 * para autenticar al usuario y cargar su información.
 */
@Service
public class UsuarioDetallesServicio implements UserDetailsService {

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    /**
     * Carga el usuario basándose en el nombre de usuario (correo electrónico).
     *
     * @param username El nombre de usuario (correo electrónico) que se desea autenticar.
     * @return Los detalles del usuario cargado.
     * @throws UsernameNotFoundException Si el usuario no es encontrado o está dado de baja.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepositorio.findByCorreo(username)
                .filter(u -> !u.esInactivo())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado o dado de baja"));
        return usuario;
    }
}
