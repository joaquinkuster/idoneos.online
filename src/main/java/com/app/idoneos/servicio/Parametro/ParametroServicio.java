package com.app.idoneos.servicio.Parametro;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Administrador;
import com.app.idoneos.modelo.Parametro;

/**
 * Servicio para gestionar las operaciones relacionadas con el parámetro.
 */
public interface ParametroServicio {

    // ---------------------------------------------------------------- Lecturas

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

    /**
     * Obtiene el valor de un parámetro del sistema como texto. Si es un parámetro sensible, lo devuelve descifrado.
     *
     * @param clave la clave del parámetro
     * @return el valor del parámetro, o vacío si no existe
     * @throws IllegalStateException si es un parámetro sensible y no se puede descifrar (cambió la clave de cifrado)
     */
    Optional<String> obtenerTexto(String clave);

    /**
     * Obtiene el valor numérico (con decimales) de un parámetro del sistema.
     *
     * @param clave la clave del parámetro
     * @param valorPorDefecto el valor a devolver si el parámetro no existe o no es un número
     * @return el valor del parámetro
     */
    float obtenerDecimal(String clave, float valorPorDefecto);

    // ---------------------------------------------------------------- Modificaciones

    /**
     * Registra el valor de un parámetro: lo crea si no existe y, si existe, lo reemplaza. Si es un parámetro sensible
     * ({@link Parametro#esClaveSensible}), lo guarda cifrado.
     *
     * @param administrador el administrador que lo registra
     * @param clave la clave del parámetro
     * @param valor el valor (puede ser vacío)
     * @return el parámetro guardado
     * @throws IllegalArgumentException si falta la clave o el valor
     */
    Parametro registrarTexto(Administrador administrador, String clave, String valor);
}
