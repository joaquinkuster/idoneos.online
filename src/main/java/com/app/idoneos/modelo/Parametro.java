package com.app.idoneos.modelo;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Parámetro de configuración del sistema, definido como un par clave y valor y gestionado por un administrador.
 */
@Entity
@Table(name = "Parametro")
@Getter
@Setter
@NoArgsConstructor
public class Parametro {

    /**
     * Clave del parámetro con la cantidad mínima de unidades con material publicado que debe tener
     * el cronograma de un programa para poder registrar cohortes.
     */
    public static final String MINIMO_UNIDADES_CON_MATERIAL = "programa.minimo_unidades_con_material";

    /** Clave del plazo, en minutos, para modificar una consulta o una respuesta del foro desde su registro. */
    public static final String PLAZO_EDICION_FORO = "foro.plazo_edicion_minutos";

    /** Plazo de edición del foro que se usa cuando el parámetro no está cargado. */
    public static final int PLAZO_EDICION_FORO_POR_DEFECTO = 15;

    /**
     * Clave del plazo, en minutos, que el sistema espera la confirmación de un pago online antes de registrarlo como
     * rechazado. MODO vence las solicitudes de pago entre los 5 y los 10 minutos.
     */
    public static final String PLAZO_CONFIRMACION_PAGO = "pagos.plazo_confirmacion_minutos";

    /** Plazo de confirmación del pago que se usa cuando el parámetro no está cargado (el vencimiento de MODO). */
    public static final int PLAZO_CONFIRMACION_PAGO_POR_DEFECTO = 10;

    /** Clave del CBU de la institución, para las transferencias bancarias. */
    public static final String CBU_INSTITUCION = "pagos.cbu_institucion";

    /**
     * Identificador único del parámetro.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idParametro")
    private int idParametro;

    /**
     * Relación con Administrador.
     */
    @ManyToOne
    @JoinColumn(name = "idAdministrador", nullable = false)
    private Administrador administrador;

    /**
     * Clave del parámetro.
     */
    @Column(name = "clave", nullable = false, length = 100, unique = true)
    private String clave;

    /**
     * Valor del parámetro.
     */
    @Column(name = "valor", nullable = false, columnDefinition = "TEXT")
    private String valor;

    /**
     * Fecha y hora de creación del parámetro. Se establece al momento actual por defecto.
     */
    @Column(name = "fechaCreacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    /**
     * Fecha y hora de la última modificación del registro.
     */
    @Column(name = "ultimaModificacion", nullable = true)
    private LocalDateTime ultimaModificacion;

    /**
     * Constructor para crear el parámetro con los datos básicos.
     *
     * @param administrador relación con Administrador.
     * @param clave clave del parámetro.
     * @param valor valor del parámetro.
     */
    public Parametro(Administrador administrador, String clave, String valor) {
        this.administrador = administrador;
        this.clave = clave;
        this.valor = valor;
    }

    /**
     * Devuelve una representación en forma de cadena del parámetro.
     *
     * @return La representación textual.
     */
    @Override
    public String toString() {
        return clave;
    }
}
