package com.app.idoneos.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import com.app.idoneos.model.Administrador;
import com.app.idoneos.model.Alumno;
import com.app.idoneos.model.Autoevaluacion;
import com.app.idoneos.model.AutoevaluacionPool;
import com.app.idoneos.model.Bibliografia;
import com.app.idoneos.model.Categoria;
import com.app.idoneos.model.CategoriaMaterialLectura;
import com.app.idoneos.model.Certificado;
import com.app.idoneos.model.ClaseEnVivo;
import com.app.idoneos.model.Cohorte;
import com.app.idoneos.model.Comprobante;
import com.app.idoneos.model.ConsultaForo;
import com.app.idoneos.model.Curso;
import com.app.idoneos.model.CursoModalidad;
import com.app.idoneos.model.Descuento;
import com.app.idoneos.model.Docente;
import com.app.idoneos.model.DocenteCurso;
import com.app.idoneos.model.Enlace;
import com.app.idoneos.model.EstadoClaseEnVivo;
import com.app.idoneos.model.EstadoCohorte;
import com.app.idoneos.model.EstadoEnvio;
import com.app.idoneos.model.EstadoGeneracion;
import com.app.idoneos.model.EstadoPago;
import com.app.idoneos.model.Grabacion;
import com.app.idoneos.model.Inscripcion;
import com.app.idoneos.model.IntentoAutoevaluacion;
import com.app.idoneos.model.MaterialLectura;
import com.app.idoneos.model.MetodoPago;
import com.app.idoneos.model.Modalidad;
import com.app.idoneos.model.Nivel;
import com.app.idoneos.model.OpcionRespuesta;
import com.app.idoneos.model.Pago;
import com.app.idoneos.model.Parametro;
import com.app.idoneos.model.Pool;
import com.app.idoneos.model.Pregunta;
import com.app.idoneos.model.PreguntaIntento;
import com.app.idoneos.model.Programa;
import com.app.idoneos.model.Progreso;
import com.app.idoneos.model.Recurso;
import com.app.idoneos.model.RespuestaForo;
import com.app.idoneos.model.RespuestaIntento;
import com.app.idoneos.model.Rol;
import com.app.idoneos.model.RolUsuario;
import com.app.idoneos.model.Tema;
import com.app.idoneos.model.TemaCronograma;
import com.app.idoneos.model.TemaRecurso;
import com.app.idoneos.model.TerminoGlosario;
import com.app.idoneos.model.TipoNotificacion;
import com.app.idoneos.service.recurso.RecursoService.TipoRecurso;
import com.app.idoneos.model.Usuario;
import com.app.idoneos.model.VideoClon;
import com.app.idoneos.repository.AdministradorRepository;
import com.app.idoneos.repository.AlumnoRepository;
import com.app.idoneos.repository.AutoevaluacionPoolRepository;
import com.app.idoneos.repository.AutoevaluacionRepository;
import com.app.idoneos.repository.BibliografiaRepository;
import com.app.idoneos.repository.CategoriaMaterialLecturaRepository;
import com.app.idoneos.repository.CategoriaRepository;
import com.app.idoneos.repository.CertificadoRepository;
import com.app.idoneos.repository.ClaseEnVivoRepository;
import com.app.idoneos.repository.CohorteRepository;
import com.app.idoneos.repository.ComprobanteRepository;
import com.app.idoneos.repository.ConsultaForoRepository;
import com.app.idoneos.repository.CursoModalidadRepository;
import com.app.idoneos.repository.CursoRepository;
import com.app.idoneos.repository.DescuentoRepository;
import com.app.idoneos.repository.DocenteCursoRepository;
import com.app.idoneos.repository.DocenteRepository;
import com.app.idoneos.repository.EnlaceRepository;
import com.app.idoneos.repository.EstadoClaseEnVivoRepository;
import com.app.idoneos.repository.EstadoCohorteRepository;
import com.app.idoneos.repository.EstadoEnvioRepository;
import com.app.idoneos.repository.EstadoGeneracionRepository;
import com.app.idoneos.repository.EstadoPagoRepository;
import com.app.idoneos.repository.GrabacionRepository;
import com.app.idoneos.repository.InscripcionRepository;
import com.app.idoneos.repository.IntentoAutoevaluacionRepository;
import com.app.idoneos.repository.MaterialLecturaRepository;
import com.app.idoneos.repository.MetodoPagoRepository;
import com.app.idoneos.repository.ModalidadRepository;
import com.app.idoneos.repository.NivelRepository;
import com.app.idoneos.repository.OpcionRespuestaRepository;
import com.app.idoneos.repository.PagoRepository;
import com.app.idoneos.repository.ParametroRepository;
import com.app.idoneos.repository.PoolRepository;
import com.app.idoneos.repository.PreguntaIntentoRepository;
import com.app.idoneos.repository.PreguntaRepository;
import com.app.idoneos.repository.ProgramaRepository;
import com.app.idoneos.repository.ProgresoRepository;
import com.app.idoneos.repository.RecursoRepository;
import com.app.idoneos.repository.RespuestaForoRepository;
import com.app.idoneos.repository.RespuestaIntentoRepository;
import com.app.idoneos.repository.RolRepository;
import com.app.idoneos.repository.RolUsuarioRepository;
import com.app.idoneos.repository.TemaCronogramaRepository;
import com.app.idoneos.repository.TemaRecursoRepository;
import com.app.idoneos.repository.TemaRepository;
import com.app.idoneos.repository.TerminoGlosarioRepository;
import com.app.idoneos.repository.TipoNotificacionRepository;
import com.app.idoneos.repository.UsuarioRepository;
import com.app.idoneos.repository.VideoClonRepository;
import com.app.idoneos.service.parametro.ParametroService;
import com.app.idoneos.util.DuracionUtil;
import com.app.idoneos.util.MotivoPagoUtil;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Servicio de carga de semilla (datos iniciales). */
@Service
public class SemillaService {

    /** Carga horaria de los cursos de prueba para el alumno: 10 horas. */
    private static final int CARGA_HORARIA_SEMILLA_MINUTOS = 600;

    /** Duración inicial de los cursos de prueba, hasta que se les cargan los temas: 8 semanas. */
    private static final int DURACION_SEMILLA_DIAS = 56;

    /** Días acumulados del cronograma de cada programa de la semilla, para fijar la duración de sus cursos. */
    private final java.util.Map<Integer, Integer> diasPorPrograma = new java.util.HashMap<>();

    /** La duración del cronograma más largo de cada curso de la semilla. */
    private final java.util.Map<Integer, Integer> duracionMaximaDelCurso = new java.util.HashMap<>();

    /** Contraseña de todos los usuarios de prueba. */
    private static final String CLAVE_DE_PRUEBA = "123456";

    @jakarta.persistence.PersistenceContext
    private jakarta.persistence.EntityManager entityManager;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private RolUsuarioRepository rolUsuarioRepository;

    @Autowired
    private AdministradorRepository administradorRepository;

    @Autowired
    private DocenteRepository docenteRepository;

    @Autowired
    private AlumnoRepository alumnoRepository;

    @Autowired
    private ParametroRepository parametroRepository;

    @Autowired
    private ParametroService parametroService;

    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private ComprobanteRepository comprobanteRepository;

    @Autowired
    private EstadoPagoRepository estadoPagoRepository;

    @Autowired
    private com.app.idoneos.repository.EstadoInscripcionRepository estadoInscripcionRepository;

    @Autowired
    private com.app.idoneos.repository.EstadoAvanceRepository estadoAvanceRepository;

    private com.app.idoneos.model.EstadoInscripcion estadoInscripcion(String nombre) {
        return estadoInscripcionRepository.findByNombre(nombre).orElseThrow();
    }

    private com.app.idoneos.model.EstadoAvance estadoAvance(String nombre) {
        return estadoAvanceRepository.findByNombre(nombre).orElseThrow();
    }

    @Autowired
    private EstadoCohorteRepository estadoCohorteRepository;

    @Autowired
    private MetodoPagoRepository metodoPagoRepository;

    @Autowired
    private DescuentoRepository descuentoRepository;

    @Autowired
    private NivelRepository nivelRepository;

    @Autowired
    private ModalidadRepository modalidadRepository;

    @Autowired
    private CategoriaMaterialLecturaRepository categoriaMaterialLecturaRepository;

