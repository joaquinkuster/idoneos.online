package com.app.idoneos.servicio;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.idoneos.modelo.Administrador;
import com.app.idoneos.modelo.Alumno;
import com.app.idoneos.modelo.Categoria;
import com.app.idoneos.modelo.ClaseEnVivo;
import com.app.idoneos.modelo.ConsultaForo;
import com.app.idoneos.modelo.RespuestaForo;
import com.app.idoneos.modelo.Autoevaluacion;
import com.app.idoneos.utilidades.DuracionUtilidad;
import com.app.idoneos.modelo.AutoevaluacionPool;
import com.app.idoneos.modelo.Certificado;
import com.app.idoneos.modelo.Comprobante;
import com.app.idoneos.modelo.Cohorte;
import com.app.idoneos.modelo.Curso;
import com.app.idoneos.modelo.Descuento;
import com.app.idoneos.modelo.CursoModalidad;
import com.app.idoneos.modelo.Docente;
import com.app.idoneos.modelo.EstadoClaseEnVivo;
import com.app.idoneos.modelo.EstadoCohorte;
import com.app.idoneos.modelo.EstadoPago;
import com.app.idoneos.modelo.Inscripcion;
import com.app.idoneos.modelo.IntentoAutoevaluacion;
import com.app.idoneos.modelo.Material;
import com.app.idoneos.modelo.MetodoPago;
import com.app.idoneos.modelo.Modalidad;
import com.app.idoneos.modelo.Nivel;
import com.app.idoneos.modelo.OpcionRespuesta;
import com.app.idoneos.modelo.Pago;
import com.app.idoneos.modelo.Parametro;
import com.app.idoneos.modelo.DocenteCurso;
import com.app.idoneos.modelo.Pool;
import com.app.idoneos.modelo.Pregunta;
import com.app.idoneos.modelo.PreguntaIntento;
import com.app.idoneos.modelo.Programa;
import com.app.idoneos.modelo.RespuestaIntento;
import com.app.idoneos.modelo.Progreso;
import com.app.idoneos.modelo.Rol;
import com.app.idoneos.modelo.RolUsuario;
import com.app.idoneos.modelo.TipoMaterial;
import com.app.idoneos.modelo.TerminoGlosario;
import com.app.idoneos.modelo.Tema;
import com.app.idoneos.modelo.TemaCronograma;
import com.app.idoneos.modelo.Usuario;
import com.app.idoneos.repositorio.AdministradorRepositorio;
import com.app.idoneos.repositorio.AlumnoRepositorio;
import com.app.idoneos.repositorio.CategoriaRepositorio;
import com.app.idoneos.repositorio.ClaseEnVivoRepositorio;
import com.app.idoneos.repositorio.ConsultaForoRepositorio;
import com.app.idoneos.repositorio.RespuestaForoRepositorio;
import com.app.idoneos.repositorio.AutoevaluacionPoolRepositorio;
import com.app.idoneos.repositorio.AutoevaluacionRepositorio;
import com.app.idoneos.repositorio.CertificadoRepositorio;
import com.app.idoneos.repositorio.CohorteRepositorio;
import com.app.idoneos.repositorio.ComprobanteRepositorio;
import com.app.idoneos.repositorio.CursoModalidadRepositorio;
import com.app.idoneos.repositorio.CursoRepositorio;
import com.app.idoneos.repositorio.DescuentoRepositorio;
import com.app.idoneos.repositorio.DocenteRepositorio;
import com.app.idoneos.repositorio.EstadoClaseEnVivoRepositorio;
import com.app.idoneos.repositorio.EstadoCohorteRepositorio;
import com.app.idoneos.repositorio.EstadoPagoRepositorio;
import com.app.idoneos.repositorio.InscripcionRepositorio;
import com.app.idoneos.repositorio.IntentoAutoevaluacionRepositorio;
import com.app.idoneos.repositorio.MaterialRepositorio;
import com.app.idoneos.repositorio.MetodoPagoRepositorio;
import com.app.idoneos.repositorio.ModalidadRepositorio;
import com.app.idoneos.repositorio.NivelRepositorio;
import com.app.idoneos.repositorio.OpcionRespuestaRepositorio;
import com.app.idoneos.repositorio.PagoRepositorio;
import com.app.idoneos.repositorio.ParametroRepositorio;
import com.app.idoneos.repositorio.PoolRepositorio;
import com.app.idoneos.repositorio.PreguntaIntentoRepositorio;
import com.app.idoneos.repositorio.PreguntaRepositorio;
import com.app.idoneos.repositorio.RespuestaIntentoRepositorio;
import com.app.idoneos.servicio.Parametro.ParametroServicio;
import com.app.idoneos.repositorio.DocenteCursoRepositorio;
import com.app.idoneos.repositorio.ProgramaRepositorio;
import com.app.idoneos.repositorio.ProgresoRepositorio;
import com.app.idoneos.repositorio.RolRepositorio;
import com.app.idoneos.repositorio.RolUsuarioRepositorio;
import com.app.idoneos.repositorio.TipoMaterialRepositorio;
import com.app.idoneos.repositorio.TerminoGlosarioRepositorio;
import com.app.idoneos.repositorio.TemaCronogramaRepositorio;
import com.app.idoneos.repositorio.TemaRepositorio;
import com.app.idoneos.repositorio.UsuarioRepositorio;

/**
 * Servicio para insertar datos iniciales en la base de datos (semilla).
 * Crea los catálogos básicos, los usuarios de prueba de cada rol (administrador, docentes y alumnos)
 * y los datos académicos necesarios para probar el módulo de gestión de cursos:
 * categorías, cursos con su equipo docente, programas con cronograma y material, cohortes en
 * distintos estados e inscripciones.
 */
@Service
public class SemillaServicio {

    /** Carga horaria de los cursos de prueba para el alumno: 10 horas. */
    private static final int CARGA_HORARIA_SEMILLA_MINUTOS = 600;

    /** Duración inicial de los cursos de prueba, hasta que se les cargan los temas: 8 semanas. */
    private static final int DURACION_SEMILLA_DIAS = 56;

    /** Días acumulados del cronograma de cada programa de la semilla, para fijar la duración de sus cursos. */
    private final java.util.Map<Integer, Integer> diasPorPrograma = new java.util.HashMap<>();

    /** La duración del cronograma más largo de cada curso de la semilla. */
    private final java.util.Map<Integer, Integer> duracionMaximaDelCurso = new java.util.HashMap<>();

    /**
     * Contraseña de todos los usuarios de prueba.
     */
    private static final String CLAVE_DE_PRUEBA = "123456";

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @Autowired
    private RolRepositorio rolRepositorio;

    @Autowired
    private RolUsuarioRepositorio rolUsuarioRepositorio;

    @Autowired
    private AdministradorRepositorio administradorRepositorio;

    @Autowired
    private DocenteRepositorio docenteRepositorio;

    @Autowired
    private AlumnoRepositorio alumnoRepositorio;

    @Autowired
    private ParametroRepositorio parametroRepositorio;

    @Autowired
    private ParametroServicio parametroServicio;

    @Autowired
    private PagoRepositorio pagoRepositorio;

    @Autowired
    private ComprobanteRepositorio comprobanteRepositorio;

    @Autowired
    private EstadoPagoRepositorio estadoPagoRepositorio;

    @Autowired
    private EstadoCohorteRepositorio estadoCohorteRepositorio;

    @Autowired
    private MetodoPagoRepositorio metodoPagoRepositorio;

    @Autowired
    private DescuentoRepositorio descuentoRepositorio;

    @Autowired
    private NivelRepositorio nivelRepositorio;

    @Autowired
    private ModalidadRepositorio modalidadRepositorio;

    @Autowired
    private TipoMaterialRepositorio tipoMaterialRepositorio;

    @Autowired
    private EstadoClaseEnVivoRepositorio estadoClaseEnVivoRepositorio;

    @Autowired
    private CategoriaRepositorio categoriaRepositorio;

