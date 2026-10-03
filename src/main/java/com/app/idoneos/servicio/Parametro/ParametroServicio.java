package com.app.idoneos.servicio.Parametro;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Administrador;
import com.app.idoneos.modelo.Parametro;

/**
 * Servicio para gestionar las operaciones relacionadas con el parámetro.
 */
public interface ParametroServicio {

    /**
     * Busca los registros de parametro asociados a administrador.
     *
     * @param administrador el registro de Administrador asociado
     * @return una lista de Parametro asociados al Administrador indicado
     */
    List<Parametro> buscarPorAdministrador(Administrador administrador);

    /**
     * Busca el parámetro por su atributo único 'clave'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param clave el valor de 'clave' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<Parametro> buscarPorClave(String clave);

    /**
     * Obtiene el valor entero de un parámetro del sistema.
     *
     * @param clave la clave del parámetro
     * @param valorPorDefecto el valor a devolver si el parámetro no existe o no es un entero
     * @return el valor entero del parámetro
     */
    int obtenerEntero(String clave, int valorPorDefecto);
}