    @Autowired
    private EstadoGeneracionRepository estadoGeneracionRepository;

    @Autowired
    private com.app.idoneos.repository.EstadoOCRRepository estadoOcrRepository;

    @Autowired
    private EstadoEnvioRepository estadoEnvioRepository;

    @Autowired
    private TipoNotificacionRepository tipoNotificacionRepository;

    @Autowired
    private EstadoClaseEnVivoRepository estadoClaseEnVivoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private CursoModalidadRepository cursoModalidadRepository;

    @Autowired
    private DocenteCursoRepository docenteCursoRepository;

    @Autowired
    private ProgramaRepository programaRepository;

    @Autowired
    private TemaRepository temaRepository;

    @Autowired
    private TemaCronogramaRepository temaCronogramaRepository;

    @Autowired
    private RecursoRepository recursoRepository;

    @Autowired
    private TemaRecursoRepository temaRecursoRepository;

    @Autowired
    private EnlaceRepository enlaceRepository;

    @Autowired
    private GrabacionRepository grabacionRepository;

    @Autowired
    private MaterialLecturaRepository materialLecturaRepository;

    @Autowired
    private BibliografiaRepository bibliografiaRepository;

    @Autowired
    private VideoClonRepository videoClonRepository;

    /** Carpeta base donde se guardan los archivos de los cursos (la misma que usa la subida de materiales). */
    @Autowired
    @Qualifier("directorioCursos")
    private java.nio.file.Path directorioCursos;

    /** Carpeta con los PDF y los videos de ejemplo que la semilla sube al servidor. */
    @Value("${idoneos.recursos-ejemplo:recursos-ejemplo}")
    private String carpetaDeRecursosDeEjemplo;

    /** Cuántos recursos de cada clase tiene como máximo cada docente: 3 PDF (2 materiales y 1 bibliografía) y 2 videos (1 grabación y 1 con clon). */
    private static final java.util.Map<String, Integer> CUPO_DE_RECURSOS = java.util.Map.of("material", 2, "bibliografia", 1, "grabacion", 1, "clon", 1);

    /** Los recursos que ya creó cada docente, por clase: los temas los reutilizan en lugar de subir uno nuevo cada vez. */
    private final java.util.Map<String, List<Recurso>> recursosDeLosDocentes = new java.util.HashMap<>();

    /** El orden en que aparece cada docente, para darle a cada uno sus propios archivos de ejemplo. */
    private final java.util.Map<Integer, Integer> ordenDeLosDocentes = new java.util.HashMap<>();

    @Autowired
    private CohorteRepository cohorteRepository;

    @Autowired
    private CertificadoRepository certificadoRepository;

    @Autowired
    private InscripcionRepository inscripcionRepository;

    @Autowired
    private ProgresoRepository progresoRepository;

    @Autowired
    private ClaseEnVivoRepository claseEnVivoRepository;

    @Autowired
    private TerminoGlosarioRepository terminoGlosarioRepository;

    @Autowired
    private AutoevaluacionRepository autoevaluacionRepository;

    @Autowired
    private IntentoAutoevaluacionRepository intentoAutoevaluacionRepository;

    @Autowired
    private PoolRepository poolRepository;

    @Autowired
    private PreguntaRepository preguntaRepository;

    @Autowired
    private OpcionRespuestaRepository opcionRespuestaRepository;

    @Autowired
    private AutoevaluacionPoolRepository autoevaluacionPoolRepository;

    @Autowired
    private PreguntaIntentoRepository preguntaIntentoRepository;

    @Autowired
    private RespuestaIntentoRepository respuestaIntentoRepository;

    @Autowired
    private ConsultaForoRepository consultaForoRepository;

    @Autowired
    private RespuestaForoRepository respuestaForoRepository;

