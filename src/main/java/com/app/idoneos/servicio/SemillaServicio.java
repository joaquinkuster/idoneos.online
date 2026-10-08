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
import com.app.idoneos.modelo.Autoevaluacion;
import com.app.idoneos.modelo.Cohorte;
import com.app.idoneos.modelo.Curso;
import com.app.idoneos.modelo.CursoModalidad;
import com.app.idoneos.modelo.Docente;
import com.app.idoneos.modelo.EstadoClaseEnVivo;
import com.app.idoneos.modelo.Inscripcion;
import com.app.idoneos.modelo.IntentoAutoevaluacion;
import com.app.idoneos.modelo.Material;
import com.app.idoneos.modelo.Modalidad;
import com.app.idoneos.modelo.Nivel;
import com.app.idoneos.modelo.Parametro;
import com.app.idoneos.modelo.ParticipacionDocente;
import com.app.idoneos.modelo.Programa;
import com.app.idoneos.modelo.Progreso;
import com.app.idoneos.modelo.Rol;
import com.app.idoneos.modelo.RolUsuario;
import com.app.idoneos.modelo.TipoMaterial;
import com.app.idoneos.modelo.TerminoGlosario;
import com.app.idoneos.modelo.Unidad;
import com.app.idoneos.modelo.UnidadCronograma;
import com.app.idoneos.modelo.Usuario;
import com.app.idoneos.repositorio.AdministradorRepositorio;
import com.app.idoneos.repositorio.AlumnoRepositorio;
import com.app.idoneos.repositorio.CategoriaRepositorio;
import com.app.idoneos.repositorio.ClaseEnVivoRepositorio;
import com.app.idoneos.repositorio.ConsultaForoRepositorio;
import com.app.idoneos.repositorio.AutoevaluacionRepositorio;
import com.app.idoneos.repositorio.CohorteRepositorio;
import com.app.idoneos.repositorio.CursoModalidadRepositorio;
import com.app.idoneos.repositorio.CursoRepositorio;
import com.app.idoneos.repositorio.DocenteRepositorio;
import com.app.idoneos.repositorio.EstadoClaseEnVivoRepositorio;
import com.app.idoneos.repositorio.InscripcionRepositorio;
import com.app.idoneos.repositorio.IntentoAutoevaluacionRepositorio;
import com.app.idoneos.repositorio.MaterialRepositorio;
import com.app.idoneos.repositorio.ModalidadRepositorio;
import com.app.idoneos.repositorio.NivelRepositorio;
import com.app.idoneos.repositorio.ParametroRepositorio;
import com.app.idoneos.repositorio.ParticipacionDocenteRepositorio;
import com.app.idoneos.repositorio.ProgramaRepositorio;
import com.app.idoneos.repositorio.ProgresoRepositorio;
import com.app.idoneos.repositorio.RolRepositorio;
import com.app.idoneos.repositorio.RolUsuarioRepositorio;
import com.app.idoneos.repositorio.TipoMaterialRepositorio;
import com.app.idoneos.repositorio.TerminoGlosarioRepositorio;
import com.app.idoneos.repositorio.UnidadCronogramaRepositorio;
import com.app.idoneos.repositorio.UnidadRepositorio;
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
    private ParticipacionDocenteRepositorio participacionDocenteRepositorio;

    @Autowired
    private ProgramaRepositorio programaRepositorio;

    @Autowired
    private UnidadRepositorio unidadRepositorio;

    @Autowired
    private UnidadCronogramaRepositorio unidadCronogramaRepositorio;

    @Autowired
    private MaterialRepositorio materialRepositorio;

    @Autowired
    private CohorteRepositorio cohorteRepositorio;

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
    private ConsultaForoRepositorio consultaForoRepositorio;

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
        estadoClaseEnVivoRepositorio.save(new EstadoClaseEnVivo("Cancelada"));

        // Administrador y parámetros
        Usuario usuarioAdmin = crearUsuario("Admin", "Idóneos", "20111222", correoAdmin, "+54 376 4000000", rolAdministrador);
        Administrador admin = administradorRepositorio.save(new Administrador(usuarioAdmin));
        parametroRepositorio.save(new Parametro(admin, Parametro.MINIMO_UNIDADES_CON_MATERIAL, "2"));

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

        LocalDateTime ahora = LocalDateTime.now();

        // Curso 1: Mercado de Capitales Argentino (en vivo + grabada), con cohortes abierta, en dictado y finalizada
        ParticipacionDocente titular1 = crearCurso("Mercado de Capitales Argentino",
                "Aprendé a operar acciones, bonos, ONs y opciones en BYMA con docentes de primer nivel.", 150000f,
                "mercado-capitales-argentino.jpg", mercadoCapitales, intermedio, true,
                List.of(enVivo, grabada), fausto, List.of(sebastian, mariano));
        Curso curso1 = titular1.getCurso();
        Programa programa1 = crearPrograma(curso1, "Programa 2026",
                "Dominar la operatoria bursátil y el análisis de instrumentos del mercado argentino.",
                "Manual del mercado de capitales (CNV) y apuntes de cátedra.");
        Unidad unidad1 = agregarUnidad(programa1, curso1, 1, 2, "Introducción al mercado de capitales",
                "Actores, instrumentos y funcionamiento del mercado.", titular1, presentacion);
        Unidad unidad2 = agregarUnidad(programa1, curso1, 2, 3, "Renta fija y bonos",
                "Valuación y operatoria de bonos soberanos.", titular1, presentacion);
        agregarUnidad(programa1, curso1, 3, 4, "Renta variable y opciones", "Acciones, CEDEARs y estrategias con opciones.",
                null, null);
        Cohorte abierta1 = crearCohorte(programa1, ahora.minusDays(10), ahora.plusDays(20), ahora.plusDays(25),
                ahora.plusDays(90), 12, 30);
        Cohorte enDictado1 = crearCohorte(programa1, ahora.minusDays(60), ahora.minusDays(30), ahora.minusDays(20),
                ahora.plusDays(40), 12, 25);
        crearCohorte(programa1, ahora.minusDays(200), ahora.minusDays(170), ahora.minusDays(160), ahora.minusDays(80), 12, 25);
        crearInscripcion(abierta1, lucia, ahora, unidad1);
        crearInscripcion(abierta1, martin, ahora, null);
        Inscripcion inscripcionValentina = crearInscripcion(enDictado1, valentina, ahora, unidad1);
        claseEnVivoRepositorio.save(new ClaseEnVivo(titular1, claseProgramada, enDictado1,
                "Clase 1: Panorama del mercado de capitales", ahora.plusDays(3), 90));
        claseEnVivoRepositorio.save(new ClaseEnVivo(titular1, claseFinalizada, enDictado1,
                "Clase 0: Presentación del curso", ahora.minusDays(15), 60));
        claseEnVivoRepositorio.save(new ClaseEnVivo(titular1, claseEnVivoAhora, enDictado1,
                "Clase magistral: Operatoria de renta fija en BYMA", ahora.minusMinutes(20), 90));

        // Contenido de las unidades 1 y 2 (acceso al curso del alumno en dictado, CU-27)
        agregarMaterial(unidad1, titular1, grabacion, "Grabación: Estructura del mercado argentino");
        agregarMaterial(unidad1, titular1, bibliografia, "Ley 26.831 y modificatorias CNV");
        agregarMaterial(unidad2, titular1, bibliografia, "Guía teórica de renta fija");
        agregarGlosario(unidad1, "BYMA", "Bolsas y Mercados Argentinos: el mercado donde se negocian los valores.");
        agregarGlosario(unidad1, "CNV", "Comisión Nacional de Valores: organismo que regula y controla el mercado de capitales.");
        agregarGlosario(unidad1, "ON", "Obligación negociable: título de deuda que emiten las empresas para financiarse.");
        agregarGlosario(unidad2, "TIR", "Tasa interna de retorno: rendimiento anual de un bono si se lo mantiene hasta el vencimiento.");
        agregarGlosario(unidad2, "Duration", "Plazo promedio ponderado en que se recibe el dinero de un bono.");
        Autoevaluacion autoevaluacion1 = new Autoevaluacion(unidad1, "Autoevaluación Unidad 1: Marco regulatorio", 10, 6f,
                ahora.minusDays(30));
        autoevaluacion1.setIntentosPermitidos(3);
        autoevaluacion1.setTiempoLimite(30);
        autoevaluacion1 = autoevaluacionRepositorio.save(autoevaluacion1);
        Autoevaluacion autoevaluacion2 = new Autoevaluacion(unidad2, "Autoevaluación Unidad 2: Ejercicios de rendimiento",
                10, 7f, ahora.minusDays(5));
        autoevaluacion2.setIntentosPermitidos(3);
        autoevaluacionRepositorio.save(autoevaluacion2);
        registrarIntento(inscripcionValentina, autoevaluacion1, 4.5f, false, ahora.minusDays(12));
        registrarIntento(inscripcionValentina, autoevaluacion1, 8f, true, ahora.minusDays(10));
        consultaForoRepositorio.save(new ConsultaForo(unidad1, inscripcionValentina,
                "¿La CNV exige matrícula para operar con obligaciones negociables?"));

        // Curso 2: Macroeconomía de Coyuntura (grabada), con cohorte abierta sin inscriptos
        ParticipacionDocente titular2 = crearCurso("Macroeconomía de Coyuntura",
                "Tipo de cambio, inflación y tasas: las variables que mueven la economía argentina.", 0f,
                "macroeconomia-coyuntura.jpg", macroeconomia, basico, true, List.of(grabada), fausto, List.of());
        Curso curso2 = titular2.getCurso();
        Programa programa2 = crearPrograma(curso2, "Programa 2026",
                "Interpretar la coyuntura macroeconómica y su impacto en las decisiones financieras.",
                "Informes del BCRA e INDEC.");
        agregarUnidad(programa2, curso2, 1, 2, "Inflación y precios", "Medición y causas de la inflación.", titular2, presentacion);
        agregarUnidad(programa2, curso2, 2, 2, "Tipo de cambio", "Regímenes cambiarios y brecha.", titular2, presentacion);
        agregarUnidad(programa2, curso2, 3, 2, "Tasas y política monetaria", "Instrumentos del BCRA.", titular2, presentacion);
        crearCohorte(programa2, ahora.minusDays(5), ahora.plusDays(30), null, null, 8, null);

        // Curso 3: Planificación Fiscal Corporativa (no alcanza el mínimo de unidades con material)
        ParticipacionDocente titular3 = crearCurso("Planificación Fiscal Corporativa",
                "Estrategias de planificación tributaria para empresas: IVA, ganancias y regímenes especiales.", 120000f,
                "planificacion-fiscal-corporativa.jpg", impuestos, avanzado, true, List.of(grabada), sebastian, List.of());
        Curso curso3 = titular3.getCurso();
        Programa programa3 = crearPrograma(curso3, "Programa 2026",
                "Diseñar estructuras fiscales eficientes dentro del marco legal.", "Ley de Impuesto a las Ganancias.");
        agregarUnidad(programa3, curso3, 1, 3, "Impuesto a las ganancias", "Determinación y planificación.", titular3, presentacion);
        agregarUnidad(programa3, curso3, 2, 3, "IVA y regímenes especiales", "Débito, crédito y regímenes.", null, null);

        // Curso 4: Finanzas Personales e Inversión (grabada + clon de IA), con cohorte abierta e inscripción
        ParticipacionDocente titular4 = crearCurso("Finanzas Personales e Inversión",
                "Organizá tus finanzas, aprendé a ahorrar e invertir con criterio y planificá tu patrimonio.", 80000f,
                "finanzas-personales-inversion.jpg", finanzasPersonales, basico, true,
                List.of(grabada, clonIa), mariano, List.of());
        Curso curso4 = titular4.getCurso();
        Programa programa4 = crearPrograma(curso4, "Programa 2026",
                "Construir un plan financiero personal sólido.", "Guía de educación financiera (CNV).");
        Unidad unidad4 = agregarUnidad(programa4, curso4, 1, 2, "Presupuesto y ahorro", "Ordenar ingresos y gastos.",
                titular4, presentacion);
        agregarUnidad(programa4, curso4, 2, 3, "Instrumentos de inversión", "Plazos fijos, bonos y fondos.", titular4, presentacion);
        agregarUnidad(programa4, curso4, 3, 2, "Planificación patrimonial", "Objetivos y horizonte de inversión.",
                titular4, presentacion);
        Cohorte abierta4 = crearCohorte(programa4, ahora.minusDays(2), ahora.plusDays(40), null, null, 10, 20);
        crearInscripcion(abierta4, martin, ahora, unidad4);

        // Curso 5: Análisis Técnico Bursátil (en vivo + grabada), con cohorte próxima
        ParticipacionDocente titular5 = crearCurso("Análisis Técnico Bursátil",
                "Gráficos, indicadores y patrones para tomar decisiones de compra y venta.", 95000f,
                "analisis-tecnico-bursatil.jpg", cuantitativas, intermedio, true,
                List.of(enVivo, grabada), mariano, List.of(fausto));
        Curso curso5 = titular5.getCurso();
        Programa programa5 = crearPrograma(curso5, "Programa 2026",
                "Aplicar herramientas de análisis técnico a distintos activos.", "Technical Analysis of the Financial Markets.");
        agregarUnidad(programa5, curso5, 1, 3, "Fundamentos del análisis técnico", "Tendencias, soportes y resistencias.",
                titular5, presentacion);
        agregarUnidad(programa5, curso5, 2, 3, "Indicadores y osciladores", "Medias móviles, RSI y MACD.", titular5, presentacion);
        crearCohorte(programa5, ahora.plusDays(15), ahora.plusDays(45), ahora.plusDays(50), ahora.plusDays(100), 10, null);

        // Curso 6: Introducción a las Finanzas (sin programas ni unidades: puede darse de baja)
        crearCurso("Introducción a las Finanzas", "Conceptos básicos para empezar a entender el mundo financiero.", 40000f,
                "introduccion-finanzas.jpg", mercadoCapitales, basico, false, List.of(grabada), sebastian, List.of());

        // Curso 7: curso dado de baja (para ver cómo se listan los registros dados de baja)
        ParticipacionDocente titular7 = crearCurso("Economía Argentina 2024",
                "Edición histórica del curso de economía argentina. Ya no se dicta.", 60000f,
                "economia-argentina-2024.jpg", macroeconomia, intermedio, false, List.of(grabada), fausto, List.of());
        Curso curso7 = titular7.getCurso();
        curso7.marcarInactivo();
        cursoRepositorio.save(curso7);
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
    private ParticipacionDocente crearCurso(String nombre, String descripcion, float precio, String imagen,
            Categoria categoria, Nivel nivel, boolean emiteCertificado, List<Modalidad> modalidades,
            Docente titular, List<Docente> ayudantes) {
        Curso curso = new Curso(nivel, categoria, nombre, precio);
        curso.setDescripcion(descripcion);
        curso.setImagen(imagen);
        curso.setEmiteCertificado(emiteCertificado);
        curso = cursoRepositorio.save(curso);
        for (Modalidad modalidad : modalidades) {
            cursoModalidadRepositorio.save(new CursoModalidad(curso, modalidad));
        }
        for (Docente ayudante : ayudantes) {
            participacionDocenteRepositorio.save(new ParticipacionDocente(curso, ayudante, false));
        }
        return participacionDocenteRepositorio.save(new ParticipacionDocente(curso, titular, true));
    }

    private Programa crearPrograma(Curso curso, String nombre, String objetivos, String bibliografia) {
        Programa programa = new Programa(curso, nombre, objetivos, bibliografia);
        programa.setDescripcion("Plan de formación del curso " + curso.getNombre() + ".");
        return programaRepositorio.save(programa);
    }

    /**
     * Agrega una unidad al curso y a su cronograma. Si se indica una participación docente, carga
     * además un material publicado asociado a la unidad.
     */
    private Unidad agregarUnidad(Programa programa, Curso curso, int numeroOrden, int semanas, String titulo,
            String descripcion, ParticipacionDocente autorMaterial, TipoMaterial tipoMaterial) {
        Unidad unidad = new Unidad(curso, titulo, "Contenido de la unidad: " + titulo + ". " + descripcion);
        unidad.setDescripcion(descripcion);
        unidad = unidadRepositorio.save(unidad);
        unidadCronogramaRepositorio.save(new UnidadCronograma(programa, unidad, numeroOrden, semanas));
        if (autorMaterial != null) {
            Material material = new Material(autorMaterial, tipoMaterial, unidad, "Material: " + titulo);
            material.setContenido("Material de estudio de la unidad " + titulo + ".");
            materialRepositorio.save(material);
        }
        return unidad;
    }

    private Cohorte crearCohorte(Programa programa, LocalDateTime inicioInscripcion, LocalDateTime finInscripcion,
            LocalDateTime inicioDictado, LocalDateTime finDictado, int semanasAcceso, Integer cupoMaximo) {
        Cohorte cohorte = new Cohorte(programa, inicioInscripcion, finInscripcion, semanasAcceso);
        cohorte.setFechaInicioDictado(inicioDictado);
        cohorte.setFechaFinDictado(finDictado);
        cohorte.setCupoMaximo(cupoMaximo);
        return cohorteRepositorio.save(cohorte);
    }

    /**
     * Inscribe a un alumno en una cohorte y, si se indica una unidad, la registra como completada.
     */
    private Inscripcion crearInscripcion(Cohorte cohorte, Alumno alumno, LocalDateTime ahora, Unidad unidadCompletada) {
        Inscripcion inscripcion = new Inscripcion(cohorte, alumno, ahora.plusWeeks(cohorte.getSemanasAcceso()));
        inscripcion.setHabilitado(true);
        inscripcion = inscripcionRepositorio.save(inscripcion);
        if (unidadCompletada != null) {
            Progreso progreso = new Progreso(unidadCompletada, inscripcion);
            progreso.setCompletada(true);
            progreso.setFechaCompletada(ahora);
            progresoRepositorio.save(progreso);
        }
        return inscripcion;
    }

    private void agregarMaterial(Unidad unidad, ParticipacionDocente autor, TipoMaterial tipo, String titulo) {
        Material material = new Material(autor, tipo, unidad, titulo);
        material.setAutor(autor.getDocente().getUsuario().getNombreCompleto());
        materialRepositorio.save(material);
    }

    private void agregarGlosario(Unidad unidad, String termino, String definicion) {
        terminoGlosarioRepositorio.save(new TerminoGlosario(unidad, termino, definicion));
    }

    private void registrarIntento(Inscripcion inscripcion, Autoevaluacion autoevaluacion, float nota, boolean aprobado,
            LocalDateTime fechaEntrega) {
        IntentoAutoevaluacion intento = new IntentoAutoevaluacion(inscripcion, autoevaluacion);
        intento.setNota(nota);
        intento.setAprobado(aprobado);
        intento.setFechaEntrega(fechaEntrega);
        intentoAutoevaluacionRepositorio.save(intento);
    }
}
