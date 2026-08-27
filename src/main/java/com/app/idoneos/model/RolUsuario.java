package com.app.idoneos.model;
import com.app.idoneos.service.modulo_reportes.*;


/**
 * Catálogo genérico de roles del sistema.
 * 
 * Enums de soporte para type-safety en servicios y seguridad.
 * No es una entidad JPA persistida directamente como tabla única.
 */
public enum RolUsuario {
    Administrador,
    Docente,
    Alumno
}

