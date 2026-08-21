package com.app.idoneos.service.modulo_configuracion;
import com.app.idoneos.service.Reportes.*;

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

import java.util.List;
import java.util.Optional;

/**
 * Interfaz genérica que define los métodos básicos de operación para un servicio CRUD
 * (Crear, Leer, Actualizar, Eliminar) en entidades de tipo {@code T}.
 * 
 * @param <T> el tipo de entidad sobre la cual se realizan las operaciones CRUD
 */
public interface CrudService<T> {

    /**
     * Guarda una entidad en el sistema.
     * 
     * @param entidad la entidad a guardar
     * @return la entidad guardada, con su estado actualizado (incluyendo campos generados como IDs)
     */
    T guardar(T entidad);

    /**
     * Encuentra una entidad por su ID.
     * 
     * @param id el ID de la entidad a buscar
     * @return un {@link Optional} que contiene la entidad si se encuentra, o {@link Optional#empty()} si no existe
     */
    Optional<T> buscarPorId(Integer id);

    /**
     * Obtiene todas las entidades almacenadas en el sistema.
     * 
     * @return una lista de todas las entidades
     */
    List<T> obtenerTodo();

    /**
     * Actualiza una entidad existente en el sistema.
     * 
     * @param entidad la entidad con los nuevos datos
     * @return la entidad actualizada
     */
    T modificar(T entidad);

    /**
     * Elimina una entidad en el sistema.
     * 
     * @param entidad la entidad a borrar
     */
    void borrar(T entidad);

    /**
     * Verifica si una entidad existe en el sistema, dado su ID.
     * 
     * @param id el ID de la entidad
     * @return {@code true} si la entidad existe, {@code false} si no existe
     */
    boolean existePorId(Integer id);
}

