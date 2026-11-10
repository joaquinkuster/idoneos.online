package com.app.idoneos.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.annotations.Check;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entidad que representa un descuento porcentual por cantidad de cursos, con vigencia y límite de usos. */
@Entity
@Table(name = "Descuento")
@Check(name = "ck_descuento_nombre", constraints = "trim(nombre) <> ''")
@Check(name = "ck_descuento_porcentaje", constraints = "porcentaje >= 1 and porcentaje <= 99")
@Check(name = "ck_descuento_vigencia", constraints = "vigencia_hasta > vigencia_desde")
@Check(name = "ck_descuento_limite", constraints = "cantidad_limite > 0")
@Check(name = "ck_descuento_cursos", constraints = "cursos_requeridos >= 0")
@Check(name = "ck_descuento_uso", constraints = "cantidad_usada >= 0 and cantidad_usada <= cantidad_limite")
@Getter
@Setter
@NoArgsConstructor
public class Descuento {

    /** Porcentaje mínimo permitido. */
    public static final float PORCENTAJE_MINIMO = 1;

    /** Porcentaje máximo permitido: un descuento no puede cubrir todo el precio (no hay descuentos del 100%). */
    public static final float PORCENTAJE_MAXIMO = 99;

    /** Estados que se derivan de la vigencia y de la cantidad de usos (el de baja se guarda aparte). */
    public static final String ESTADO_VIGENTE = "Vigente";
    public static final String ESTADO_PROXIMO = "Próximo";
    public static final String ESTADO_VENCIDO = "Vencido";
    public static final String ESTADO_AGOTADO = "Agotado";
    public static final String ESTADO_DADO_DE_BAJA = "Dado de baja";

    /** Identificador único del descuento. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idDescuento")
    private int idDescuento;

    /** Nombre del descuento. */
    @Column(name = "nombre", nullable = false, length = 50, unique = true)
    private String nombre;

    /** Cantidad de cursos que el alumno debe haber comprado (inscripciones habilitadas con pago aprobado) para acceder al descuento. */
    @Column(name = "cursosRequeridos", nullable = false)
    private int cursosRequeridos;

    /** Porcentaje de descuento (1-100). */
    @Column(name = "porcentaje", nullable = false)
    private float porcentaje;

    /** Fecha y hora desde la que el descuento está vigente. */
    @Column(name = "vigenciaDesde", nullable = false)
    private LocalDateTime vigenciaDesde;

    /** Fecha y hora hasta la que el descuento está vigente. */
    @Column(name = "vigenciaHasta", nullable = false)
    private LocalDateTime vigenciaHasta;

    /** Cantidad máxima de veces que puede aplicarse el descuento. */
    @Column(name = "cantidadLimite", nullable = false)
    private int cantidadLimite;

    /** Calcula la cantidad de veces que todavía puede aplicarse el descuento. */
    @Column(name = "cantidadUsada", nullable = false)
    private int cantidadUsada;

    /** Fecha y hora de creación del registro. */
    @Column(name = "fechaCreacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    /** Fecha y hora de la última modificación del registro. */
    @Column(name = "ultimaModificacion", nullable = true)
    private LocalDateTime ultimaModificacion;

    /** Indica si el registro fue dado de baja (baja lógica). */
    @Column(name = "baja", nullable = false)
    private Boolean baja = false;

    /** Pagos que referencian a este registro. */
    @OneToMany(mappedBy = "descuento", cascade = CascadeType.ALL)
    private Set<Pago> pagos = new HashSet<>();

    /** Crea el descuento con los datos básicos. */
    public Descuento(String nombre, int cursosRequeridos, float porcentaje, LocalDateTime vigenciaDesde, LocalDateTime vigenciaHasta, int cantidadLimite, int cantidadUsada) {
        this.nombre = nombre;
        this.cursosRequeridos = cursosRequeridos;
        this.porcentaje = porcentaje;
        this.vigenciaDesde = vigenciaDesde;
        this.vigenciaHasta = vigenciaHasta;
        this.cantidadLimite = cantidadLimite;
        this.cantidadUsada = cantidadUsada;
    }

    /** Marca el registro como dado de baja (baja lógica), estableciendo el atributo 'baja' a true. */
    public void marcarInactivo() {
        baja = true;
    }

    /** Verifica si el registro está dado de baja. */
    public boolean esInactivo() {
        return baja;
    }

    /** Indica si ya se aplicó el descuento tantas veces como permite su cantidad límite. */
    public boolean estaAgotado() {
        return cantidadUsada >= cantidadLimite;
    }

    /** Indica si la vigencia del descuento ya terminó. */
    public boolean estaVencido(LocalDateTime ahora) {
        return vigenciaHasta.isBefore(ahora);
    }

    /** Indica si el descuento puede aplicarse en el momento dado: no está dado de baja. */
    public boolean estaVigente(LocalDateTime ahora) {
        return !baja && !estaAgotado() && !estaVencido(ahora) && !vigenciaDesde.isAfter(ahora);
    }

    /** Obtiene el estado del descuento: Dado de baja, Agotado, Vencido, Próximo (todavía no comenzó su vigencia) o Vigente. */
    public String getEstado(LocalDateTime ahora) {
        if (baja) {
            return ESTADO_DADO_DE_BAJA;
        }
        if (estaAgotado()) {
            return ESTADO_AGOTADO;
        }
        if (estaVencido(ahora)) {
            return ESTADO_VENCIDO;
        }
        return vigenciaDesde.isAfter(ahora) ? ESTADO_PROXIMO : ESTADO_VIGENTE;
    }

    /** Obtiene el estado del descuento en este momento. */
    public String getEstado() {
        return getEstado(LocalDateTime.now());
    }

    /** Calcula el monto a pagar de un curso con este descuento aplicado, redondeado a centavos. */
    public float aplicarA(float precio) {
        return java.math.BigDecimal.valueOf(precio).multiply(java.math.BigDecimal.valueOf(100 - porcentaje))
                .divide(java.math.BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP)
                .max(java.math.BigDecimal.ZERO).floatValue();
    }

    /** Cantidad de veces que todavía puede aplicarse el descuento. */
    public int getCantidadDisponible() {
        return Math.max(0, cantidadLimite - cantidadUsada);
    }

    /** Obtiene el porcentaje para mostrar, sin decimales cuando es un número entero (20, no 20.0). */
    public String getPorcentajeTexto() {
        return porcentaje == Math.rint(porcentaje) ? String.valueOf((int) porcentaje) : String.valueOf(porcentaje);
    }

    /** Arma el resumen del descuento para mostrar en las cards de los cursos: su nombre, el porcentaje y, si tiene condición, cuántos cursos comprados exige. */
    public String getResumen() {
        String condicion = cursosRequeridos <= 0 ? ""
                : " (con " + cursosRequeridos + (cursosRequeridos == 1 ? " curso comprado)" : " cursos comprados)");
        return nombre + ": " + getPorcentajeTexto() + "%" + condicion;
    }

    /** Devuelve una representación en forma de cadena del descuento. */
    @Override
    public String toString() {
        return nombre;
    }
}