    @Autowired
    private CursoRepositorio cursoRepositorio;

    @Autowired
    private CursoModalidadRepositorio cursoModalidadRepositorio;

    @Autowired
    private DocenteCursoRepositorio docenteCursoRepositorio;

    @Autowired
    private ProgramaRepositorio programaRepositorio;

    @Autowired
    private TemaRepositorio temaRepositorio;

    @Autowired
    private TemaCronogramaRepositorio temaCronogramaRepositorio;

    @Autowired
    private MaterialRepositorio materialRepositorio;

    @Autowired
    private CohorteRepositorio cohorteRepositorio;

    @Autowired
    private CertificadoRepositorio certificadoRepositorio;

    @Autowired
    private InscripcionRepositorio inscripcionRepositorio;

    @Autowired
    private ProgresoRepositorio progresoRepositorio;

    @Autowired
    private ClaseEnVivoRepositorio claseEnVivoRepositorio;

    @Autowired
    private TerminoGlosarioRepositorio terminoGlosarioRepositorio;

    @Autowired
    private AutoevaluacionRepositorio autoevaluacionRepositorio;

    @Autowired
    private IntentoAutoevaluacionRepositorio intentoAutoevaluacionRepositorio;

    @Autowired
    private PoolRepositorio poolRepositorio;

    @Autowired
    private PreguntaRepositorio preguntaRepositorio;

    @Autowired
    private OpcionRespuestaRepositorio opcionRespuestaRepositorio;

    @Autowired
    private AutoevaluacionPoolRepositorio autoevaluacionPoolRepositorio;

    @Autowired
    private PreguntaIntentoRepositorio preguntaIntentoRepositorio;

    @Autowired
    private RespuestaIntentoRepositorio respuestaIntentoRepositorio;

    @Autowired
    private ConsultaForoRepositorio consultaForoRepositorio;

