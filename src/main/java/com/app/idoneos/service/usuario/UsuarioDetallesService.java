package com.app.idoneos.service.usuario;

import com.app.idoneos.model.Usuario;
import com.app.idoneos.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/** Servicio de detalles de usuario para Spring Security. Carga el usuario por su nombre de usuario, que es el correo electrónico. */
@Service
public class UsuarioDetallesService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /** Carga el usuario basándose en el nombre de usuario (correo electrónico). */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByCorreo(username)
                .filter(u -> !u.esInactivo())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado o dado de baja"));
        return usuario;
    }
}
