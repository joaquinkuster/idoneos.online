package com.app.idoneos.service.parametro;

import com.app.idoneos.service.CrudService;

import com.app.idoneos.model.Administrador;
import com.app.idoneos.model.Parametro;
import java.util.Optional;

/**
 * Servicio para gestionar las operaciones relacionadas con el parámetro.
 */
public interface ParametroService extends CrudService<Parametro> {

    // ---------------------------------------------------------------- Lecturas


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