    @Autowired
    private RespuestaForoRepositorio respuestaForoRepositorio;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * Método que se ejecuta automáticamente para insertar datos iniciales
     * en la base de datos cuando el contexto de la aplicación se haya inicializado.
     *
     * Verifica si ya existe el usuario administrador y, si no existe, crea los catálogos, los usuarios
     * de prueba y los datos académicos del módulo de cursos.
     */
    @Transactional
    public void insertarSemilla() {
        String correoAdmin = "admin@idoneos.online";
        if (usuarioRepositorio.findByCorreo(correoAdmin).isPresent()) {
            return;
        }

        // Roles
        Rol rolAdministrador = rolRepositorio.save(new Rol("Administrador"));
        Rol rolDocente = rolRepositorio.save(new Rol("Docente"));
        Rol rolAlumno = rolRepositorio.save(new Rol("Alumno"));

        // Catálogos
        Nivel basico = nivelRepositorio.save(new Nivel("Básico"));
        Nivel intermedio = nivelRepositorio.save(new Nivel("Intermedio"));
        Nivel avanzado = nivelRepositorio.save(new Nivel("Avanzado"));
        Modalidad enVivo = modalidadRepositorio.save(new Modalidad(Modalidad.EN_VIVO));
        Modalidad grabada = modalidadRepositorio.save(new Modalidad(Modalidad.GRABADA));
        Modalidad clonIa = modalidadRepositorio.save(new Modalidad(Modalidad.CLON_IA));
        TipoMaterial presentacion = tipoMaterialRepositorio.save(new TipoMaterial("Presentación"));
        TipoMaterial grabacion = tipoMaterialRepositorio.save(new TipoMaterial("Grabación"));
        TipoMaterial bibliografia = tipoMaterialRepositorio.save(new TipoMaterial("Bibliografía"));
        tipoMaterialRepositorio.save(new TipoMaterial("Resumen"));
        EstadoClaseEnVivo claseProgramada = estadoClaseEnVivoRepositorio.save(new EstadoClaseEnVivo("Programada"));
        EstadoClaseEnVivo claseEnVivoAhora = estadoClaseEnVivoRepositorio.save(new EstadoClaseEnVivo(EstadoClaseEnVivo.EN_VIVO));
        EstadoClaseEnVivo claseFinalizada = estadoClaseEnVivoRepositorio.save(new EstadoClaseEnVivo("Finalizada"));
        estadoCohorteRepositorio.save(new EstadoCohorte(EstadoCohorte.PENDIENTE));
        estadoCohorteRepositorio.save(new EstadoCohorte(EstadoCohorte.ACTIVA));
        estadoCohorteRepositorio.save(new EstadoCohorte(EstadoCohorte.CANCELADA));
        EstadoPago pagoPendiente = estadoPagoRepositorio.save(new EstadoPago(EstadoPago.PENDIENTE));
        EstadoPago pagoAprobado = estadoPagoRepositorio.save(new EstadoPago(EstadoPago.APROBADO));
        EstadoPago pagoRechazado = estadoPagoRepositorio.save(new EstadoPago(EstadoPago.RECHAZADO));
        MetodoPago pagoModo = metodoPagoRepositorio.save(new MetodoPago(MetodoPago.MODO));
        MetodoPago pagoTransferencia = metodoPagoRepositorio.save(new MetodoPago(MetodoPago.TRANSFERENCIA));
        estadoClaseEnVivoRepositorio.save(new EstadoClaseEnVivo("Cancelada"));

        // Administrador y parámetros
        Usuario usuarioAdmin = crearUsuario("Admin", "Idóneos", "20111222", correoAdmin, "+54 376 4000000", rolAdministrador);
        Administrador admin = administradorRepositorio.save(new Administrador(usuarioAdmin));
        parametroRepositorio.save(new Parametro(admin, Parametro.MINIMO_TEMAS_CON_MATERIAL, "2"));
        parametroRepositorio.save(new Parametro(admin, Parametro.COHORTE_PLAZO_MINIMO_CONTENIDO_DIAS,
                String.valueOf(Parametro.COHORTE_PLAZO_MINIMO_CONTENIDO_DIAS_POR_DEFECTO)));
        parametroRepositorio.save(new Parametro(admin, Parametro.CRONOGRAMA_DIAS_POR_TEMA_NUEVO,
                String.valueOf(Parametro.CRONOGRAMA_DIAS_POR_TEMA_NUEVO_POR_DEFECTO)));
        parametroRepositorio.save(new Parametro(admin, Parametro.PLAZO_CONFIRMACION_PAGO,
                String.valueOf(Parametro.PLAZO_CONFIRMACION_PAGO_POR_DEFECTO)));
        parametroRepositorio.save(new Parametro(admin, Parametro.CBU_INSTITUCION, "0000003100012345678901"));
        parametroRepositorio.save(new Parametro(admin, Parametro.PLAZO_EDICION_FORO,
                String.valueOf(Parametro.PLAZO_EDICION_FORO_POR_DEFECTO)));
        parametroRepositorio.save(new Parametro(admin, Parametro.GRACIA_ENTREGA_AUTOEVALUACION,
                String.valueOf(Parametro.GRACIA_ENTREGA_AUTOEVALUACION_POR_DEFECTO)));
        // Medio de pago MODO: credenciales de PRUEBA (ambiente de preproducción) publicadas en la documentación de MODO.
        // El cliente las reemplaza por las de su cuenta; la contraseña se guarda cifrada.
        parametroServicio.registrarTexto(admin, Parametro.MODO_USUARIO, "PLAYDIGITAL SA-318979-preprod");
        parametroServicio.registrarTexto(admin, Parametro.MODO_CLAVE, "318979-P75V/QLKfVKX");
        parametroServicio.registrarTexto(admin, Parametro.MODO_URL_API, "https://merchants.preprod.playdigital.com.ar");
        parametroServicio.registrarTexto(admin, Parametro.MODO_URL_SCRIPT_MODAL,
                "https://ecommerce-modal.preprod.modo.com.ar/bundle.js");
        parametroServicio.registrarTexto(admin, Parametro.MODO_NOMBRE_COMERCIO, "Idoneos Online");
        // Gateway Decidir 2.0/Tradicional y Decidir+ (las tarjetas de prueba de Payway) y una cuota sin interés
        parametroServicio.registrarTexto(admin, Parametro.MODO_PROCESSOR_CODE, "P1019");
        parametroServicio.registrarTexto(admin, Parametro.MODO_CC_CODE, "1CSI");
        parametroServicio.registrarTexto(admin, Parametro.MODO_URL_PUBLICA, "");
        parametroServicio.registrarTexto(admin, Parametro.MODO_MONTO_MINIMO,
                String.valueOf((int) Parametro.MODO_MONTO_MINIMO_POR_DEFECTO));

        // Descuentos: uno de cada estado (vigentes con y sin condición, próximo, vencido y agotado)
        LocalDateTime hoy = LocalDateTime.now();
        descuentoRepositorio.save(new Descuento("Descuento de bienvenida", 0, 10, hoy.minusDays(10), hoy.plusDays(60), 100, 0));
        descuentoRepositorio.save(new Descuento("Alumno frecuente", 2, 20, hoy.minusDays(10), hoy.plusDays(90), 50, 0));
        descuentoRepositorio.save(new Descuento("Promo de verano", 0, 15, hoy.plusDays(30), hoy.plusDays(90), 30, 0));
        descuentoRepositorio.save(new Descuento("Lanzamiento", 0, 25, hoy.minusDays(90), hoy.minusDays(30), 20, 8));
        descuentoRepositorio.save(new Descuento("Preventa agotada", 1, 30, hoy.minusDays(20), hoy.plusDays(20), 5, 5));

        // Docentes
        Docente fausto = crearDocente("Fausto", "Spotorno", "23456789", "fausto.spotorno@idoneos.online", rolDocente, 20,
                "12845", "Economista UCA, Director de Maestría en UADE y socio de Orlando J. Ferreres & Asoc.");
        Docente sebastian = crearDocente("Sebastián", "Bordato", "24567890", "sebastian.bordato@idoneos.online", rolDocente, 15,
                "15932", "Contador Público UBA. Experto en planificación fiscal corporativa y mercado de capitales.");
        Docente mariano = crearDocente("Mariano", "Otálora", "25678901", "mariano.otalora@idoneos.online", rolDocente, 18,
                "18451", "Especialista en planificación patrimonial, inversiones inmobiliarias y finanzas personales.");

        // Alumnos
        Alumno lucia = crearAlumno("Lucía", "Fernández", "40123456", "lucia.fernandez@correo.com", rolAlumno);
        Alumno martin = crearAlumno("Martín", "Gómez", "41234567", "martin.gomez@correo.com", rolAlumno);
        Alumno valentina = crearAlumno("Valentina", "Ruiz", "42345678", "valentina.ruiz@correo.com", rolAlumno);
        // Alumno sin inscripciones: permite probar la pantalla "Mis cursos" sin cursos
        crearAlumno("Tomás", "Herrera", "43456789", "tomas.herrera@correo.com", rolAlumno);

        // Categorías
        Categoria mercadoCapitales = categoriaRepositorio.save(crearCategoria("Mercado de Capitales",
                "Acciones, bonos, ONs, opciones y operatoria bursátil en BYMA."));
        Categoria macroeconomia = categoriaRepositorio.save(crearCategoria("Macroeconomía",
                "Análisis de variables macroeconómicas, política monetaria y fiscal."));
        Categoria impuestos = categoriaRepositorio.save(crearCategoria("Impuestos y Finanzas",
                "Planificación fiscal y tributaria para empresas y personas."));
        Categoria finanzasPersonales = categoriaRepositorio.save(crearCategoria("Finanzas Personales",
                "Ahorro, inversión y planificación patrimonial."));
        Categoria cuantitativas = categoriaRepositorio.save(crearCategoria("Finanzas Cuantitativas",
                "Análisis técnico, modelos y herramientas cuantitativas."));
        categoriaRepositorio.save(crearCategoria("Criptoactivos", "Activos digitales y finanzas descentralizadas."));
        Categoria contabilidad = categoriaRepositorio.save(crearCategoria("Contabilidad",
                "Registro, lectura e interpretación de la información contable."));
        Categoria flujoDeCaja = categoriaRepositorio.save(crearCategoria("Eficiencia de Flujo de Caja",
                "Gestión del capital de trabajo y planificación de la liquidez."));

        LocalDateTime ahora = LocalDateTime.now();

        // Curso 1: Mercado de Capitales Argentino (en vivo + grabada), con cohortes abierta, en dictado y finalizada
        DocenteCurso titular1 = crearCurso("Mercado de Capitales Argentino",
                "Aprendé a operar acciones, bonos, ONs y opciones en BYMA con docentes de primer nivel.", 150000f,
                "mercado-capitales-argentino.jpg", mercadoCapitales, intermedio, true,
                List.of(enVivo, grabada), fausto, List.of(sebastian, mariano));
        Curso curso1 = titular1.getCurso();
        Programa programa1 = crearPrograma(curso1, "Programa 2026",
                "Dominar la operatoria bursátil y el análisis de instrumentos del mercado argentino.");
        Tema tema1 = agregarTema(programa1, curso1, 1, 2, "Introducción al mercado de capitales",
                "Actores, instrumentos y funcionamiento del mercado.", titular1, presentacion);
        Tema tema2 = agregarTema(programa1, curso1, 2, 3, "Renta fija y bonos",
                "Valuación y operatoria de bonos soberanos.", titular1, presentacion);
        Tema tema3 = agregarTema(programa1, curso1, 3, 4, "Renta variable y opciones",
                "Acciones, CEDEARs y estrategias con opciones.", null, null);
        Cohorte abierta1 = crearCohorte(programa1, ahora.minusDays(10), ahora.plusDays(20), ahora.plusDays(25),
                ahora.plusDays(90), 12, 30);
        Cohorte enDictado1 = crearCohorte(programa1, ahora.minusDays(60), ahora.minusDays(30), ahora.minusDays(20),
                ahora.plusDays(40), 12, 25);
        crearCohorte(programa1, ahora.minusDays(200), ahora.minusDays(170), ahora.minusDays(160), ahora.minusDays(80), 12, 25);
        Inscripcion luciaMercado = crearInscripcion(abierta1, lucia, ahora, tema1);
        Inscripcion martinMercado = crearInscripcion(abierta1, martin, ahora, null);
        Inscripcion inscripcionValentina = crearInscripcion(enDictado1, valentina, ahora, tema1);
        // Pagos de las inscripciones a un curso con costo (CU-50): aprobados por MODO, con su comprobante
        registrarPago(luciaMercado, pagoAprobado, pagoModo, "Tarjeta de crédito Mastercard", "0008", "QRL3M0A1", null,
                ahora.minusDays(9));
        registrarPago(martinMercado, pagoAprobado, pagoModo, "Cuenta bancaria Macro", "6168", "QRM4R7B2", null,
                ahora.minusDays(8));
        registrarPago(inscripcionValentina, pagoAprobado, pagoModo, "Tarjeta de crédito Visa", "0008", "QRV9A1C3", null,
                ahora.minusDays(55));
        claseEnVivoRepositorio.save(new ClaseEnVivo(titular1, claseProgramada, enDictado1,
                "Clase 1: Panorama del mercado de capitales", ahora.plusDays(3), 90));
        claseEnVivoRepositorio.save(new ClaseEnVivo(titular1, claseFinalizada, enDictado1,
                "Clase 0: Presentación del curso", ahora.minusDays(15), 60));
        claseEnVivoRepositorio.save(new ClaseEnVivo(titular1, claseEnVivoAhora, enDictado1,
                "Clase magistral: Operatoria de renta fija en BYMA", ahora.minusMinutes(20), 90));

        // Contenido de los temas 1 y 2 (acceso al curso del alumno en dictado, CU-27)
        agregarMaterial(tema1, titular1, grabacion, "Grabación: Estructura del mercado argentino");
        agregarMaterial(tema1, titular1, bibliografia, "Ley 26.831 y modificatorias CNV");
        agregarMaterial(tema2, titular1, bibliografia, "Guía teórica de renta fija");
        agregarMaterial(tema2, titular1, presentacion, "Borrador: ejercicios de duration").setOculto(true);
        agregarGlosario(tema1, "BYMA", "Bolsas y Mercados Argentinos: el mercado donde se negocian los valores.");
        agregarGlosario(tema1, "CNV", "Comisión Nacional de Valores: organismo que regula y controla el mercado de capitales.");
        agregarGlosario(tema1, "ON", "Obligación negociable: título de deuda que emiten las empresas para financiarse.");
        agregarGlosario(tema2, "TIR", "Tasa interna de retorno: rendimiento anual de un bono si se lo mantiene hasta el vencimiento.");
        agregarGlosario(tema2, "Duration", "Plazo promedio ponderado en que se recibe el dinero de un bono.");
        // Autoevaluaciones (MOD-F-04): la 1 deja navegar libremente y salir; la 2 es secuencial, descuenta por error y no deja salir
        Pool poolMarcoRegulatorio = crearPool(tema1, "Marco regulatorio");
        agregarPregunta(poolMarcoRegulatorio, "¿Qué organismo regula y controla el mercado de capitales argentino?", false,
                "*Comisión Nacional de Valores", "Banco Central de la República Argentina",
                "Administración Federal de Ingresos Públicos", "Superintendencia de Seguros de la Nación");
        agregarPregunta(poolMarcoRegulatorio, "BYMA es el mercado donde se negocian los valores en la Argentina.", false,
                "*Verdadero", "Falso");
        agregarPregunta(poolMarcoRegulatorio, "¿Cuáles de estos instrumentos se negocian en BYMA?", true,
                "*Acciones", "*Obligaciones negociables", "Plazos fijos tradicionales", "Préstamos personales");
        agregarPregunta(poolMarcoRegulatorio, "Una obligación negociable es un título de deuda que emite una empresa.", false,
                "*Verdadero", "Falso");
        agregarPregunta(poolMarcoRegulatorio, "¿Quiénes necesitan matrícula para operar en el mercado de capitales?", false,
                "*Los agentes y sociedades de bolsa", "El inversor minorista", "Quien compra un CEDEAR",
                "Quien invierte en un fondo común");
        agregarPregunta(poolMarcoRegulatorio, "¿Qué funciones cumple la CNV?", true,
                "*Autorizar la oferta pública de valores", "*Supervisar a los agentes registrados",
                "Fijar la tasa de interés de política monetaria", "Emitir moneda");
        Autoevaluacion autoevaluacion1 = agregarAutoevaluacion(tema1, poolMarcoRegulatorio,
                "Autoevaluación Tema 1: Marco regulatorio", 5, 6f, 3, 30, ahora.minusDays(30), false, true, true);
        Pool poolRentaFija = crearPool(tema2, "Renta fija");
        agregarPregunta(poolRentaFija, "¿Qué mide la tasa interna de retorno (TIR) de un bono?", false,
                "*El rendimiento anual si se lo mantiene hasta el vencimiento", "El precio de mercado del bono",
                "El valor nominal del cupón", "La inflación esperada");
        agregarPregunta(poolRentaFija, "Cuando suben las tasas de interés, el precio de un bono tiende a bajar.", false,
                "*Verdadero", "Falso");
        agregarPregunta(poolRentaFija, "¿Qué conceptos forman parte de los flujos de un bono con cupones?", true,
                "*Los cupones de interés", "*La amortización del capital", "El tipo de cambio oficial", "El valor del dólar blue");
        agregarPregunta(poolRentaFija, "La duration es el plazo promedio ponderado en que se recibe el dinero de un bono.", false,
                "*Verdadero", "Falso");
        agregarPregunta(poolRentaFija, "Un bono cotiza a la par cuando su precio es igual a:", false,
                "*Su valor nominal", "Su valor residual menos los cupones", "Su TIR", "Cero");
        agregarPregunta(poolRentaFija, "¿Qué factores afectan la sensibilidad del precio de un bono?", true,
                "*La duration", "*El cupón", "El color del papel", "El horario de operación");
        agregarAutoevaluacion(tema2, poolRentaFija, "Autoevaluación Tema 2: Ejercicios de rendimiento", 5, 7f, 3, 20,
                ahora.minusDays(5), true, false, false);
        agregarEvaluacionBasica(tema3, 3, ahora);
        // Valentina: un intento con 2 correctas y 1 incorrecta de 5 (4,00: no aprobado) y otro con 4 correctas (8,00: aprobado)
        registrarIntento(inscripcionValentina, autoevaluacion1, 2, 1, ahora.minusDays(12));
        registrarIntento(inscripcionValentina, autoevaluacion1, 4, 0, ahora.minusDays(10));
        // Lucía ya completó el tema 1: su intento aprobado
        registrarIntento(luciaMercado, autoevaluacion1, 5, 0, ahora.minusDays(1));
        ConsultaForo consultaCnv = consultaForoRepositorio.save(new ConsultaForo(tema1, inscripcionValentina,
                "¿La CNV exige matrícula para operar con obligaciones negociables?"));
        consultaCnv.setFechaCreacion(ahora.minusDays(3));
        consultaForoRepositorio.save(consultaCnv);
        RespuestaForo respuestaCnv = new RespuestaForo(titular1, consultaCnv,
                "Para operar como inversor no, la matrícula la necesitan los agentes. Lo vemos en la clase 2.");
        respuestaCnv.setFechaCreacion(ahora.minusDays(2));
        respuestaForoRepositorio.save(respuestaCnv);
        ConsultaForo consultaTir = consultaForoRepositorio.save(new ConsultaForo(tema2, inscripcionValentina,
                "¿Cómo se calcula la duration de un bono con cupones?"));
        consultaTir.setFechaCreacion(ahora.minusHours(5));
        consultaForoRepositorio.save(consultaTir);

        // Curso 2: Macroeconomía de Coyuntura (grabada), con cohorte abierta sin inscriptos
        DocenteCurso titular2 = crearCurso("Macroeconomía de Coyuntura",
                "Tipo de cambio, inflación y tasas: las variables que mueven la economía argentina.", 0f,
                "macroeconomia-coyuntura.jpg", macroeconomia, basico, true, List.of(grabada), fausto, List.of());
        Curso curso2 = titular2.getCurso();
        Programa programa2 = crearPrograma(curso2, "Programa 2026",
                "Interpretar la coyuntura macroeconómica y su impacto en las decisiones financieras.");
        agregarEvaluacionBasica(agregarTema(programa2, curso2, 1, 2, "Inflación y precios", "Medición y causas de la inflación.",
                titular2, presentacion), 1, ahora);
        agregarEvaluacionBasica(agregarTema(programa2, curso2, 2, 2, "Tipo de cambio", "Regímenes cambiarios y brecha.",
                titular2, presentacion), 2, ahora);
        agregarEvaluacionBasica(agregarTema(programa2, curso2, 3, 2, "Tasas y política monetaria", "Instrumentos del BCRA.",
                titular2, presentacion), 3, ahora);
        crearCohorte(programa2, ahora.minusDays(5), ahora.plusDays(30), null, null, 8, null);

        // Curso 3: Planificación Fiscal Corporativa (no alcanza el mínimo de temas con material)
        DocenteCurso titular3 = crearCurso("Planificación Fiscal Corporativa",
                "Estrategias de planificación tributaria para empresas: IVA, ganancias y regímenes especiales.", 120000f,
                "planificacion-fiscal-corporativa.jpg", impuestos, avanzado, true, List.of(grabada), sebastian, List.of());
        Curso curso3 = titular3.getCurso();
        Programa programa3 = crearPrograma(curso3, "Programa 2026",
                "Diseñar estructuras fiscales eficientes dentro del marco legal.");
        agregarTema(programa3, curso3, 1, 3, "Impuesto a las ganancias", "Determinación y planificación.", titular3, presentacion);
        agregarTema(programa3, curso3, 2, 3, "IVA y regímenes especiales", "Débito, crédito y regímenes.", null, null);

        // Curso 4: Finanzas Personales e Inversión (grabada + clon de IA), con cohorte abierta e inscripción
        DocenteCurso titular4 = crearCurso("Finanzas Personales e Inversión",
                "Organizá tus finanzas, aprendé a ahorrar e invertir con criterio y planificá tu patrimonio.", 80000f,
                "finanzas-personales-inversion.jpg", finanzasPersonales, basico, true,
                List.of(grabada, clonIa), mariano, List.of());
        Curso curso4 = titular4.getCurso();
        Programa programa4 = crearPrograma(curso4, "Programa 2026",
                "Construir un plan financiero personal sólido.");
        Tema tema4 = agregarTema(programa4, curso4, 1, 2, "Presupuesto y ahorro", "Ordenar ingresos y gastos.",
                titular4, presentacion);
        Autoevaluacion autoevaluacion4 = agregarEvaluacionBasica(tema4, 1, ahora);
        agregarEvaluacionBasica(agregarTema(programa4, curso4, 2, 3, "Instrumentos de inversión",
                "Plazos fijos, bonos y fondos.", titular4, presentacion), 2, ahora);
        agregarEvaluacionBasica(agregarTema(programa4, curso4, 3, 2, "Planificación patrimonial",
                "Objetivos y horizonte de inversión.", titular4, presentacion), 3, ahora);
        Cohorte abierta4 = crearCohorte(programa4, ahora.minusDays(2), ahora.plusDays(40), null, null, 10, 20);
        Inscripcion martinFinanzas = crearInscripcion(abierta4, martin, ahora, tema4);
        registrarIntento(martinFinanzas, autoevaluacion4, 3, 0, ahora.minusDays(1));
        registrarPago(martinFinanzas, pagoAprobado, pagoTransferencia, "Transferencia al CBU de la institución", null,
                "TRF-000451", null, ahora.minusDays(1));
        // Curso con costo: la inscripción queda pendiente de pago hasta que se apruebe el pago
        Inscripcion pendienteDePago = crearInscripcion(abierta4, lucia, ahora, null);
        pendienteDePago.setHabilitado(false);
        pendienteDePago.setFechaVencimientoAcceso(null); // se calcula cuando se habilite
        inscripcionRepositorio.save(pendienteDePago);
        // Su pago con MODO fue rechazado: puede reintentarlo (CU-52) o lo puede acreditar el administrador (CU-54)
        registrarPago(pendienteDePago, pagoRechazado, pagoModo, "Tarjeta de crédito Visa", "0008", null,
                "INSUFFICIENT_FUNDS", ahora.minusHours(3));

        // Curso 5: Análisis Técnico Bursátil (en vivo + grabada), con cohorte próxima
        DocenteCurso titular5 = crearCurso("Análisis Técnico Bursátil",
                "Gráficos, indicadores y patrones para tomar decisiones de compra y venta.", 95000f,
                "analisis-tecnico-bursatil.jpg", cuantitativas, intermedio, true,
                List.of(enVivo, grabada), mariano, List.of(fausto));
        Curso curso5 = titular5.getCurso();
        Programa programa5 = crearPrograma(curso5, "Programa 2026",
                "Aplicar herramientas de análisis técnico a distintos activos.");
        agregarEvaluacionBasica(agregarTema(programa5, curso5, 1, 3, "Fundamentos del análisis técnico",
                "Tendencias, soportes y resistencias.", titular5, presentacion), 1, ahora);
        agregarEvaluacionBasica(agregarTema(programa5, curso5, 2, 3, "Indicadores y osciladores",
                "Medias móviles, RSI y MACD.", titular5, presentacion), 2, ahora);
        crearCohorte(programa5, ahora.plusDays(15), ahora.plusDays(45), ahora.plusDays(50), ahora.plusDays(100), 10, null);
        // Cohorte pendiente, registrada por el administrador sin programa: el docente todavía tiene que elegir uno
        crearCohorteConEstado(curso5, null, ahora.plusDays(30), ahora.plusDays(60), ahora.plusDays(65), ahora.plusDays(120), 10,
                null, EstadoCohorte.PENDIENTE);
        // Cohorte cancelada: llegó la fecha de inicio de la inscripción y el contenido no estaba completo
        crearCohorteConEstado(curso5, programa5, ahora.minusDays(3), ahora.plusDays(25), ahora.plusDays(30), ahora.plusDays(80), 10,
                null, EstadoCohorte.CANCELADA);
        // Cohorte en su semana 6 de 6 con Valentina sin completar ningún tema: va atrasada (la primera terminaba en la semana 3)
        Cohorte enDictado5 = crearCohorte(programa5, ahora.minusDays(70), ahora.minusDays(45), ahora.minusDays(40),
                ahora.plusDays(2), 8, 20);
        Inscripcion valentinaAnalisis = crearInscripcion(enDictado5, valentina, ahora, null);
        registrarPago(valentinaAnalisis, pagoAprobado, pagoModo, "Tarjeta de débito Visa", "5609", "QRV2D5E4", null,
                ahora.minusDays(68));
        // Cohorte finalizada: la inscripción ya venció y el alumno tiene su certificado
        Cohorte finalizada5 = crearCohorte(programa5, ahora.minusDays(220), ahora.minusDays(190), ahora.minusDays(185),
                ahora.minusDays(130), 8, 20);
        Inscripcion inscripcionVencida = crearInscripcion(finalizada5, martin, ahora, null);
        inscripcionVencida.setFechaCreacion(ahora.minusDays(215));
        inscripcionVencida.setFechaVencimientoAcceso(ahora.minusDays(50));
        inscripcionRepositorio.save(inscripcionVencida);
        registrarPago(inscripcionVencida, pagoAprobado, pagoModo, "Tarjeta de crédito Mastercard", "5290", "QRX7F9G5", null,
                ahora.minusDays(214));
        certificadoRepositorio.save(new Certificado(inscripcionVencida, "CERT-2026-000001", ahora.minusDays(55),
                "Certificado de finalización del curso Análisis Técnico Bursátil", "Martín Gómez", "41234567"));

        // Curso 6: Introducción a las Finanzas (sin programas ni temas: puede darse de baja)
        crearCurso("Introducción a las Finanzas", "Conceptos básicos para empezar a entender el mundo financiero.", 40000f,
                "introduccion-finanzas.jpg", mercadoCapitales, basico, false, List.of(grabada), sebastian, List.of());

        // Curso 7: curso dado de baja (para ver cómo se listan los registros dados de baja)
        DocenteCurso titular7 = crearCurso("Economía Argentina 2024",
                "Edición histórica del curso de economía argentina. Ya no se dicta.", 60000f,
                "economia-argentina-2024.jpg", macroeconomia, intermedio, false, List.of(grabada), fausto, List.of());
        Curso curso7 = titular7.getCurso();
        curso7.marcarInactivo();
        cursoRepositorio.save(curso7);

        // Más cursos con inscripción abierta, para que el inicio y el catálogo tengan con qué armar sus listados
        crearCursoAbierto("Contabilidad Básica para Inversores",
                "Los conceptos contables que necesitás para leer el balance de una empresa.", 45000f, contabilidad, basico,
                List.of(grabada), sebastian, presentacion, "Activo, pasivo y patrimonio", "Estado de resultados", ahora);
        crearCursoAbierto("Estados Contables: Cómo Leerlos",
                "Analizá balances y ratios para evaluar la situación de una empresa.", 70000f, contabilidad, intermedio,
                List.of(grabada, clonIa), sebastian, presentacion, "Lectura del balance", "Ratios financieros", ahora);
        crearCursoAbierto("Impuestos para Monotributistas y Autónomos",
                "Obligaciones, categorías y estrategias para tributar de forma eficiente.", 55000f, impuestos, basico,
                List.of(grabada), sebastian, presentacion, "Monotributo y autónomos", "Facturación y obligaciones", ahora);
        crearCursoAbierto("Flujo de Caja para Pymes",
                "Proyectá tu liquidez y tomá mejores decisiones de capital de trabajo.", 85000f, flujoDeCaja, intermedio,
                List.of(enVivo, grabada), mariano, presentacion, "Proyección de ingresos y egresos", "Capital de trabajo", ahora);
        crearCursoAbierto("Bonos Soberanos y Obligaciones Negociables",
                "Valuación, riesgo y operatoria de renta fija en el mercado argentino.", 120000f, mercadoCapitales, avanzado,
                List.of(enVivo), fausto, presentacion, "Valuación de bonos", "Riesgo y duration", ahora);
        crearCursoAbierto("Opciones y Derivados en BYMA",
                "Cobertura y especulación con opciones y futuros, paso a paso.", 140000f, mercadoCapitales, avanzado,
                List.of(grabada), fausto, presentacion, "Fundamentos de las opciones", "Estrategias con opciones", ahora);
        crearCursoAbierto("Inflación y Política Monetaria",
                "Cómo se forman los precios y qué puede hacer el Banco Central.", 0f, macroeconomia, intermedio,
                List.of(grabada), fausto, presentacion, "Inflación: causas y medición", "Política monetaria", ahora);
        crearCursoAbierto("Cómo Armar tu Primer Portafolio",
                "Diversificá tus inversiones según tu perfil y tu horizonte de tiempo.", 65000f, finanzasPersonales, basico,
                List.of(grabada, clonIa), mariano, presentacion, "Perfil del inversor", "Diversificación", ahora);

        // Veinte cursos más con inscripción abierta, de todas las categorías, niveles y modalidades
        crearCursoAbierto("Introducción a la Renta Variable",
                "Qué son las acciones, cómo se operan y qué mirar antes de comprar la primera.", 48000f, mercadoCapitales, basico,
                List.of(grabada), sebastian, presentacion, "Qué es una acción", "Cómo operar en BYMA", ahora);
        crearCursoAbierto("Fondos Comunes de Inversión",
                "Cómo funcionan los fondos, qué tipos hay y cómo elegir el que se ajusta a tu perfil.", 52000f, mercadoCapitales, basico,
                List.of(grabada, clonIa), mariano, presentacion, "Tipos de fondos", "Cómo elegir un fondo", ahora);
        crearCursoAbierto("CEDEARs: Invertir en Empresas del Mundo",
                "Accedé a empresas del exterior desde el mercado argentino, con sus ventajas y riesgos.", 98000f, mercadoCapitales, intermedio,
                List.of(enVivo, grabada), fausto, presentacion, "Qué es un CEDEAR", "Riesgos y tipo de cambio", ahora);
        crearCursoAbierto("Análisis Fundamental de Empresas",
                "Valuá una empresa a partir de sus balances, su negocio y su industria.", 135000f, mercadoCapitales, avanzado,
                List.of(grabada), sebastian, presentacion, "Lectura de estados financieros", "Valuación por múltiplos", ahora);
        crearCursoAbierto("Dólar y Tipo de Cambio en Argentina",
                "Por qué hay tantos dólares, qué determina su precio y cómo leer el mercado cambiario.", 42000f, macroeconomia, basico,
                List.of(grabada), fausto, presentacion, "Los tipos de dólar", "Brecha y expectativas", ahora);
        crearCursoAbierto("Economía para No Economistas",
                "Los conceptos básicos de la economía explicados sin tecnicismos, con ejemplos cotidianos.", 0f, macroeconomia, basico,
                List.of(grabada, clonIa), fausto, presentacion, "Oferta, demanda y precios", "Crecimiento y empleo", ahora);
        crearCursoAbierto("Deuda Pública y Riesgo País",
                "Cómo se endeuda un país, qué mide el riesgo país y cómo impacta en tus inversiones.", 118000f, macroeconomia, avanzado,
                List.of(enVivo), fausto, presentacion, "Deuda soberana", "Riesgo país y mercados", ahora);
        crearCursoAbierto("Ahorro e Inversión desde Cero",
                "Un plan simple para empezar a ahorrar e invertir aunque nunca lo hayas hecho.", 38000f, finanzasPersonales, basico,
                List.of(grabada), mariano, presentacion, "Ordenar tus finanzas", "Primeras inversiones", ahora);
        crearCursoAbierto("Jubilación y Planificación del Retiro",
                "Calculá cuánto necesitás y cómo construir tu ahorro para el retiro.", 62000f, finanzasPersonales, intermedio,
                List.of(grabada), mariano, presentacion, "Cuánto necesito para retirarme", "Instrumentos de largo plazo", ahora);
        crearCursoAbierto("Inversiones Inmobiliarias para Principiantes",
                "Cómo evaluar una propiedad como inversión y qué alternativas hay al ladrillo.", 72000f, finanzasPersonales, intermedio,
                List.of(enVivo, grabada), mariano, presentacion, "Rentabilidad inmobiliaria", "Alternativas al ladrillo", ahora);
        crearCursoAbierto("Gestión de Deudas y Crédito Personal",
                "Cómo usar el crédito con criterio, salir de deudas y cuidar tu historial.", 35000f, finanzasPersonales, basico,
                List.of(grabada), mariano, presentacion, "Tasas y costo real del crédito", "Estrategias para salir de deudas", ahora);
        crearCursoAbierto("Indicadores y Estrategias de Trading",
                "Armá y probá estrategias de operación con indicadores técnicos.", 88000f, cuantitativas, intermedio,
                List.of(grabada), mariano, presentacion, "Indicadores de tendencia", "Gestión del riesgo", ahora);
        crearCursoAbierto("Modelos de Valuación con Excel",
                "Construí modelos de valuación paso a paso en planillas de cálculo.", 125000f, cuantitativas, avanzado,
                List.of(enVivo, grabada), sebastian, presentacion, "Flujo de fondos descontado", "Análisis de sensibilidad", ahora);
        crearCursoAbierto("Estadística Aplicada a las Finanzas",
                "Media, desvío, correlación y probabilidad aplicados a retornos y riesgo.", 76000f, cuantitativas, intermedio,
                List.of(grabada), fausto, presentacion, "Retorno y riesgo", "Correlación y diversificación", ahora);
        crearCursoAbierto("IVA y Ganancias en la Práctica",
                "Cómo se liquidan el IVA y el impuesto a las ganancias, con casos reales.", 58000f, impuestos, intermedio,
                List.of(grabada), sebastian, presentacion, "Liquidación del IVA", "Ganancias de personas y sociedades", ahora);
        crearCursoAbierto("Planificación Tributaria para Pymes",
                "Estrategias para optimizar la carga impositiva de una pyme dentro de la ley.", 110000f, impuestos, avanzado,
                List.of(enVivo), sebastian, presentacion, "Estructura tributaria", "Beneficios y regímenes", ahora);
        crearCursoAbierto("Contabilidad de Costos",
                "Cómo calcular el costo de un producto o servicio y fijar precios con fundamento.", 64000f, contabilidad, intermedio,
                List.of(grabada), sebastian, presentacion, "Clasificación de costos", "Costeo y precio de venta", ahora);
        crearCursoAbierto("Presupuesto y Control de Gestión",
                "Armá un presupuesto anual y controlá los desvíos mes a mes.", 69000f, flujoDeCaja, intermedio,
                List.of(grabada, clonIa), sebastian, presentacion, "Armado del presupuesto", "Control de desvíos", ahora);
        crearCursoAbierto("Financiamiento y Tasas para Pymes",
                "Qué opciones de financiamiento tiene una pyme y cómo comparar sus costos.", 54000f, flujoDeCaja, basico,
                List.of(enVivo, grabada), mariano, presentacion, "Fuentes de financiamiento", "Costo financiero total", ahora);
        crearCursoAbierto("Tesorería y Capital de Trabajo",
                "Administrá la caja de una empresa para no quedarte sin liquidez.", 92000f, flujoDeCaja, avanzado,
                List.of(grabada), sebastian, presentacion, "Gestión de tesorería", "Ciclo de conversión del efectivo", ahora);
    }

