package com.app.idoneos.controller.modulo_usuarios;

import com.app.idoneos.model.Usuario;
import com.app.idoneos.repository.modulo_configuracion.ConfiguracionRepository;
import com.app.idoneos.repository.modulo_cursos.CategoriaRepository;
import com.app.idoneos.repository.modulo_cursos.CursoRepository;
import com.app.idoneos.repository.modulo_gestion_academica.UnidadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Advice Global para garantizar que el objeto Usuario, datos de navegación y catálogos clave
 * estén siempre disponibles en el Model de las 100 plantillas Thymeleaf, evitando NullPointerExceptions.
 */
@ControllerAdvice
public class GlobalModelAdvice {

    @Autowired private CategoriaRepository categoriaRepository;
    @Autowired private CursoRepository cursoRepository;
    @Autowired private UnidadRepository unidadRepository;
    @Autowired private ConfiguracionRepository configuracionRepository;

    @ModelAttribute("usuario")
    public Usuario populateUsuario(Authentication auth) {
        if (auth != null && auth.getPrincipal() instanceof Usuario) {
            return (Usuario) auth.getPrincipal();
        }
        return null;
    }

    @ModelAttribute("categoriasGlobal")
    public Object populateCategorias() {
        return categoriaRepository.findByBajaFalse();
    }

    @ModelAttribute("cursosGlobal")
    public Object populateCursos() {
        return cursoRepository.findByBajaFalse();
    }
}
