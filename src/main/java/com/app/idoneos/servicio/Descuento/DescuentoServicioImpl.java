package com.app.idoneos.servicio.Descuento;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Descuento;
import com.app.idoneos.utilidades.MensajeBajaUtilidad;
import com.app.idoneos.utilidades.TextoUtilidad;
import org.springframework.transaction.annotation.Transactional;
import com.app.idoneos.repositorio.DescuentoRepositorio;
import com.app.idoneos.servicio.CrudServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link Descuento}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link DescuentoServicio}.
 */
@Service
public class DescuentoServicioImpl implements DescuentoServicio, CrudServicio<Descuento> {

    /** Longitud máxima del nombre (la de la columna). */
    private static final int LONGITUD_NOMBRE = 50;

    @Autowired
    private DescuentoRepositorio descuentoRepositorio;

    // ---------------------------------------------------------------- Lecturas

    /**
     * Busca el descuento por su identificador. Solo devuelve registros activos (no dados de baja).
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<Descuento> buscarPorId(Integer id) {
        return descuentoRepositorio.findById(id)
                .filter(entidad -> !entidad.esInactivo()); // Solo devuelve registros activos
    }

    /**
     * Obtiene todos los registros. Solo devuelve registros activos.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<Descuento> obtenerTodo() {
        return descuentoRepositorio.findByBajaFalse();
    }

    /**
     * Verifica si existe un registro con el identificador dado y está activo.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return descuentoRepositorio.existsById(id) &&
                buscarPorId(id).isPresent(); // Solo cuenta registros activos
    }

    /**
     * Busca el descuento por su atributo único 'nombre'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param nombre el valor de 'nombre' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    @Override
    public Optional<Descuento> buscarPorNombre(String nombre) {
        return descuentoRepositorio.findByNombre(nombre);
    }

    @Override
    public List<Descuento> buscarConFiltros(String nombre, String estado, String orden) {
        String criterio = TextoUtilidad.normalizar(nombre);
        LocalDateTime ahora = LocalDateTime.now();
        Comparator<Descuento> criterioOrden;
        if ("recientes".equals(orden)) {
            criterioOrden = Comparator.comparing(Descuento::getFechaCreacion)
                    .thenComparingInt(Descuento::getIdDescuento).reversed();
        } else if ("porcentaje".equals(orden)) {
            criterioOrden = Comparator.comparing(Descuento::getPorcentaje).reversed();
        } else {
            criterioOrden = Comparator.comparing(Descuento::getNombre, String.CASE_INSENSITIVE_ORDER);
        }
        return descuentoRepositorio.findAll().stream()
                .filter(d -> estado == null || estado.isBlank() || d.getEstado(ahora).equalsIgnoreCase(estado))
                .filter(d -> criterio.isEmpty() || TextoUtilidad.normalizar(d.getNombre()).contains(criterio))
                .sorted(Comparator.comparing(Descuento::esInactivo).thenComparing(criterioOrden))
                .toList();
    }

    // ---------------------------------------------------------------- Creaciones

    /**
     * Guarda el descuento en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public Descuento guardar(Descuento entidad) {
        return descuentoRepositorio.save(entidad);
    }

    @Override
    @Transactional
    public Descuento registrarDescuento(String nombre, Float porcentaje, LocalDate vigenciaDesde,
            LocalDate vigenciaHasta, Integer cantidadLimite, Integer cursosRequeridos) {
        String nombreLimpio = validarDatos(nombre, porcentaje, vigenciaDesde, vigenciaHasta, cantidadLimite,
                cursosRequeridos);
        Descuento existente = descuentoRepositorio.findAll().stream()
                .filter(d -> d.getNombre().equalsIgnoreCase(nombreLimpio)).findFirst().orElse(null);
        if (existente != null && !existente.esInactivo()) {
            throw new IllegalArgumentException("Error! Ya existe un descuento con el nombre '" + nombreLimpio + "'.");
        }
        Descuento descuento = existente != null ? existente : new Descuento();
        aplicarDatos(descuento, nombreLimpio, porcentaje, vigenciaDesde, vigenciaHasta, cantidadLimite,
                cursosRequeridos);
        if (existente != null) {
            // Reactiva el descuento dado de baja con el mismo nombre (el nombre es único); nunca se había aplicado
            descuento.setBaja(false);
            descuento.setCantidadUsada(0);
            descuento.setUltimaModificacion(LocalDateTime.now());
        }
        return descuentoRepositorio.save(descuento);
    }

    // ---------------------------------------------------------------- Modificaciones

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public Descuento modificar(Descuento entidad) {
        return descuentoRepositorio.save(entidad);
    }

    @Override
    @Transactional
    public Descuento modificarDescuento(Integer idDescuento, String nombre, Float porcentaje, LocalDate vigenciaDesde,
            LocalDate vigenciaHasta, Integer cantidadLimite, Integer cursosRequeridos) {
        Descuento descuento = descuentoRepositorio.findById(idDescuento).filter(d -> !d.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! El descuento se encuentra dado de baja."));
        String nombreLimpio = validarDatos(nombre, porcentaje, vigenciaDesde, vigenciaHasta, cantidadLimite,
                cursosRequeridos);
        if (cantidadLimite < descuento.getCantidadUsada()) {
            throw new IllegalArgumentException("Error! La cantidad límite no puede ser menor a la cantidad ya usada ("
                    + descuento.getCantidadUsada() + ").");
        }
        for (Descuento otro : descuentoRepositorio.findAll()) {
            if (otro.getIdDescuento() != idDescuento && otro.getNombre().equalsIgnoreCase(nombreLimpio)) {
                throw new IllegalArgumentException(otro.esInactivo()
                        ? "Error! Ya existe un descuento dado de baja con el nombre '" + nombreLimpio + "'."
                        : "Error! El nombre coincide con el de otro descuento.");
            }
        }
        aplicarDatos(descuento, nombreLimpio, porcentaje, vigenciaDesde, vigenciaHasta, cantidadLimite,
                cursosRequeridos);
        descuento.setUltimaModificacion(LocalDateTime.now());
        return descuentoRepositorio.save(descuento);
    }

    // ---------------------------------------------------------------- Bajas

    /**
     * Da de baja el registro (baja lógica), en lugar de eliminarlo de la base de datos.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(Descuento entidad) {
        entidad.marcarInactivo(); // Baja lógica
        descuentoRepositorio.save(entidad);
    }

    @Override
    @Transactional
    public void darDeBajaDescuento(Integer idDescuento) {
        Descuento descuento = descuentoRepositorio.findById(idDescuento).filter(d -> !d.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! El descuento se encuentra dado de baja."));
        if (descuento.getCantidadUsada() > 0) {
            throw new IllegalArgumentException(MensajeBajaUtilidad.bajaBloqueada("el descuento", descuento.getNombre(),
                    "se aplicó a " + MensajeBajaUtilidad.cantidad(descuento.getCantidadUsada(), "inscripción",
                            "inscripciones"),
                    "Un descuento aplicado ya no puede darse de baja; se desactiva solo al vencer o al agotarse."));
        }
        descuento.marcarInactivo();
        descuento.setUltimaModificacion(LocalDateTime.now());
        descuentoRepositorio.save(descuento);
    }

    @Override
    @Transactional
    public void darDeBajaVarios(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("Error! Debe seleccionar al menos un registro.");
        }
        for (Integer id : ids) {
            try {
                darDeBajaDescuento(id);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Error! No se dio de baja ningún registro. "
                        + e.getMessage().replaceFirst("^Error! ", ""));
            }
        }
    }

    /**
     * Valida los datos de un descuento (los mismos en el alta y en la modificación) y devuelve el nombre sin espacios
     * sobrantes.
     */
    private String validarDatos(String nombre, Float porcentaje, LocalDate vigenciaDesde, LocalDate vigenciaHasta,
            Integer cantidadLimite, Integer cursosRequeridos) {
        List<String> faltantes = new ArrayList<>();
        if (nombre == null || nombre.isBlank()) {
            faltantes.add("nombre");
        }
        if (porcentaje == null) {
            faltantes.add("porcentaje");
        }
        if (vigenciaDesde == null) {
            faltantes.add("vigencia desde");
        }
        if (vigenciaHasta == null) {
            faltantes.add("vigencia hasta");
        }
        if (cantidadLimite == null) {
            faltantes.add("cantidad límite");
        }
        if (!faltantes.isEmpty()) {
            throw new IllegalArgumentException("Error! Faltan completar los campos obligatorios: "
                    + String.join(", ", faltantes) + ".");
        }
        String nombreLimpio = nombre.trim();
        if (nombreLimpio.length() > LONGITUD_NOMBRE) {
            throw new IllegalArgumentException("Error! El nombre no puede superar los " + LONGITUD_NOMBRE
                    + " caracteres.");
        }
        if (porcentaje.isNaN() || porcentaje < Descuento.PORCENTAJE_MINIMO || porcentaje > Descuento.PORCENTAJE_MAXIMO) {
            throw new IllegalArgumentException("Error! El porcentaje debe estar entre 1 y 100.");
        }
        if (vigenciaHasta.isBefore(vigenciaDesde)) {
            throw new IllegalArgumentException("Error! La vigencia hasta debe ser posterior a la vigencia desde.");
        }
        if (cantidadLimite <= 0) {
            throw new IllegalArgumentException("Error! La cantidad límite debe ser un número entero mayor a cero.");
        }
        if (cursosRequeridos != null && cursosRequeridos < 0) {
            throw new IllegalArgumentException(
                    "Error! La cantidad de cursos requeridos debe ser un número entero mayor o igual a cero.");
        }
        return nombreLimpio;
    }

    private void aplicarDatos(Descuento descuento, String nombre, Float porcentaje, LocalDate vigenciaDesde,
            LocalDate vigenciaHasta, Integer cantidadLimite, Integer cursosRequeridos) {
        descuento.setNombre(nombre);
        descuento.setPorcentaje(porcentaje);
        descuento.setVigenciaDesde(vigenciaDesde.atStartOfDay());
        descuento.setVigenciaHasta(vigenciaHasta.atTime(23, 59, 59));
        descuento.setCantidadLimite(cantidadLimite);
        descuento.setCursosRequeridos(cursosRequeridos == null ? 0 : cursosRequeridos);
    }
}