    /**
     * Crea un curso sin imagen con un programa de dos temas con material y una cohorte con inscripción abierta.
     */
    private void crearCursoAbierto(String nombre, String descripcion, float precio, Categoria categoria, Nivel nivel,
            List<Modalidad> modalidades, Docente titular, TipoMaterial tipoMaterial, String tema1, String tema2,
            LocalDateTime ahora) {
        DocenteCurso participacion = crearCurso(nombre, descripcion, precio, null, categoria, nivel, true,
                modalidades, titular, List.of());
        Curso curso = participacion.getCurso();
        Programa programa = crearPrograma(curso, "Programa 2026", "Dominar los contenidos centrales del curso.");
        agregarEvaluacionBasica(agregarTema(programa, curso, 1, 2, tema1, "Conceptos centrales.", participacion,
                tipoMaterial), 1, ahora);
        agregarEvaluacionBasica(agregarTema(programa, curso, 2, 2, tema2, "Aplicación práctica.", participacion,
                tipoMaterial), 2, ahora);
        boolean enVivo = modalidades.stream().anyMatch(modalidad -> Modalidad.EN_VIVO.equals(modalidad.getNombre()));
        crearCohorte(programa, ahora.minusDays(1), ahora.plusDays(45), enVivo ? ahora.plusDays(50) : null,
                enVivo ? ahora.plusDays(100) : null, 8, 30);
    }

