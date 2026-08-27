package com.app.idoneos.service.modulo_usuarios;
import com.app.idoneos.service.modulo_reportes.*;

import com.app.idoneos.exception.*;
import com.app.idoneos.repository.modulo_cursos.*;
import com.app.idoneos.repository.modulo_gestion_academica.*;
import com.app.idoneos.repository.modulo_inscripciones.*;
import com.app.idoneos.repository.modulo_evaluaciones.*;
import com.app.idoneos.repository.modulo_clases_vivo.*;
import com.app.idoneos.repository.modulo_ia.*;
import com.app.idoneos.repository.modulo_usuarios.*;
import com.app.idoneos.repository.modulo_auditoria.*;
import com.app.idoneos.repository.modulo_reportes.*;
import com.app.idoneos.repository.modulo_configuracion.*;
import com.app.idoneos.service.modulo_configuracion.*;
import com.app.idoneos.service.modulo_cursos.*;
import com.app.idoneos.service.modulo_gestion_academica.*;
import com.app.idoneos.service.modulo_inscripciones.*;
import com.app.idoneos.service.modulo_evaluaciones.*;
import com.app.idoneos.service.modulo_ia.*;
import com.app.idoneos.service.modulo_usuarios.*;

import com.app.idoneos.model.*;

import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Servicio que implementa la interfaz {@link UserDetailsService} para cargar los detalles del usuario
 * basándose en el nombre de usuario (correo electrónico). Este servicio es utilizado por Spring Security
 * para autenticar al usuario y cargar su información.
 */
@Service
public class UsuarioDetallesService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    /**
     * Constructor de la clase {@link UsuarioDetallesService}.
     * 
     * @param usuarioRepository El repositorio que se utilizará para acceder a los datos de usuario.
     */
    public UsuarioDetallesService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Carga el usuario basándose en el nombre de usuario (correo electrónico).
     * 
     * @param username El nombre de usuario (correo electrónico) que se desea autenticar.
     * @return Los detalles del usuario cargado.
     * @throws UsernameNotFoundException Si el usuario no es encontrado o está inactivo.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Buscar el usuario por su correo y asegurarse de que esté activo
        Optional<Usuario> usuario = usuarioRepository.findByCorreoAndBajaFalse(username);
        
        // Si el usuario no se encuentra o está inactivo, lanzar una excepción
        if (!usuario.isPresent()) {
            throw new UsernameNotFoundException("Usuario no encontrado o inactivo");
        }
        
        // Devolver los detalles del usuario encontrado
        return usuario.get();
    }
}

