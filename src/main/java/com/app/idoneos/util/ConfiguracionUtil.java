package com.app.idoneos.util;

import java.util.List;

import com.app.idoneos.model.Parametro;

/** Catálogo de los parámetros que el administrador puede configurar: el conjunto de claves está definido de antemano y no se puede ampliar desde la pantalla. */
public final class ConfiguracionUtil {

    private ConfiguracionUtil() {
    }

    /** Tipo de dato de un parámetro. */
    public enum Tipo {
        ENTERO, DECIMAL, TEXTO, CORREO, SECRETO, IMAGEN
    }

    /**
     * Definición de un parámetro configurable.
     *
     * @param clave clave del parámetro
     * @param etiqueta nombre que ve el administrador
     * @param ayuda aclaración debajo del campo
     * @param tipo tipo de dato
     * @param unidad unidad del valor (minutos, días...), o vacío
     * @param minimo valor mínimo (solo numéricos)
     * @param maximo valor máximo (solo numéricos)
     * @param largo largo máximo (solo texto)
     * @param obligatorio si el valor debe completarse (los secretos ya guardados se conservan si se dejan vacíos)
     */
    public record Definicion(String clave, String etiqueta, String ayuda, Tipo tipo, String unidad, int minimo, int maximo, int largo, boolean obligatorio) {

        /** Indica si el campo es numérico. */
        public boolean esNumerico() {
            return tipo == Tipo.ENTERO || tipo == Tipo.DECIMAL;
        }
    }

    /** Sección del formulario de configuración. */
    public record Seccion(String titulo, String descripcion, List<Definicion> parametros) {
    }

    private static Definicion entero(String clave, String etiqueta, String ayuda, String unidad, int minimo, int maximo) {
        return new Definicion(clave, etiqueta, ayuda, Tipo.ENTERO, unidad, minimo, maximo, 0, true);
    }

    private static Definicion texto(String clave, String etiqueta, String ayuda, int largo, boolean obligatorio) {
        return new Definicion(clave, etiqueta, ayuda, Tipo.TEXTO, "", 0, 0, largo, obligatorio);
    }

    private static Definicion secreto(String clave, String etiqueta, String ayuda) {
        return new Definicion(clave, etiqueta, ayuda, Tipo.SECRETO, "", 0, 0, 255, false);
    }