    /**
     * Crea un usuario con su rol vigente y lo establece como su rol por defecto.
     *
     * @param nombre El nombre del usuario.
     * @param apellido El apellido del usuario.
     * @param dni El DNI del usuario.
     * @param correo El correo electrónico del usuario.
     * @param rol El rol del usuario.
     * @return El usuario guardado.
     */
    private Usuario crearUsuario(String nombre, String apellido, String dni, String correo, String telefono, Rol rol) {
        Usuario usuario = new Usuario(nombre, apellido, dni, correo, passwordEncoder.encode(CLAVE_DE_PRUEBA));
        usuario.setTelefono(telefono);
        usuario.setEmailValidado(true);
        usuario.setRolPorDefecto(rol);
        usuario = usuarioRepositorio.save(usuario);
        rolUsuarioRepositorio.save(new RolUsuario(rol, usuario));
        return usuario;
    }

    private Docente crearDocente(String nombre, String apellido, String dni, String correo, Rol rol,
            int aniosExperiencia, String matriculaCnv, String biografia) {
        Usuario usuario = crearUsuario(nombre, apellido, dni, correo, "+54 11 4000000", rol);
        Docente docente = new Docente(usuario);
        docente.setAniosExperiencia(aniosExperiencia);
        docente.setMatriculaCnv(matriculaCnv);
        docente.setBiografia(biografia);
        docente.setHabilitado(true);
        return docenteRepositorio.save(docente);
    }

