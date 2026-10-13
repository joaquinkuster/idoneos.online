package com.app.idoneos.servicio.Descuento;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Descuento;

/**
 * Servicio para gestionar las operaciones relacionadas con el descuento.
 */
public interface DescuentoServicio {

    /**
     * Busca el descuento por su atributo único 'nombre'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param nombre el valor de 'nombre' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<Descuento> buscarPorNombre(String nombre);

    /**
     * Busca descuentos por nombre y estado (CU-56).
     *
     * @param nombre parte del nombre (opcional)
     * @param estado Vigente, Próximo, Vencido, Agotado o Dado de baja (opcional; vacío para todos)
     * @param orden "nombre" (A–Z, por defecto), "recientes" (más nuevos primero) o "porcentaje" (mayor primero);
     *              los dados de baja van siempre al final
     * @return los descuentos que cumplen los criterios
     */
    List<Descuento> buscarConFiltros(String nombre, String estado, String orden);

    /**
     * Registra un descuento (CU-57), con cantidad usada en cero. Si existe uno dado de baja con el mismo nombre,
     * lo reactiva con los datos nuevos.
     *
     * @param nombre nombre del descuento
     * @param porcentaje porcentaje, entre 1 y 100
     * @param vigenciaDesde primer día de vigencia
     * @param vigenciaHasta último día de vigencia (no anterior al primero)
     * @param cantidadLimite cantidad de veces que puede aplicarse, mayor a cero
     * @param cursosRequeridos cantidad de cursos que el alumno debe haber comprado (opcional, mayor o igual a cero)
     * @return el descuento registrado
     * @throws IllegalArgumentException si falta un dato obligatorio o alguno no es válido
     */
    Descuento registrarDescuento(String nombre, Float porcentaje, LocalDate vigenciaDesde, LocalDate vigenciaHasta,
            Integer cantidadLimite, Integer cursosRequeridos);

    /**
     * Modifica los datos de un descuento que no está dado de baja (CU-58). Los vencidos y agotados pueden
     * modificarse (por ejemplo para extender la vigencia), pero la cantidad límite no puede ser menor a la usada.
     *
     * @param idDescuento identificador del descuento
     * @throws IllegalArgumentException si el descuento está dado de baja o algún dato no es válido
     * @see #registrarDescuento
     */
    Descuento modificarDescuento(Integer idDescuento, String nombre, Float porcentaje, LocalDate vigenciaDesde,
            LocalDate vigenciaHasta, Integer cantidadLimite, Integer cursosRequeridos);

    /**
     * Da de baja un descuento que no está dado de baja y nunca se aplicó (CU-59).
     *
     * @param idDescuento identificador del descuento
     * @throws IllegalArgumentException si ya está dado de baja o se aplicó a alguna inscripción
     */
    void darDeBajaDescuento(Integer idDescuento);

    /**
     * Da de baja varios descuentos a la vez, todos o ninguno.
     *
     * @param ids identificadores de los descuentos
     * @throws IllegalArgumentException si no se indicó ninguno o alguno no puede darse de baja
     */
    void darDeBajaVarios(List<Integer> ids);
}