    /** Todos los parámetros del sistema, agrupados por sección. */
    public static final List<Seccion> SECCIONES = List.of(
            new Seccion("Datos institucionales", "Se usan en el sitio, los comprobantes de pago y las constancias.", List.of(
                    texto(Parametro.INSTITUCION_RAZON_SOCIAL, "Razón social", "", 100, true),
                    texto(Parametro.INSTITUCION_CUIT, "CUIT", "Con el formato 30-12345678-9.", 13, true),
                    texto(Parametro.INSTITUCION_DOMICILIO, "Domicilio", "", 150, true),
                    new Definicion(Parametro.INSTITUCION_LOGO, "Logo", "JPG, PNG o WebP, hasta 2 MB.", Tipo.IMAGEN, "", 0, 0, 255, false),
                    new Definicion(Parametro.INSTITUCION_EMAIL, "Correo de contacto", "", Tipo.CORREO, "", 0, 0, 100, true),
                    texto(Parametro.INSTITUCION_TELEFONO, "Teléfono de contacto", "", 30, true),
                    texto(Parametro.CBU_INSTITUCION, "CBU bancario", "Cuenta que reciben los alumnos para pagar por transferencia (22 dígitos).", 22, true))),
            new Seccion("Sesiones", "Control de los dispositivos con la sesión iniciada.", List.of(
                    entero(Parametro.LIMITE_SESIONES_CONCURRENTES, "Sesiones concurrentes por usuario", "Cantidad máxima de dispositivos con sesión iniciada a la vez.", "sesiones", 1, 20))),
            new Seccion("Pagos", "Plazos y montos del cobro online.", List.of(
                    entero(Parametro.PLAZO_CONFIRMACION_PAGO, "Espera de confirmación de un pago", "Tiempo que se espera la confirmación antes de registrar el pago como rechazado.", "minutos", 1, 1440),
                    new Definicion(Parametro.MODO_MONTO_MINIMO, "Monto mínimo de un pago", "Monto mínimo que admite el medio de pago.", Tipo.ENTERO, "pesos", 1, 1_000_000, 0, true))),
            new Seccion("Clases en vivo y grabaciones", "Horarios de las transmisiones y disponibilidad de las grabaciones.", List.of(
                    entero(Parametro.CLASE_ANTICIPACION_MINUTOS, "Anticipación para iniciar una clase", "Cuánto antes de su horario puede el docente iniciar la clase.", "minutos", 0, 240),
                    entero(Parametro.CLASE_TOLERANCIA_MINUTOS, "Espera del inicio de una clase", "Tiempo que se espera una clase programada antes de cancelarla sola.", "minutos", 1, 240),
                    entero(Parametro.CLASE_CORTE_SENAL_MINUTOS, "Corte por falta de señal", "Minutos sin señal después de los cuales la clase se cierra sola.", "minutos", 1, 120),
                    entero(Parametro.CLASE_DURACION_MAXIMA_HORAS, "Duración máxima de una clase", "", "horas", 1, 12),
                    entero(Parametro.CLASE_RETENCION_GRABACION_MESES, "Disponibilidad de las grabaciones", "Tiempo que una grabación queda disponible para los alumnos.", "meses", 1, 60),
                    entero(Parametro.CLASE_AVISO_VENCIMIENTO_DIAS, "Aviso previo al vencimiento de una grabación", "Días de anticipación con que se avisa a los alumnos.", "días", 1, 90))),
            new Seccion("Cursos, cohortes y evaluaciones", "Reglas de armado del dictado.", List.of(
                    entero(Parametro.MINIMO_TEMAS_CON_MATERIAL, "Unidades mínimas por programa", "Temas con material publicado que necesita un programa para registrar una cohorte.", "temas", 1, 100),
                    entero(Parametro.COHORTE_PLAZO_MINIMO_CONTENIDO_DIAS, "Plazo mínimo para completar el contenido", "Días entre hoy y el inicio de la inscripción de una cohorte pendiente.", "días", 1, 365),
                    entero(Parametro.COHORTE_RECORDATORIO_DIAS_ANTICIPACION, "Anticipación del recordatorio de cohorte", "Días antes con que se avisa a quien marcó «Recordarme».", "días", 1, 90),
                    entero(Parametro.CRONOGRAMA_DIAS_POR_TEMA_NUEVO, "Duración de un tema nuevo", "Días que ocupa en el cronograma un tema nuevo.", "días", 1, 90),
                    entero(Parametro.GRACIA_ENTREGA_AUTOEVALUACION, "Tolerancia de entrega de una autoevaluación", "", "segundos", 0, 600),
                    entero(Parametro.PLAZO_EDICION_FORO, "Edición de consultas y respuestas del foro", "Tiempo límite para modificar desde su registro.", "minutos", 1, 1440))),
            new Seccion("Generación con IA", "Bancos de preguntas, resúmenes y presentaciones.", List.of(
                    entero(Parametro.IA_CANTIDAD_PREGUNTAS_BANCO, "Preguntas por banco", "", "preguntas", 1, 100),
                    entero(Parametro.IA_PROPORCION_OPCION_MULTIPLE, "Proporción de preguntas de opción múltiple", "Del resto, la mitad son de opción única y la otra mitad de verdadero o falso.", "%", 0, 100),
                    entero(Parametro.TOPE_SEMANAL_CONTENIDOS_IA, "Contenidos con IA por docente y semana", "Bancos de preguntas, resúmenes y presentaciones.", "contenidos", 1, 1000),
                    entero(Parametro.TOPE_SEMANAL_CLONES, "Clases con clon por docente y semana", "", "clases", 1, 1000))),
            new Seccion("Integraciones", "Credenciales de los servicios externos. Los secretos se guardan cifrados: dejalos vacíos para conservar el valor actual.", List.of(
                    texto(Parametro.GOOGLE_CLIENT_ID, "Google OAuth: identificador de cliente", "", 255, false),
                    secreto(Parametro.GOOGLE_CLIENT_SECRET, "Google OAuth: secreto de cliente", ""),
                    texto(Parametro.MODO_USUARIO, "MODO: usuario", "", 100, true),
                    secreto(Parametro.MODO_CLAVE, "MODO: contraseña", ""),
                    texto(Parametro.MODO_PROCESSOR_CODE, "MODO: código de procesador", "Lo asigna MODO al comercio.", 50, true),
                    texto(Parametro.MODO_CC_CODE, "MODO: condiciones comerciales", "Por ejemplo 1CSI (una cuota sin interés).", 50, true),
                    texto(Parametro.MODO_NOMBRE_COMERCIO, "MODO: nombre del comercio", "", 100, true),
                    texto(Parametro.MODO_URL_API, "MODO: dirección de la API", "Preproducción o producción.", 255, true),
                    texto(Parametro.MODO_URL_CHECKOUT, "MODO: dirección del sitio de pagos", "", 255, true),
                    texto(Parametro.MODO_URL_PUBLICA, "Dirección pública del sistema", "Por ejemplo https://idoneos.online. Si queda vacía se usa la de la solicitud.", 255, false),
                    secreto(Parametro.HEYGEN_API_KEY, "HeyGen: clave de la API", "")))
    );
}