    private Alumno crearAlumno(String nombre, String apellido, String dni, String correo, Rol rol) {
        return alumnoRepositorio.save(new Alumno(crearUsuario(nombre, apellido, dni, correo, null, rol)));
    }

    private Categoria crearCategoria(String nombre, String descripcion) {
        Categoria categoria = new Categoria(nombre);
        categoria.setDescripcion(descripcion);
        return categoria;
    }

    /**
     * Crea un curso con sus modalidades y su equipo docente.
     *
     * @return La participación del docente titular (de la que se obtiene el curso).
     */
    private DocenteCurso crearCurso(String nombre, String descripcion, float precio, String imagen,
            Categoria categoria, Nivel nivel, boolean emiteCertificado, List<Modalidad> modalidades,
            Docente titular, List<Docente> ayudantes) {
        Curso curso = new Curso(nivel, categoria, nombre, precio, CARGA_HORARIA_SEMILLA_MINUTOS, DURACION_SEMILLA_DIAS);
        curso.setDescripcion(descripcion);
        curso.setImagen(imagen);
        curso.setEmiteCertificado(emiteCertificado);
        curso = cursoRepositorio.save(curso);
        for (Modalidad modalidad : modalidades) {
            cursoModalidadRepositorio.save(new CursoModalidad(curso, modalidad));
        }
        for (Docente ayudante : ayudantes) {
            docenteCursoRepositorio.save(new DocenteCurso(curso, ayudante, false));
        }
        return docenteCursoRepositorio.save(new DocenteCurso(curso, titular, true));
    }

