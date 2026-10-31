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
     * Clave del parámetro con la cantidad mínima de temas con material publicado que debe tener
     * el cronograma de un programa para poder registrar cohortes.
     */
    public static final String MINIMO_TEMAS_CON_MATERIAL = "programa.minimo_temas_con_material";

    /**
     * Clave de los días que como mínimo debe haber entre hoy y el inicio de la inscripción de una cohorte pendiente: es el
     * tiempo que tiene el docente para completar el contenido.
     */
    public static final String COHORTE_PLAZO_MINIMO_CONTENIDO_DIAS = "cohorte.plazo_minimo_contenido_dias";

    /** Días de plazo mínimo para completar el contenido cuando el parámetro no está cargado (una semana). */
    public static final int COHORTE_PLAZO_MINIMO_CONTENIDO_DIAS_POR_DEFECTO = 7;

    /** Clave de los días que ocupa en el cronograma un tema nuevo, mientras no se indique otra duración. */
    public static final String CRONOGRAMA_DIAS_POR_TEMA_NUEVO = "cronograma.dias_por_tema_nuevo";

    /** Días que ocupa un tema nuevo cuando el parámetro no está cargado (una semana). */
    public static final int CRONOGRAMA_DIAS_POR_TEMA_NUEVO_POR_DEFECTO = 7;

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
     * Clave de la tolerancia, en segundos, con la que se admite una respuesta o entrega de un intento de autoevaluación que
     * llega después de agotado su tiempo límite (por la latencia entre el navegador y el servidor).
     */
    public static final String GRACIA_ENTREGA_AUTOEVALUACION = "autoevaluacion.gracia_segundos";

    /** Tolerancia de entrega de las autoevaluaciones que se usa cuando el parámetro no está cargado. */
    public static final int GRACIA_ENTREGA_AUTOEVALUACION_POR_DEFECTO = 15;

    // ---- Medio de pago MODO: lo que el cliente reemplaza cuando abre su cuenta de MODO. La semilla (solo desarrollo)
    // ---- carga las credenciales de PRUEBA que MODO publica en su documentación.

    /** Usuario de la cuenta de MODO. */
    public static final String MODO_USUARIO = "modo.usuario";

    /** Contraseña de la cuenta de MODO. Se guarda cifrada (ver {@link #CLAVES_SENSIBLES}). */
    public static final String MODO_CLAVE = "modo.clave";

    /** Código del procesador (gateway) que MODO le asigna al comercio. */
    public static final String MODO_PROCESSOR_CODE = "modo.processor_code";

    /** Código de las condiciones comerciales (cuotas) que se ofrecen, por ejemplo {@code 1CSI}. */
    public static final String MODO_CC_CODE = "modo.cc_code";

    /** Dirección de la API de MODO: preproducción o producción. */
    public static final String MODO_URL_API = "modo.url_api";

    /** Dirección del script que muestra el modal de pago de MODO (cambia entre preproducción y producción). */
    public static final String MODO_URL_SCRIPT_MODAL = "modo.url_script_modal";

    /** Nombre del comercio, que MODO exige en cada pedido. */
    public static final String MODO_NOMBRE_COMERCIO = "modo.nombre_comercio";

    /**
     * Dirección pública del sistema (por ejemplo {@code https://idoneos.online}), a la que MODO puede avisar los cambios
     * de estado de un pago. Vacía mientras el sistema no es accesible desde internet: el pago se confirma consultándolo.
     */
    public static final String MODO_URL_PUBLICA = "modo.url_publica";

    /** Monto mínimo, en pesos, que admite el medio de pago para crear una solicitud. */
    public static final String MODO_MONTO_MINIMO = "modo.monto_minimo";

    /** Monto mínimo que se usa cuando el parámetro no está cargado (el del gateway Decidir). */
    public static final float MODO_MONTO_MINIMO_POR_DEFECTO = 30f;

    /** Claves de los parámetros cuyo valor se guarda cifrado y nunca se muestra completo. */
    public static final java.util.Set<String> CLAVES_SENSIBLES = java.util.Set.of(MODO_CLAVE);

    /** Lo que se muestra en lugar del valor de un parámetro sensible. */
    public static final String VALOR_ENMASCARADO = "••••••••";

    /**
     * Indica si el valor de un parámetro se guarda cifrado.
     *
     * @param clave La clave del parámetro.
     * @return {@code true} si es una clave sensible.
     */
    public static boolean esClaveSensible(String clave) {
        return CLAVES_SENSIBLES.contains(clave);
    }

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
     * Indica si el valor del parámetro se guarda cifrado.
     *
     * @return {@code true} si es un parámetro sensible.
     */
    public boolean esSensible() {
        return esClaveSensible(clave);
    }

    /**
     * Obtiene el valor para mostrarlo en pantalla: el de los parámetros sensibles no se muestra.
     *
     * @return El valor, o {@link #VALOR_ENMASCARADO} si es un parámetro sensible.
     */
    public String getValorEnmascarado() {
        return esSensible() ? VALOR_ENMASCARADO : valor;
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