    @Autowired
    @org.springframework.context.annotation.Lazy // evita una dependencia circular con los servicios
    private com.app.idoneos.service.cohorte.CohorteService cohorteService;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /** Inserta los datos iniciales en la base de datos cuando el contexto de Spring termina de cargar. */
    @Transactional
    public void insertarSemilla() {
        cursosAbiertosDeLaSemilla.clear();
        String correoAdmin = "admin@idoneos.online";
        if (usuarioRepository.findByCorreo(correoAdmin).isPresent()) {
            return;
        }
        // Los valores predefinidos (niveles, modalidades, roles, estados...) no son datos de demostración: los inserta siempre el script
        // valoresPredefinidos.sql, también con la semilla apagada. Acá solo se buscan.
        Rol rolAdministrador = predefinido(rolRepository.findByNombre("Administrador"), "el rol Administrador");
        Rol rolDocente = predefinido(rolRepository.findByNombre("Docente"), "el rol Docente");
        Rol rolAlumno = predefinido(rolRepository.findByNombre("Alumno"), "el rol Alumno");
        Nivel basico = predefinido(nivelRepository.findByNombre("Básico"), "el nivel Básico");
        Nivel intermedio = predefinido(nivelRepository.findByNombre("Intermedio"), "el nivel Intermedio");
        Nivel avanzado = predefinido(nivelRepository.findByNombre("Avanzado"), "el nivel Avanzado");
        Modalidad enVivo = predefinido(modalidadRepository.findByNombre(Modalidad.EN_VIVO), "la modalidad En vivo");
        Modalidad grabada = predefinido(modalidadRepository.findByNombre(Modalidad.GRABADA), "la modalidad Grabada");
        Modalidad clonIa = predefinido(modalidadRepository.findByNombre(Modalidad.CLON_IA), "la modalidad Con clon de IA");
        TipoRecurso presentacion = TipoRecurso.PRESENTACION;
        TipoRecurso grabacion = TipoRecurso.GRABACION;
        TipoRecurso bibliografia = TipoRecurso.BIBLIOGRAFIA;
        EstadoClaseEnVivo claseProgramada = predefinido(estadoClaseEnVivoRepository.findByNombre("Programada"), "el estado de clase Programada");
        EstadoClaseEnVivo claseEnVivoAhora = predefinido(estadoClaseEnVivoRepository.findByNombre(EstadoClaseEnVivo.EN_VIVO), "el estado de clase En vivo");
        EstadoClaseEnVivo claseFinalizada = predefinido(estadoClaseEnVivoRepository.findByNombre("Finalizada"), "el estado de clase Finalizada");
        EstadoPago pagoPendiente = predefinido(estadoPagoRepository.findByNombre(EstadoPago.PENDIENTE), "el estado de pago Pendiente");
        EstadoPago pagoAprobado = predefinido(estadoPagoRepository.findByNombre(EstadoPago.APROBADO), "el estado de pago Aprobado");
        EstadoPago pagoRechazado = predefinido(estadoPagoRepository.findByNombre(EstadoPago.RECHAZADO), "el estado de pago Rechazado");
        MetodoPago pagoModo = predefinido(metodoPagoRepository.findByNombre(MetodoPago.MODO), "el método de pago MODO");
        MetodoPago pagoTransferencia = predefinido(metodoPagoRepository.findByNombre(MetodoPago.TRANSFERENCIA), "el método de pago Transferencia bancaria");

        // Administrador y parámetros
        Usuario usuarioAdmin = crearUsuario("Admin", "Idóneos", "20111222", correoAdmin, "+54 376 4000000", rolAdministrador);
        Administrador admin = administradorRepository.save(new Administrador(usuarioAdmin));
        parametroRepository.save(new Parametro(admin, Parametro.MINIMO_TEMAS_CON_MATERIAL, "2"));
        parametroRepository.save(new Parametro(admin, Parametro.COHORTE_PLAZO_MINIMO_CONTENIDO_DIAS,
                String.valueOf(Parametro.COHORTE_PLAZO_MINIMO_CONTENIDO_DIAS_POR_DEFECTO)));
        parametroRepository.save(new Parametro(admin, Parametro.CRONOGRAMA_DIAS_POR_TEMA_NUEVO,
                String.valueOf(Parametro.CRONOGRAMA_DIAS_POR_TEMA_NUEVO_POR_DEFECTO)));
        parametroRepository.save(new Parametro(admin, Parametro.PLAZO_CONFIRMACION_PAGO,
                String.valueOf(Parametro.PLAZO_CONFIRMACION_PAGO_POR_DEFECTO)));
        parametroRepository.save(new Parametro(admin, Parametro.CBU_INSTITUCION, "0000003100012345678901"));
        parametroRepository.save(new Parametro(admin, Parametro.COHORTE_RECORDATORIO_DIAS_ANTICIPACION,
                String.valueOf(Parametro.COHORTE_RECORDATORIO_DIAS_ANTICIPACION_POR_DEFECTO)));
        parametroRepository.save(new Parametro(admin, Parametro.PLAZO_EDICION_FORO,
                String.valueOf(Parametro.PLAZO_EDICION_FORO_POR_DEFECTO)));
        parametroRepository.save(new Parametro(admin, Parametro.GRACIA_ENTREGA_AUTOEVALUACION,
                String.valueOf(Parametro.GRACIA_ENTREGA_AUTOEVALUACION_POR_DEFECTO)));
        // Medio de pago MODO: credenciales de PRUEBA (ambiente de preproducción) publicadas en la documentación de MODO.
        // El cliente las reemplaza por las de su cuenta. La contraseña se guarda cifrada.
        parametroService.registrarTexto(admin, Parametro.MODO_USUARIO, "PLAYDIGITAL SA-318979-preprod");
        parametroService.registrarTexto(admin, Parametro.MODO_CLAVE, "318979-P75V/QLKfVKX");
        parametroService.registrarTexto(admin, Parametro.MODO_URL_API, "https://merchants.preprod.playdigital.com.ar");
        parametroService.registrarTexto(admin, Parametro.MODO_URL_CHECKOUT, "https://site.preprod.modo.com.ar/checkout");
        parametroService.registrarTexto(admin, Parametro.MODO_NOMBRE_COMERCIO, "Idoneos Online");
        // Gateway Decidir 2.0/Tradicional y Decidir+ (las tarjetas de prueba de Payway) y una cuota sin interés
        parametroService.registrarTexto(admin, Parametro.MODO_PROCESSOR_CODE, "P1019");
        parametroService.registrarTexto(admin, Parametro.MODO_CC_CODE, "1CSI");
        parametroService.registrarTexto(admin, Parametro.MODO_URL_PUBLICA, "");
        parametroService.registrarTexto(admin, Parametro.MODO_MONTO_MINIMO,
                String.valueOf((int) Parametro.MODO_MONTO_MINIMO_POR_DEFECTO));

        // Descuentos: uno de cada estado (vigentes con y sin condición, próximo, vencido y agotado)
        LocalDateTime hoy = LocalDateTime.now();
        descuentoRepository.save(new Descuento("Descuento de bienvenida", 0, 10, hoy.minusDays(10), hoy.plusDays(60), 100, 0));
        descuentoRepository.save(new Descuento("Alumno frecuente", 2, 20, hoy.minusDays(10), hoy.plusDays(90), 50, 0));
        descuentoRepository.save(new Descuento("Promo de verano", 0, 15, hoy.plusDays(30), hoy.plusDays(90), 30, 0));
        descuentoRepository.save(new Descuento("Lanzamiento", 0, 25, hoy.minusDays(90), hoy.minusDays(30), 20, 8));
        descuentoRepository.save(new Descuento("Preventa agotada", 1, 30, hoy.minusDays(20), hoy.plusDays(20), 5, 5));

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
        Categoria mercadoCapitales = categoriaRepository.save(crearCategoria("Mercado de Capitales",
                "Acciones, bonos, ONs, opciones y operatoria bursátil en BYMA."));
        Categoria macroeconomia = categoriaRepository.save(crearCategoria("Macroeconomía",
                "Análisis de variables macroeconómicas, política monetaria y fiscal."));
        Categoria impuestos = categoriaRepository.save(crearCategoria("Impuestos y Finanzas",
                "Planificación fiscal y tributaria para empresas y personas."));
        Categoria finanzasPersonales = categoriaRepository.save(crearCategoria("Finanzas Personales",
                "Ahorro, inversión y planificación patrimonial."));
        Categoria cuantitativas = categoriaRepository.save(crearCategoria("Finanzas Cuantitativas",
                "Análisis técnico, modelos y herramientas cuantitativas."));
        categoriaRepository.save(crearCategoria("Criptoactivos", "Activos digitales y finanzas descentralizadas."));
        Categoria contabilidad = categoriaRepository.save(crearCategoria("Contabilidad",
                "Registro, lectura e interpretación de la información contable."));
        Categoria flujoDeCaja = categoriaRepository.save(crearCategoria("Eficiencia de Flujo de Caja",
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
        Tema tema1 = agregar(programa1, curso1, 1, 2, "Introducción al mercado de capitales",
                "Actores, instrumentos y funcionamiento del mercado.", titular1, presentacion);
        Tema tema2 = agregar(programa1, curso1, 2, 3, "Renta fija y bonos",
                "Valuación y operatoria de bonos soberanos.", titular1, presentacion);
        Tema tema3 = agregar(programa1, curso1, 3, 4, "Renta variable y opciones",
                "Acciones, CEDEARs y estrategias con opciones.", null, null);
        Cohorte abierta1 = crearCohorte(programa1, ahora.minusDays(10), ahora.plusDays(20), ahora.plusDays(25),
                ahora.plusDays(90), 12, 30);
        Cohorte enDictado1 = crearCohorte(programa1, ahora.minusDays(60), ahora.minusDays(30), ahora.minusDays(20),
                ahora.plusDays(40), 12, 25);
        crearCohorte(programa1, ahora.minusDays(200), ahora.minusDays(170), ahora.minusDays(160), ahora.minusDays(80), 12, 25);
        Inscripcion luciaMercado = crearInscripcion(abierta1, lucia, ahora, tema1);
        Inscripcion martinMercado = crearInscripcion(abierta1, martin, ahora, null);
        Inscripcion inscripcionValentina = crearInscripcion(enDictado1, valentina, ahora, tema1);
        // Pagos de las inscripciones a un curso con costo: aprobados por MODO, con su comprobante
        registrarPago(luciaMercado, pagoAprobado, pagoModo, "Tarjeta de crédito Mastercard", "0008", "QRL3M0A1", null,
                ahora.minusDays(9));
        registrarPago(martinMercado, pagoAprobado, pagoModo, "Cuenta bancaria Macro", "6168", "QRM4R7B2", null,
                ahora.minusDays(8));
        registrarPago(inscripcionValentina, pagoAprobado, pagoModo, "Tarjeta de crédito Visa", "0008", "QRV9A1C3", null,
                ahora.minusDays(55));
        claseEnVivoRepository.save(new ClaseEnVivo(titular1, claseProgramada, tema1, enDictado1,
                "Clase 1: Panorama del mercado de capitales", ahora.plusDays(3), 90));
        claseEnVivoRepository.save(new ClaseEnVivo(titular1, claseFinalizada, tema1, enDictado1,
                "Clase 0: Presentación del curso", ahora.minusDays(15), 60));
        claseEnVivoRepository.save(new ClaseEnVivo(titular1, claseEnVivoAhora, tema2, enDictado1,
                "Clase magistral: Operatoria de renta fija en BYMA", ahora.minusMinutes(20), 90));

        // Contenido de los temas 1 y 2 (acceso al curso del alumno en dictado)
        agregarMaterial(tema1, titular1, grabacion, "Grabación: Estructura del mercado argentino");
        agregarMaterial(tema1, titular1, bibliografia, "Ley 26.831 y modificatorias CNV");
        agregarMaterial(tema2, titular1, bibliografia, "Guía teórica de renta fija");
        TemaRecurso borrador = agregarMaterial(tema2, titular1, presentacion, "Borrador: ejercicios de duration");
        if (borrador != null) {
            borrador.setOculto(true);
        }
        agregarGlosario(tema1, "BYMA", "Bolsas y Mercados Argentinos: el mercado donde se negocian los valores.");
        agregarGlosario(tema1, "CNV", "Comisión Nacional de Valores: organismo que regula y controla el mercado de capitales.");
        agregarGlosario(tema1, "ON", "Obligación negociable: título de deuda que emiten las empresas para financiarse.");
        agregarGlosario(tema2, "TIR", "Tasa interna de retorno: rendimiento anual de un bono si se lo mantiene hasta el vencimiento.");
        agregarGlosario(tema2, "Duration", "Plazo promedio ponderado en que se recibe el dinero de un bono.");
        // Autoevaluaciones: la 1 deja navegar libremente y salir. La 2 es secuencial, descuenta por error y no deja salir
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
        ConsultaForo consultaCnv = consultaForoRepository.save(new ConsultaForo(tema1, inscripcionValentina,
                "¿La CNV exige matrícula para operar con obligaciones negociables?"));
        consultaCnv.setFechaCreacion(ahora.minusDays(3));
        consultaForoRepository.save(consultaCnv);
        RespuestaForo respuestaCnv = new RespuestaForo(titular1, consultaCnv,
                "Para operar como inversor no, la matrícula la necesitan los agentes. Lo vemos en la clase 2.");
        respuestaCnv.setFechaCreacion(ahora.minusDays(2));
        respuestaForoRepository.save(respuestaCnv);
        ConsultaForo consultaTir = consultaForoRepository.save(new ConsultaForo(tema2, inscripcionValentina,
                "¿Cómo se calcula la duration de un bono con cupones?"));
        consultaTir.setFechaCreacion(ahora.minusHours(5));
        consultaForoRepository.save(consultaTir);

        // Curso 2: Macroeconomía de Coyuntura (grabada), con cohorte abierta sin inscriptos
        DocenteCurso titular2 = crearCurso("Macroeconomía de Coyuntura",
                "Tipo de cambio, inflación y tasas: las variables que mueven la economía argentina.", 0f,
                "macroeconomia-coyuntura.jpg", macroeconomia, basico, true, List.of(grabada), fausto, List.of());
        Curso curso2 = titular2.getCurso();
        Programa programa2 = crearPrograma(curso2, "Programa 2026",
                "Interpretar la coyuntura macroeconómica y su impacto en las decisiones financieras.");
        agregarEvaluacionBasica(agregar(programa2, curso2, 1, 2, "Inflación y precios", "Medición y causas de la inflación.",
                titular2, presentacion), 1, ahora);
        agregarEvaluacionBasica(agregar(programa2, curso2, 2, 2, "Tipo de cambio", "Regímenes cambiarios y brecha.",
                titular2, presentacion), 2, ahora);
        agregarEvaluacionBasica(agregar(programa2, curso2, 3, 2, "Tasas y política monetaria", "Instrumentos del BCRA.",
                titular2, presentacion), 3, ahora);
        crearCohorte(programa2, ahora.minusDays(5), ahora.plusDays(30), null, null, 8, null);

        // Curso 3: Planificación Fiscal Corporativa (no alcanza el mínimo de temas con material)
        DocenteCurso titular3 = crearCurso("Planificación Fiscal Corporativa",
                "Estrategias de planificación tributaria para empresas: IVA, ganancias y regímenes especiales.", 120000f,
                "planificacion-fiscal-corporativa.jpg", impuestos, avanzado, true, List.of(grabada), sebastian, List.of());
        Curso curso3 = titular3.getCurso();
        Programa programa3 = crearPrograma(curso3, "Programa 2026",
                "Diseñar estructuras fiscales eficientes dentro del marco legal.");
        agregar(programa3, curso3, 1, 3, "Impuesto a las ganancias", "Determinación y planificación.", titular3, presentacion);
        agregar(programa3, curso3, 2, 3, "IVA y regímenes especiales", "Débito, crédito y regímenes.", null, null);

        // Curso 4: Finanzas Personales e Inversión (grabada + clon de IA), con cohorte abierta e inscripción
        DocenteCurso titular4 = crearCurso("Finanzas Personales e Inversión",
                "Organizá tus finanzas, aprendé a ahorrar e invertir con criterio y planificá tu patrimonio.", 80000f,
                "finanzas-personales-inversion.jpg", finanzasPersonales, basico, true,
                List.of(grabada, clonIa), mariano, List.of());
        Curso curso4 = titular4.getCurso();
        Programa programa4 = crearPrograma(curso4, "Programa 2026",
                "Construir un plan financiero personal sólido.");
        Tema tema4 = agregar(programa4, curso4, 1, 2, "Presupuesto y ahorro", "Ordenar ingresos y gastos.",
                titular4, presentacion);
        Autoevaluacion autoevaluacion4 = agregarEvaluacionBasica(tema4, 1, ahora);
        agregarEvaluacionBasica(agregar(programa4, curso4, 2, 3, "Instrumentos de inversión",
                "Plazos fijos, bonos y fondos.", titular4, presentacion), 2, ahora);
        agregarEvaluacionBasica(agregar(programa4, curso4, 3, 2, "Planificación patrimonial",
                "Objetivos y horizonte de inversión.", titular4, presentacion), 3, ahora);
        Cohorte abierta4 = crearCohorte(programa4, ahora.minusDays(2), ahora.plusDays(40), null, null, 10, 20);
        Inscripcion martinFinanzas = crearInscripcion(abierta4, martin, ahora, tema4);
        registrarIntento(martinFinanzas, autoevaluacion4, 3, 0, ahora.minusDays(1));
        registrarPago(martinFinanzas, pagoAprobado, pagoTransferencia, "Transferencia al CBU de la institución", null,
                "TRF-000451", null, ahora.minusDays(1));
        // Curso con costo: la inscripción queda pendiente de pago hasta que se apruebe el pago
        Inscripcion pendienteDePago = crearInscripcion(abierta4, lucia, ahora, null);
        pendienteDePago.setEstadoInscripcion(estadoInscripcion(com.app.idoneos.model.EstadoInscripcion.PENDIENTE_DE_PAGO));
        pendienteDePago.setFechaVencimientoAcceso(null); // el vencimiento se calcula cuando se habilite
        inscripcionRepository.save(pendienteDePago);
        // Su pago con MODO fue rechazado: puede reintentarlo o lo puede acreditar el administrador
        registrarPago(pendienteDePago, pagoRechazado, pagoModo, "Tarjeta de crédito Visa", "0008", null,
                MotivoPagoUtil.traducir("INSUFFICIENT_FUNDS"), ahora.minusHours(3));

        // Curso 5: Análisis Técnico Bursátil (en vivo + grabada), con cohorte próxima
        DocenteCurso titular5 = crearCurso("Análisis Técnico Bursátil",
                "Gráficos, indicadores y patrones para tomar decisiones de compra y venta.", 95000f,
                "analisis-tecnico-bursatil.jpg", cuantitativas, intermedio, true,
                List.of(enVivo, grabada), mariano, List.of(fausto));
        Curso curso5 = titular5.getCurso();
        Programa programa5 = crearPrograma(curso5, "Programa 2026",
                "Aplicar herramientas de análisis técnico a distintos activos.");
        agregarEvaluacionBasica(agregar(programa5, curso5, 1, 3, "Fundamentos del análisis técnico",
                "Tendencias, soportes y resistencias.", titular5, presentacion), 1, ahora);
        agregarEvaluacionBasica(agregar(programa5, curso5, 2, 3, "Indicadores y osciladores",
                "Medias móviles, RSI y MACD.", titular5, presentacion), 2, ahora);
        crearCohorte(programa5, ahora.plusDays(15), ahora.plusDays(45), ahora.plusDays(50), ahora.plusDays(100), 10, null);
        // Cohorte pendiente, registrada por el administrador sin programa: el docente todavía tiene que elegir uno
        crearCohorteConEstado(curso5, null, ahora.plusDays(30), ahora.plusDays(60), ahora.plusDays(65), ahora.plusDays(120), 10,
                null, EstadoCohorte.PENDIENTE);
        // Cohorte cancelada: llegó la fecha de inicio de la inscripción y el contenido no estaba completo
        crearCohorteConEstado(curso5, null, ahora.minusDays(3), ahora.plusDays(25), ahora.plusDays(30), ahora.plusDays(80), 10,
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
        inscripcionVencida.setEstadoAvance(estadoAvance(com.app.idoneos.model.EstadoAvance.FINALIZADO)); // ya tiene su certificado
        inscripcionRepository.save(inscripcionVencida);
        registrarPago(inscripcionVencida, pagoAprobado, pagoModo, "Tarjeta de crédito Mastercard", "5290", "QRX7F9G5", null,
                ahora.minusDays(214));
        certificadoRepository.save(new Certificado(inscripcionVencida, "CERT-2026-000001", ahora.minusDays(55),
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
        cursoRepository.save(curso7);

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

        completarElContenidoDeLosProgramas(ahora);
        dejarIncompletosLaMayoriaDeLosCursosAbiertos(ahora);
        publicarLasCohortes(ahora);
    }

    /** Obtiene un valor predefinido que tiene que haber cargado el script valoresPredefinidos.sql. */
    private static <T> T predefinido(java.util.Optional<T> valor, String descripcion) {
        return valor.orElseThrow(() -> new IllegalStateException("Falta " + descripcion + ": lo carga el script valoresPredefinidos.sql"));
    }

    /** Publica las cohortes. Es lo último que hace la semilla. */
    private void publicarLasCohortes(LocalDateTime ahora) {
        int plazo = Parametro.COHORTE_PLAZO_MINIMO_CONTENIDO_DIAS_POR_DEFECTO;
        // La semilla corre en una sola transacción y agrega materiales y evaluaciones por los repositorios.
        entityManager.flush();
        entityManager.clear();
        for (Cohorte cohorte : cohorteRepository.findByBajaFalse()) {
            if (cohorte.esCancelada() || (cohorte.esPendiente() && cohorte.getPrograma() == null)) {
                continue;
            }
            // Se cuentan en la base: la lista de inscripciones de la cohorte puede estar desactualizada
            boolean sinAlumnos = inscripcionRepository.findByCohorteAndBajaFalse(cohorte).isEmpty();
            if (sinAlumnos && !cohorteService.faltantesDeContenido(cohorte.getPrograma()).isEmpty()) {
                cohorte.setPrograma(null);
                cohorte.setFechaInicioInscripcion(ahora.plusDays(plazo + 30L));
                cohorte.setFechaFinInscripcion(ahora.plusDays(plazo + 75L));
                if (cohorte.getFechaInicioDictado() != null) {
                    cohorte.setFechaInicioDictado(ahora.plusDays(plazo + 90L));
                    cohorte.setFechaFinDictado(ahora.plusDays(plazo + 90L + cohorte.getCurso().getDuracionDias() - 1));
                }
                cohorteRepository.saveAndFlush(cohorte);
                continue;
            }
            // Con el programa completo, la semilla finaliza el diseño por el docente: la cohorte sale de pendiente
            if (cohorte.esPendiente()) {
                cohorte.setEstadoCohorte(estadoCohorteRepository.findByNombre(EstadoCohorte.PROXIMA).orElseThrow());
            }
            boolean enJuego = inscripcionRepository.contarEnJuego(cohorte, ahora) > 0;
            cohorte.setEstadoCohorte(estadoCohorteRepository.findByNombre(cohorte.calcularEstado(ahora, enJuego)).orElseThrow());
            cohorteRepository.saveAndFlush(cohorte);
        }
    }

    /** Los cursos con inscripción abierta que crea la semilla, en orden. */
    private final List<Curso> cursosAbiertosDeLaSemilla = new java.util.ArrayList<>();

    /** Deja incompletos la mayoría de los cursos abiertos, para poder probar el diseño de curso. */
    private void dejarIncompletosLaMayoriaDeLosCursosAbiertos(LocalDateTime ahora) {
        int etapa = 0;
        for (int i = 0; i < cursosAbiertosDeLaSemilla.size(); i++) {
            if (i % 3 == 0) {
                continue;
            }
            Curso curso = cursosAbiertosDeLaSemilla.get(i);
            List<Programa> programas = programaRepository.findByCursoAndBajaFalse(curso);
            List<Cohorte> cohortes = cohorteRepository.findByCursoAndBajaFalse(curso);
            int plazo = Parametro.COHORTE_PLAZO_MINIMO_CONTENIDO_DIAS_POR_DEFECTO;
            for (Cohorte cohorte : cohortes) {
                cohorte.setEstadoCohorte(estadoCohorteRepository.findByNombre(EstadoCohorte.PENDIENTE).orElseThrow());
                cohorte.setPrograma(null);
                cohorte.setFechaInicioInscripcion(ahora.plusDays(plazo + 30L));
                cohorte.setFechaFinInscripcion(ahora.plusDays(plazo + 75L));
                if (cohorte.getFechaInicioDictado() != null) {
                    cohorte.setFechaInicioDictado(ahora.plusDays(plazo + 90L));
                    cohorte.setFechaFinDictado(ahora.plusDays(plazo + 90L + curso.getDuracionDias() - 1));
                }
                cohorteRepository.saveAndFlush(cohorte);
            }
            switch (etapa % 4) {
                case 0 -> {
                    // Paso 1: al curso le faltan datos
                    curso.setObjetivos(null);
                    cursoRepository.save(curso);
                }
                case 1 -> {
                    // Paso 3: los temas están completos y todavía no hay programa
                    for (Programa programa : programas) {
                        programa.marcarInactivo();
                        programaRepository.saveAndFlush(programa);
                    }
                }
                case 2 -> {
                    // Paso 2: a los temas les falta contenido (sin autoevaluación visible).
                    for (Programa programa : programas) {
                        List<TemaCronograma> cronogramas = temaCronogramaRepository.findByProgramaAndBajaFalse(programa);
                        programa.marcarInactivo();
                        programaRepository.saveAndFlush(programa);
                        for (TemaCronograma cronograma : cronogramas) {
                            autoevaluacionRepository.findByTemaAndBajaFalse(cronograma.getTema()).forEach(autoevaluacion -> {
                                autoevaluacion.setOculto(true);
                                autoevaluacionRepository.saveAndFlush(autoevaluacion);
                            });
                        }
                    }
                }
                default -> {
                    // Paso 3: los temas están completos y el programa está registrado, pero la cohorte todavía no lo tiene asignado
                }
            }
            etapa++;
        }
    }

    /** Deja el contenido de todos los programas de la semilla completo y coherente con las modalidades de su curso: cada tema tiene su material, su autoevaluación y, si el curso es grabado o con clon, su grabación o su video con clon. */
    private void completarElContenidoDeLosProgramas(LocalDateTime ahora) {
        for (Curso curso : cursoRepository.findAll()) {
            DocenteCurso titular = docenteCursoRepository.findByCursoAndBajaFalse(curso).stream()
                    .filter(DocenteCurso::getEsTitular).findFirst().orElse(null);
            if (curso.getBaja() || titular == null) {
                continue;
            }
            List<String> modalidades = cursoModalidadRepository.findAll().stream()
                    .filter(cm -> cm.getCurso().getIdCurso() == curso.getIdCurso()).map(cm -> cm.getModalidad().getNombre()).toList();
            boolean grabado = modalidades.contains(Modalidad.GRABADA);
            boolean conClon = modalidades.contains(Modalidad.CLON_IA);
            for (Programa programa : programaRepository.findByCursoAndBajaFalse(curso)) {
                for (TemaCronograma cronograma : temaCronogramaRepository.findByProgramaAndBajaFalse(programa)) {
                    Tema tema = cronograma.getTema();
                    List<TemaRecurso> publicados = temaRecursoRepository.findByTemaAndBajaFalse(tema).stream()
                            .filter(TemaRecurso::estaPublicado).toList();
                    if (publicados.stream().noneMatch(m -> m.getModalidadAsociada() == null)) {
                        agregarMaterial(tema, titular, TipoRecurso.PRESENTACION, "Material: " + tema.getTitulo());
                    }
                    if (grabado && publicados.stream().noneMatch(m -> Modalidad.GRABADA.equals(m.getModalidadAsociada()))) {
                        agregarMaterial(tema, titular, TipoRecurso.GRABACION, "Clase grabada: " + tema.getTitulo());
                    }
                    if (conClon && publicados.stream().noneMatch(m -> m.getRecurso().getVideoClon() != null)) {
                        agregarVideoClon(tema, titular, "Video con clon: " + tema.getTitulo());
                    }
                    if (autoevaluacionRepository.findByTemaAndBajaFalse(tema).stream().noneMatch(a -> !a.getOculto())) {
                        agregarEvaluacionBasica(tema, cronograma.getNumeroOrden(), ahora);
                    }
                }
            }
        }
    }

    /** Busca los archivos de ejemplo de una clase ("pdf" o "mp4"), ordenados por nombre. Devuelve vacío si la carpeta no existe. */
    private List<java.nio.file.Path> archivosDeEjemplo(String extension) {
        java.nio.file.Path carpeta = java.nio.file.Path.of(carpetaDeRecursosDeEjemplo);
        if (!java.nio.file.Files.isDirectory(carpeta)) {
            return List.of();
        }
        try (java.util.stream.Stream<java.nio.file.Path> archivos = java.nio.file.Files.list(carpeta)) {
            return archivos.filter(a -> a.getFileName().toString().toLowerCase().endsWith("." + extension)).sorted().toList();
        } catch (java.io.IOException e) {
            return List.of();
        }
    }

    /** Genera un PDF de ejemplo con tres capítulos y sus subtítulos como marcadores, para probar el visor y el índice. */
    private void generarPdfDeEjemplo(java.nio.file.Path destino, String titulo) throws java.io.IOException {
        String[] capitulos = { "Introducción", "Conceptos principales", "Aplicación práctica" };
        String parrafo = "Los mercados financieros permiten asignar el capital entre quienes ahorran y quienes necesitan financiarse. "
                + "En este material se recorren los instrumentos más utilizados, los riesgos asociados a cada uno y los criterios "
                + "para comparar su rendimiento esperado con el plazo, la liquidez y la calidad crediticia del emisor.";
        com.lowagie.text.Document documento = new com.lowagie.text.Document(com.lowagie.text.PageSize.A4, 56, 56, 56, 56);
        try (java.io.OutputStream salida = java.nio.file.Files.newOutputStream(destino)) {
            com.lowagie.text.pdf.PdfWriter.getInstance(documento, salida);
            documento.open();
            com.lowagie.text.Font fuenteTitulo = com.lowagie.text.FontFactory.getFont(com.lowagie.text.FontFactory.HELVETICA_BOLD, 20);
            com.lowagie.text.Font fuenteCapitulo = com.lowagie.text.FontFactory.getFont(com.lowagie.text.FontFactory.HELVETICA_BOLD, 16);
            com.lowagie.text.Font fuenteSeccion = com.lowagie.text.FontFactory.getFont(com.lowagie.text.FontFactory.HELVETICA_BOLD, 12);
            com.lowagie.text.Font fuenteTexto = com.lowagie.text.FontFactory.getFont(com.lowagie.text.FontFactory.HELVETICA, 11);
            documento.add(new com.lowagie.text.Paragraph(titulo, fuenteTitulo));
            for (int c = 0; c < capitulos.length; c++) {
                com.lowagie.text.Chapter capitulo = new com.lowagie.text.Chapter(
                        new com.lowagie.text.Paragraph((c + 1) + ". " + capitulos[c], fuenteCapitulo), c + 1);
                capitulo.setNumberDepth(0);
                for (int s = 1; s <= 2; s++) {
                    com.lowagie.text.Section seccion = capitulo.addSection(
                            new com.lowagie.text.Paragraph((c + 1) + "." + s + " " + capitulos[c] + ", parte " + s, fuenteSeccion));
                    seccion.setNumberDepth(0);
                    for (int i = 0; i < 3; i++) {
                        seccion.add(new com.lowagie.text.Paragraph(parrafo, fuenteTexto));
                    }
                }
                documento.add(capitulo);
            }
            documento.close();
        } catch (com.lowagie.text.DocumentException e) {
            throw new java.io.IOException(e);
        }
    }

    /** Sube al servidor un archivo de ejemplo (el que corresponde al índice) con la ruta que usa la biblioteca del docente. */
    private Object[] subirArchivoDeEjemplo(Recurso recurso, String extension, int indice) {
        List<java.nio.file.Path> disponibles = archivosDeEjemplo(extension);
        String relativa = "biblioteca/docente" + recurso.getDocente().getIdDocente() + "/recurso" + recurso.getIdRecurso()
                + "." + extension;
        if (disponibles.isEmpty()) {
            // Sin carpeta de ejemplos, los PDF se generan: así los materiales de la semilla siempre se pueden abrir
            if ("pdf".equals(extension)) {
                java.nio.file.Path destinoGenerado = directorioCursos.resolve(relativa);
                try {
                    java.nio.file.Files.createDirectories(destinoGenerado.getParent());
                    generarPdfDeEjemplo(destinoGenerado, recurso.getTitulo());
                    return new Object[] { relativa, java.nio.file.Files.size(destinoGenerado) };
                } catch (java.io.IOException e) {
                    throw new IllegalStateException("No se pudo generar el PDF de ejemplo " + relativa, e);
                }
            }
            return new Object[] { relativa, null };
        }
        java.nio.file.Path origen = disponibles.get(indice % disponibles.size());
        java.nio.file.Path destino = directorioCursos.resolve(relativa);
        try {
            java.nio.file.Files.createDirectories(destino.getParent());
            java.nio.file.Files.deleteIfExists(destino);
            try {
                // Un vínculo al archivo de ejemplo: son muchos recursos y no hace falta repetir los mismos bytes en el disco
                java.nio.file.Files.createLink(destino, origen);
            } catch (java.io.IOException | UnsupportedOperationException e) {
                java.nio.file.Files.copy(origen, destino);
            }
            return new Object[] { relativa, java.nio.file.Files.size(destino) };
        } catch (java.io.IOException e) {
            throw new IllegalStateException("No se pudo subir el archivo de ejemplo " + origen, e);
        }
    }

    /** Incorpora al tema un video con el clon del docente, ya generado: el docente tiene uno solo y los temas lo reutilizan. */
    private TemaRecurso agregarVideoClon(Tema tema, DocenteCurso autor, String titulo) {
        Docente docente = autor.getDocente();
        Recurso recurso = recursoParaElTema(tema, docente, "clon", () -> crearVideoClon(docente, tema, titulo));
        return recurso == null ? null : vincular(tema, recurso, autor);
    }

    private Recurso crearVideoClon(Docente docente, Tema tema, String titulo) {
        Recurso recurso = recursoRepository.save(new Recurso(docente, titulo));
        VideoClon video = new VideoClon(recurso,
                estadoGeneracionRepository.findByNombre(EstadoGeneracion.GENERADO).orElseThrow(),
                "Hola, soy " + docente.getUsuario().getNombreCompleto() + ". En esta clase vemos " + tema.getTitulo() + ".");
        Object[] archivo = subirArchivoDeEjemplo(recurso, "mp4", indiceDeArchivo(docente, "mp4"));
        video.setRutaArchivo((String) archivo[0]);
        video.setTamanioBytes((Long) archivo[1]);
        recurso.setVideoClon(videoClonRepository.save(video));
        return recurso;
    }

    /** Crea un curso sin imagen con un programa de dos temas con material y una cohorte con inscripción abierta. */
    private void crearCursoAbierto(String nombre, String descripcion, float precio, Categoria categoria, Nivel nivel,
            List<Modalidad> modalidades, Docente titular, TipoRecurso tipoMaterial, String tema1, String tema2,
            LocalDateTime ahora) {
        DocenteCurso participacion = crearCurso(nombre, descripcion, precio, null, categoria, nivel, true,
                modalidades, titular, List.of());
        Curso curso = participacion.getCurso();
        cursosAbiertosDeLaSemilla.add(curso);
        Programa programa = crearPrograma(curso, "Programa 2026", "Dominar los contenidos centrales del curso.");
        agregarEvaluacionBasica(agregar(programa, curso, 1, 2, tema1, "Conceptos centrales.", participacion,
                tipoMaterial), 1, ahora);
        agregarEvaluacionBasica(agregar(programa, curso, 2, 2, tema2, "Aplicación práctica.", participacion,
                tipoMaterial), 2, ahora);
        boolean enVivo = modalidades.stream().anyMatch(modalidad -> Modalidad.EN_VIVO.equals(modalidad.getNombre()));
        crearCohorte(programa, ahora.minusDays(1), ahora.plusDays(45), enVivo ? ahora.plusDays(50) : null,
                enVivo ? ahora.plusDays(100) : null, 8, 30);
    }

    /** Crea un usuario con su rol vigente y lo establece como su rol por defecto. */
    private Usuario crearUsuario(String nombre, String apellido, String dni, String correo, String telefono, Rol rol) {
        Usuario usuario = new Usuario(nombre, apellido, dni, correo, passwordEncoder.encode(CLAVE_DE_PRUEBA));
        usuario.setTelefono(telefono);
        usuario.setEmailValidado(true);
        usuario.setRolPorDefecto(rol);
        usuario = usuarioRepository.save(usuario);
        rolUsuarioRepository.save(new RolUsuario(rol, usuario));
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
        return docenteRepository.save(docente);
    }

    private Alumno crearAlumno(String nombre, String apellido, String dni, String correo, Rol rol) {
        return alumnoRepository.save(new Alumno(crearUsuario(nombre, apellido, dni, correo, null, rol)));
    }

    private Categoria crearCategoria(String nombre, String descripcion) {
        Categoria categoria = new Categoria(nombre);
        categoria.setDescripcion(descripcion);
        return categoria;
    }

    /** Crea un curso con sus modalidades y su equipo docente. */
    private DocenteCurso crearCurso(String nombre, String descripcion, float precio, String imagen,
            Categoria categoria, Nivel nivel, boolean emiteCertificado, List<Modalidad> modalidades,
            Docente titular, List<Docente> ayudantes) {
        Curso curso = new Curso(nivel, categoria, nombre, precio, CARGA_HORARIA_SEMILLA_MINUTOS, DURACION_SEMILLA_DIAS);
        curso.setDescripcion(descripcion);
        curso.setImagen(imagen);
        curso.setEmiteCertificado(emiteCertificado);
        curso = cursoRepository.save(curso);
        for (Modalidad modalidad : modalidades) {
            cursoModalidadRepository.save(new CursoModalidad(curso, modalidad));
        }
        for (Docente ayudante : ayudantes) {
            docenteCursoRepository.save(new DocenteCurso(curso, ayudante, false));
        }
        return docenteCursoRepository.save(new DocenteCurso(curso, titular, true));
    }

    /** Crea el programa de un curso. Los objetivos que se indican son los del curso (si todavía no los tiene). */
    private Programa crearPrograma(Curso curso, String nombre, String objetivos) {
        if (curso.getObjetivos() == null) {
            curso.setObjetivos(objetivos);
            cursoRepository.save(curso);
        }
        Programa programa = new Programa(curso, nombre);
        programa.setDescripcion("Plan de formación del curso " + curso.getNombre() + ".");
        return programaRepository.save(programa);
    }

    /** Agrega un tema al curso y a su cronograma. */
    private Tema agregar(Programa programa, Curso curso, int numeroOrden, int semanas, String titulo,
            String descripcion, DocenteCurso autorMaterial, TipoRecurso tipoMaterial) {
        Tema tema = new Tema(curso, titulo, "Contenido del tema: " + titulo + ". " + descripcion);
        tema.setDescripcion(descripcion);
        tema = temaRepository.save(tema);
        // La semilla describe los temas en semanas. El cronograma se guarda en días
        int dias = semanas * DuracionUtil.DIAS_POR_SEMANA;
        temaCronogramaRepository.save(new TemaCronograma(programa, tema, numeroOrden, dias));
        // La duración del curso es la del más largo de sus programas (así sus cohortes tienen el acceso necesario)
        int totalDelPrograma = diasPorPrograma.merge(programa.getIdPrograma(), dias, Integer::sum);
        int duracion = duracionMaximaDelCurso.merge(curso.getIdCurso(), totalDelPrograma, Math::max);
        if (curso.getDuracionDias() != duracion) {
            curso.setDuracionDias(duracion);
            // Se guarda ya: el disparador de PostgreSQL no deja cambiar la duración cuando hay inscripciones
            cursoRepository.saveAndFlush(curso);
        }
        if (autorMaterial != null) {
            agregarMaterial(tema, autorMaterial, tipoMaterial, "Material: " + titulo);
        }
        return tema;
    }

    private Cohorte crearCohorte(Programa programa, LocalDateTime inicioInscripcion, LocalDateTime finInscripcion,
            LocalDateTime inicioDictado, LocalDateTime finDictado, int semanasAcceso, Integer cupoMaximo) {
        return crearCohorteConEstado(programa.getCurso(), programa, inicioInscripcion, finInscripcion, inicioDictado, finDictado,
                semanasAcceso, cupoMaximo, null);
    }

    private Cohorte crearCohorteConEstado(Curso curso, Programa programa, LocalDateTime inicioInscripcion,
            LocalDateTime finInscripcion, LocalDateTime inicioDictado, LocalDateTime finDictado, int semanasAcceso,
            Integer cupoMaximo, String estado) {
        Cohorte cohorte = new Cohorte(curso, programa, inicioInscripcion, finInscripcion,
                semanasAcceso * DuracionUtil.DIAS_POR_SEMANA);
        cohorte.setFechaInicioDictado(inicioDictado);
        cohorte.setFechaFinDictado(finDictado);
        cohorte.setCupoMaximo(cupoMaximo);
        // Sin estado indicado: la cohorte sigue el calendario.
        cohorte.setEstadoCohorte(estadoCohorteRepository.findByNombre(estado != null ? estado : EstadoCohorte.PENDIENTE).orElseThrow());
        return cohorteRepository.save(cohorte);
    }

    /** Inscribe a un alumno en una cohorte y, si se indica un tema, la registra como completada. */
    /** Registra el pago de una inscripción (el precio del curso, sin descuento) y, si está aprobado. */
    private Pago registrarPago(Inscripcion inscripcion, EstadoPago estado, MetodoPago metodo, String detalle, String digitos,
            String referencia, String motivoError, LocalDateTime fecha) {
        Pago pago = new Pago(estado, metodo, inscripcion, "IDN-SEMILLA-" + inscripcion.getIdInscripcion(),
                inscripcion.getCurso().getPrecio());
        pago.setDetalleMetodoPago(detalle);
        pago.setUltimosDigitosTarjeta(digitos);
        // Las transferencias llevan su número de comprobante. MODO, su código de referencia
        if (MetodoPago.TRANSFERENCIA.equals(metodo.getNombre())) {
            pago.setNumeroComprobante(referencia);
        } else {
            pago.setCodigoReferencia(referencia);
        }
        pago.setMotivoRechazo(motivoError);
        pago.setFechaCreacion(fecha);
        pago.setUltimaModificacion(fecha);
        boolean aprobado = EstadoPago.APROBADO.equals(estado.getNombre());
        if (aprobado) {
            pago.setFechaAprobacion(fecha);
        }
        pago = pagoRepository.save(pago);
        if (aprobado) {
            Usuario usuario = inscripcion.getAlumno().getUsuario();
            comprobanteRepository.save(new Comprobante(pago, Comprobante.generarNumero(pago.getIdPago(), fecha),
                    fecha, usuario.getNombreCompleto(), usuario.getDni()));
        }
        return pago;
    }

    private Inscripcion crearInscripcion(Cohorte cohorte, Alumno alumno, LocalDateTime ahora, Tema temaCompletada) {
        Inscripcion inscripcion = new Inscripcion(cohorte, alumno, ahora.plusDays(cohorte.getAccesoDias()));
        inscripcion.setEstadoInscripcion(estadoInscripcion(com.app.idoneos.model.EstadoInscripcion.HABILITADA));
        inscripcion.setEstadoAvance(estadoAvance(com.app.idoneos.model.EstadoAvance.EN_PROGRESO));
        inscripcion = inscripcionRepository.save(inscripcion);
        if (temaCompletada != null) {
            Progreso progreso = new Progreso(temaCompletada, inscripcion);
            progreso.setCompletada(true);
            progreso.setFechaCompletada(ahora);
            progresoRepository.save(progreso);
        }
        return inscripcion;
    }

    /** Crea un recurso del docente con los datos propios de su tipo y lo incorpora al tema. */
    /** Incorpora un material al tema. Cada docente tiene pocos recursos (con sus propios archivos) y los temas los reutilizan. */
    private TemaRecurso agregarMaterial(Tema tema, DocenteCurso autor, TipoRecurso tipo, String titulo) {
        Docente docente = autor.getDocente();
        Recurso recurso = switch (tipo) {
            case BIBLIOGRAFIA -> recursoParaElTema(tema, docente, "bibliografia", () -> crearBibliografia(docente, titulo));
            case GRABACION -> recursoParaElTema(tema, docente, "grabacion", () -> crearGrabacion(docente, titulo));
            case ENLACE -> crearEnlace(docente, titulo);
            default -> recursoParaElTema(tema, docente, "material", () -> crearMaterialDeLectura(docente, titulo));
        };
        return recurso == null ? null : vincular(tema, recurso, autor);
    }

    /** Une un recurso a un tema, al final de sus materiales. */
    private TemaRecurso vincular(Tema tema, Recurso recurso, DocenteCurso autor) {
        int orden = tema.getTemasRecurso().size() + 1;
        TemaRecurso material = temaRecursoRepository.save(new TemaRecurso(tema, recurso, autor, orden));
        tema.getTemasRecurso().add(material);
        recurso.getTemasRecurso().add(material);
        return material;
    }

    private List<Recurso> recursosDe(Docente docente, String clase) {
        return recursosDeLosDocentes.computeIfAbsent(docente.getIdDocente() + ":" + clase, k -> new java.util.ArrayList<>());
    }

    /**
     * Elige el recurso de una clase para un tema: crea uno nuevo mientras el docente no complete su cupo y después reutiliza
     * uno que el tema todavía no tenga. Devuelve null si el tema ya los usa todos.
     */
    private Recurso recursoParaElTema(Tema tema, Docente docente, String clase, java.util.function.Supplier<Recurso> crear) {
        List<Recurso> existentes = recursosDe(docente, clase);
        if (existentes.size() < CUPO_DE_RECURSOS.get(clase)) {
            Recurso nuevo = crear.get();
            existentes.add(nuevo);
            return nuevo;
        }
        for (int i = 0; i < existentes.size(); i++) {
            Recurso candidato = existentes.get((tema.getIdTema() + i) % existentes.size());
            boolean yaEstaEnElTema = tema.getTemasRecurso().stream()
                    .anyMatch(m -> m.getRecurso().getIdRecurso() == candidato.getIdRecurso());
            if (!yaEstaEnElTema) {
                return candidato;
            }
        }
        return null;
    }

    /** El índice del archivo de ejemplo que le toca al próximo recurso del docente: cada docente usa archivos distintos. */
    private int indiceDeArchivo(Docente docente, String extension) {
        int orden = ordenDeLosDocentes.computeIfAbsent(docente.getIdDocente(), k -> ordenDeLosDocentes.size());
        if ("pdf".equals(extension)) {
            return orden * 3 + recursosDe(docente, "material").size() + recursosDe(docente, "bibliografia").size();
        }
        return orden * 2 + recursosDe(docente, "grabacion").size() + recursosDe(docente, "clon").size();
    }

    /** Una bibliografía: siempre pública. */
    private Recurso crearBibliografia(Docente docente, String titulo) {
        Recurso recurso = new Recurso(docente, titulo);
        recurso.setPrivado(false);
        recurso = recursoRepository.save(recurso);
        Bibliografia bibliografia = new Bibliografia(recurso, String.format("978-987-%05d-0", recurso.getIdRecurso()),
                docente.getUsuario().getNombreCompleto());
        Object[] pdf = subirArchivoDeEjemplo(recurso, "pdf", indiceDeArchivo(docente, "pdf"));
        bibliografia.setRutaArchivo((String) pdf[0]);
        bibliografia.setTamanioBytes((Long) pdf[1]);
        bibliografia.setExtension("pdf");
        recurso.setBibliografia(bibliografiaRepository.save(bibliografia));
        return recurso;
    }

    /** Una grabación: privada, de uso del docente. */
    private Recurso crearGrabacion(Docente docente, String titulo) {
        Recurso recurso = recursoRepository.save(new Recurso(docente, titulo));
        Object[] video = subirArchivoDeEjemplo(recurso, "mp4", indiceDeArchivo(docente, "mp4"));
        Grabacion grabacion = new Grabacion(recurso, (String) video[0]);
        grabacion.setTamanioBytes((Long) video[1]);
        recurso.setGrabacion(grabacionRepository.save(grabacion));
        return recurso;
    }

    /** Un material de lectura (presentación en PDF): público en la biblioteca. */
    private Recurso crearMaterialDeLectura(Docente docente, String titulo) {
        Recurso recurso = new Recurso(docente, titulo);
        recurso.setPrivado(false);
        recurso = recursoRepository.save(recurso);
        Object[] pdf = subirArchivoDeEjemplo(recurso, "pdf", indiceDeArchivo(docente, "pdf"));
        MaterialLectura lectura = new MaterialLectura(recurso,
                categoriaMaterialLecturaRepository.findByNombre(CategoriaMaterialLectura.PRESENTACION).orElseThrow(),
                (String) pdf[0], docente.getUsuario().getNombreCompleto());
        lectura.setTamanioBytes((Long) pdf[1]);
        lectura.setExtension("pdf");
        recurso.setMaterialLectura(materialLecturaRepository.save(lectura));
        return recurso;
    }

    private Recurso crearEnlace(Docente docente, String titulo) {
        Recurso recurso = recursoRepository.save(new Recurso(docente, titulo));
        recurso.setEnlace(enlaceRepository.save(new Enlace(recurso, "https://www.argentina.gob.ar")));
        return recurso;
    }

    private void agregarGlosario(Tema tema, String termino, String definicion) {
        terminoGlosarioRepository.save(new TerminoGlosario(tema, termino, definicion));
    }

    /** Crea un pool vacío para un tema. */
    private Pool crearPool(Tema tema, String nombre) {
        return poolRepository.save(new Pool(tema, nombre));
    }

    /** Agrega una pregunta con sus opciones a un pool. */
    private Pregunta agregarPregunta(Pool pool, String texto, boolean opcionMultiple, String... opciones) {
        Pregunta pregunta = preguntaRepository.save(new Pregunta(pool, texto, opcionMultiple));
        pool.getPreguntas().add(pregunta);
        for (String opcion : opciones) {
            boolean correcta = opcion.startsWith("*");
            pregunta.getOpcionesRespuesta().add(opcionRespuestaRepository
                    .save(new OpcionRespuesta(pregunta, correcta ? opcion.substring(1) : opcion, correcta)));
        }
        return pregunta;
    }

    /** Crea una autoevaluación de un tema asociado a un pool, con el tiempo límite en minutos. */
    private Autoevaluacion agregarAutoevaluacion(Tema tema, Pool pool, String nombre, int cantidadPreguntas,
            float notaMinima, Integer intentosPermitidos, Integer tiempoLimite, LocalDateTime apertura,
            boolean descuentoPorError, boolean navegacionLibre, boolean permiteSalir) {
        Autoevaluacion autoevaluacion = new Autoevaluacion(tema, nombre, cantidadPreguntas, notaMinima, apertura);
        autoevaluacion.setIntentosPermitidos(intentosPermitidos);
        autoevaluacion.setTiempoLimite(tiempoLimite);
        autoevaluacion.setDescuentoPorError(descuentoPorError);
        autoevaluacion.setNavegacionLibre(navegacionLibre);
        autoevaluacion.setPermiteSalir(permiteSalir);
        autoevaluacion = autoevaluacionRepository.save(autoevaluacion);
        autoevaluacion.getAutoevaluacionPools()
                .add(autoevaluacionPoolRepository.save(new AutoevaluacionPool(pool, autoevaluacion)));
        tema.getAutoevaluaciones().add(autoevaluacion);
        return autoevaluacion;
    }

    /** Crea, para un tema, un pool de cuatro preguntas. */
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

    /** Registra un intento entregado: sortea las primeras preguntas de los pools de la autoevaluación. */
    private IntentoAutoevaluacion registrarIntento(Inscripcion inscripcion, Autoevaluacion autoevaluacion, int correctas,
            int incorrectas, LocalDateTime fechaEntrega) {
        IntentoAutoevaluacion intento = new IntentoAutoevaluacion(inscripcion, autoevaluacion);
        intento.setFechaCreacion(fechaEntrega.minusMinutes(20));
        intento = intentoAutoevaluacionRepository.save(intento);
        int orden = 1;
        for (Pregunta pregunta : autoevaluacion.getPreguntasDisponibles().stream()
                .limit(autoevaluacion.getCantidadPreguntas()).toList()) {
            PreguntaIntento preguntaIntento = preguntaIntentoRepository.save(new PreguntaIntento(intento, pregunta, orden));
            intento.getPreguntasIntento().add(preguntaIntento);
            boolean acierta = orden <= correctas;
            if (acierta || orden <= correctas + incorrectas) {
                for (OpcionRespuesta opcion : pregunta.getOpcionesActivas()) {
                    if (opcion.getEsCorrecta() == acierta) {
                        preguntaIntento.getRespuestasIntento()
                                .add(respuestaIntentoRepository.save(new RespuestaIntento(preguntaIntento, opcion)));
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
        return intentoAutoevaluacionRepository.save(intento);
    }
}