    /** Crea el programa de un curso; los objetivos que se indican son los del curso (si todavía no los tiene). */
    private Programa crearPrograma(Curso curso, String nombre, String objetivos) {
        if (curso.getObjetivos() == null) {
            curso.setObjetivos(objetivos);
            cursoRepositorio.save(curso);
        }
        Programa programa = new Programa(curso, nombre);
        programa.setDescripcion("Plan de formación del curso " + curso.getNombre() + ".");
        return programaRepositorio.save(programa);
    }

    /**
     * Agrega un tema al curso y a su cronograma. Si se indica una participación docente, carga
     * además un material publicado asociado al tema.
     */
    private Tema agregarTema(Programa programa, Curso curso, int numeroOrden, int semanas, String titulo,
            String descripcion, DocenteCurso autorMaterial, TipoMaterial tipoMaterial) {
        Tema tema = new Tema(curso, titulo, "Contenido del tema: " + titulo + ". " + descripcion);
        tema.setDescripcion(descripcion);
        tema = temaRepositorio.save(tema);
        // La semilla describe los temas en semanas; el cronograma se guarda en días
        int dias = semanas * DuracionUtilidad.DIAS_POR_SEMANA;
        temaCronogramaRepositorio.save(new TemaCronograma(programa, tema, numeroOrden, dias));
        // La duración del curso es la del más largo de sus programas (así sus cohortes tienen el acceso necesario)
        int totalDelPrograma = diasPorPrograma.merge(programa.getIdPrograma(), dias, Integer::sum);
        int duracion = duracionMaximaDelCurso.merge(curso.getIdCurso(), totalDelPrograma, Math::max);
        if (curso.getDuracionDias() != duracion) {
            curso.setDuracionDias(duracion);
            cursoRepositorio.save(curso);
        }
        if (autorMaterial != null) {
            Material material = new Material(autorMaterial, tipoMaterial, tema, "Material: " + titulo);
            material.setContenido("Material de estudio del tema " + titulo + ".");
            materialRepositorio.save(material);
        }
        return tema;
    }

    private Cohorte crearCohorte(Programa programa, LocalDateTime inicioInscripcion, LocalDateTime finInscripcion,
            LocalDateTime inicioDictado, LocalDateTime finDictado, int semanasAcceso, Integer cupoMaximo) {
        return crearCohorteConEstado(programa.getCurso(), programa, inicioInscripcion, finInscripcion, inicioDictado, finDictado,
                semanasAcceso, cupoMaximo, EstadoCohorte.ACTIVA);
    }

    private Cohorte crearCohorteConEstado(Curso curso, Programa programa, LocalDateTime inicioInscripcion,
            LocalDateTime finInscripcion, LocalDateTime inicioDictado, LocalDateTime finDictado, int semanasAcceso,
            Integer cupoMaximo, String estado) {
        Cohorte cohorte = new Cohorte(curso, programa, inicioInscripcion, finInscripcion,
                semanasAcceso * DuracionUtilidad.DIAS_POR_SEMANA);
        cohorte.setFechaInicioDictado(inicioDictado);
        cohorte.setFechaFinDictado(finDictado);
        cohorte.setCupoMaximo(cupoMaximo);
        cohorte.setEstadoCohorte(estadoCohorteRepositorio.findByNombre(estado).orElseThrow());
        return cohorteRepositorio.save(cohorte);
    }

    /**
     * Inscribe a un alumno en una cohorte y, si se indica un tema, la registra como completada.
     */
    /**
     * Registra el pago de una inscripción (el precio del curso, sin descuento) y, si está aprobado, su comprobante.
     */
    private Pago registrarPago(Inscripcion inscripcion, EstadoPago estado, MetodoPago metodo, String detalle, String digitos,
            String referencia, String motivoError, LocalDateTime fecha) {
        Pago pago = new Pago(estado, metodo, inscripcion, "IDN-SEMILLA-" + inscripcion.getIdInscripcion(),
                inscripcion.getCurso().getPrecio());
        pago.setDetalleMetodoPago(detalle);
        pago.setUltimosDigitosTarjeta(digitos);
        // Las transferencias llevan su número de comprobante; MODO, su código de referencia
        if (MetodoPago.TRANSFERENCIA.equals(metodo.getNombre())) {
            pago.setNumeroComprobante(referencia);
        } else {
            pago.setCodigoReferencia(referencia);
        }
        pago.setDetalleErrorOperacion(motivoError);
        pago.setFechaCreacion(fecha);
        pago.setUltimaModificacion(fecha);
        boolean aprobado = EstadoPago.APROBADO.equals(estado.getNombre());
        if (aprobado) {
            pago.setFechaAprobacion(fecha);
        }
        pago = pagoRepositorio.save(pago);
        if (aprobado) {
            Usuario usuario = inscripcion.getAlumno().getUsuario();
            comprobanteRepositorio.save(new Comprobante(pago, Comprobante.generarNumero(pago.getIdPago(), fecha),
                    fecha, usuario.getNombreCompleto(), usuario.getDni()));
        }
        return pago;
    }

    private Inscripcion crearInscripcion(Cohorte cohorte, Alumno alumno, LocalDateTime ahora, Tema temaCompletada) {
        Inscripcion inscripcion = new Inscripcion(cohorte, alumno, ahora.plusDays(cohorte.getAccesoDias()));
        inscripcion.setHabilitado(true);
        inscripcion = inscripcionRepositorio.save(inscripcion);
        if (temaCompletada != null) {
            Progreso progreso = new Progreso(temaCompletada, inscripcion);
            progreso.setCompletada(true);
            progreso.setFechaCompletada(ahora);
            progresoRepositorio.save(progreso);
        }
        return inscripcion;
    }

    private Material agregarMaterial(Tema tema, DocenteCurso autor, TipoMaterial tipo, String titulo) {
        Material material = new Material(autor, tipo, tema, titulo);
        material.setAutor(autor.getDocente().getUsuario().getNombreCompleto());
        return materialRepositorio.save(material);
    }

    private void agregarGlosario(Tema tema, String termino, String definicion) {
        terminoGlosarioRepositorio.save(new TerminoGlosario(tema, termino, definicion));
    }

    /**
     * Crea un pool vacío para un tema.
     */
    private Pool crearPool(Tema tema, String nombre) {
        return poolRepositorio.save(new Pool(tema, nombre));
    }

    /**
     * Agrega una pregunta con sus opciones a un pool. Las opciones correctas se indican con un asterisco inicial.
     */
    private Pregunta agregarPregunta(Pool pool, String texto, boolean opcionMultiple, String... opciones) {
        Pregunta pregunta = preguntaRepositorio.save(new Pregunta(pool, texto, opcionMultiple));
        pool.getPreguntas().add(pregunta);
        for (String opcion : opciones) {
            boolean correcta = opcion.startsWith("*");
            pregunta.getOpcionesRespuesta().add(opcionRespuestaRepositorio
                    .save(new OpcionRespuesta(pregunta, correcta ? opcion.substring(1) : opcion, correcta)));
        }
        return pregunta;
    }

    /**
     * Crea una autoevaluación de un tema asociado a un pool, con el tiempo límite en minutos.
     */
    private Autoevaluacion agregarAutoevaluacion(Tema tema, Pool pool, String nombre, int cantidadPreguntas,
            float notaMinima, Integer intentosPermitidos, Integer tiempoLimite, LocalDateTime apertura,
            boolean descuentoPorError, boolean navegacionLibre, boolean permiteSalir) {
        Autoevaluacion autoevaluacion = new Autoevaluacion(tema, nombre, cantidadPreguntas, notaMinima, apertura);
        autoevaluacion.setIntentosPermitidos(intentosPermitidos);
        autoevaluacion.setTiempoLimite(tiempoLimite);
        autoevaluacion.setDescuentoPorError(descuentoPorError);
        autoevaluacion.setNavegacionLibre(navegacionLibre);
        autoevaluacion.setPermiteSalir(permiteSalir);
        autoevaluacion = autoevaluacionRepositorio.save(autoevaluacion);
        autoevaluacion.getAutoevaluacionPools()
                .add(autoevaluacionPoolRepositorio.save(new AutoevaluacionPool(pool, autoevaluacion)));
        tema.getAutoevaluaciones().add(autoevaluacion);
        return autoevaluacion;
    }

    /**
     * Crea, para un tema, un pool de cuatro preguntas (dos de opción única, una de opción múltiple y una de verdadero o
     * falso) y una autoevaluación de tres preguntas que lo usa: cada tema de un programa con cohortes necesita una.
     */
    private Autoevaluacion agregarEvaluacionBasica(Tema tema, int numeroOrden, LocalDateTime ahora) {
        String descripcionTema = "«" + tema.getTitulo() + "»";
        Pool pool = crearPool(tema, "Preguntas del tema");
        agregarPregunta(pool, "¿Cuál de estas afirmaciones sobre " + descripcionTema + " es correcta?", false,
                "*La que describe el concepto central", "La que lo contradice", "La que no tiene relación");
        agregarPregunta(pool, "¿Qué idea corresponde a " + descripcionTema + "?", false,
                "*La que se vio en el material del tema", "Una idea de otra materia", "Ninguna de las anteriores");
        agregarPregunta(pool, "¿Cuáles de estos puntos se trabajan en " + descripcionTema + "?", true,
                "*El concepto central", "*Su aplicación práctica", "Un tema de otro tema", "Un tema sin relación");
        agregarPregunta(pool, "El contenido de " + descripcionTema + " se aplica en la práctica.", false, "*Verdadero", "Falso");
        return agregarAutoevaluacion(tema, pool, "Autoevaluación del tema " + numeroOrden, 3, 6f, 3, 15,
                ahora.minusDays(30), false, true, true);
    }

    /**
     * Registra un intento entregado: sortea las primeras preguntas de los pools de la autoevaluación, responde bien las
     * primeras {@code correctas}, responde mal las siguientes {@code incorrectas} y deja el resto sin responder; luego lo
     * corrige.
     */
    private IntentoAutoevaluacion registrarIntento(Inscripcion inscripcion, Autoevaluacion autoevaluacion, int correctas,
            int incorrectas, LocalDateTime fechaEntrega) {
        IntentoAutoevaluacion intento = new IntentoAutoevaluacion(inscripcion, autoevaluacion);
        intento.setFechaCreacion(fechaEntrega.minusMinutes(20));
        intento = intentoAutoevaluacionRepositorio.save(intento);
        int orden = 1;
        for (Pregunta pregunta : autoevaluacion.getPreguntasDisponibles().stream()
                .limit(autoevaluacion.getCantidadPreguntas()).toList()) {
            PreguntaIntento preguntaIntento = preguntaIntentoRepositorio.save(new PreguntaIntento(intento, pregunta, orden));
            intento.getPreguntasIntento().add(preguntaIntento);
            boolean acierta = orden <= correctas;
            if (acierta || orden <= correctas + incorrectas) {
                for (OpcionRespuesta opcion : pregunta.getOpcionesActivas()) {
                    if (opcion.getEsCorrecta() == acierta) {
                        preguntaIntento.getRespuestasIntento()
                                .add(respuestaIntentoRepositorio.save(new RespuestaIntento(preguntaIntento, opcion)));
                        if (!acierta) {
                            break; // una sola opción incorrecta alcanza
                        }
                    }
                }
            }
            orden++;
        }
        intento.setFechaEntrega(fechaEntrega);
        intento.corregir();
        return intentoAutoevaluacionRepositorio.save(intento);
    }
}
