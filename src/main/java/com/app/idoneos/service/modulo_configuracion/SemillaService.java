package com.app.idoneos.service.modulo_configuracion;

import com.app.idoneos.model.*;
import com.app.idoneos.repository.modulo_auditoria.*;
import com.app.idoneos.repository.modulo_clases_vivo.*;
import com.app.idoneos.repository.modulo_configuracion.*;
import com.app.idoneos.repository.modulo_cursos.*;
import com.app.idoneos.repository.modulo_evaluaciones.*;
import com.app.idoneos.repository.modulo_gestion_academica.*;
import com.app.idoneos.repository.modulo_ia.*;
import com.app.idoneos.repository.modulo_inscripciones.*;
import com.app.idoneos.repository.modulo_reportes.*;
import com.app.idoneos.repository.modulo_usuarios.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio Maestro para el semillado de datos iniciales completos del sistema.
 * Puebla catálogos, usuarios por rol (Admin, Docentes de Élite, Alumnos),
 * cursos, programas, unidades, cronogramas, materiales, glosarios, foros,
 * pools de preguntas, opciones, autoevaluaciones, clases en vivo, clases clon IA,
 * inscripciones, pagos, progresos, descuentos, reportes y registros de auditoría.
 */
@Service
public class SemillaService {

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private RolRepository rolRepository;
    @Autowired private CategoriaRepository categoriaRepository;
    @Autowired private NivelRepository nivelRepository;
    @Autowired private CursoRepository cursoRepository;
    @Autowired private UnidadRepository unidadRepository;
    @Autowired private MaterialRepository materialRepository;
    @Autowired private ProgramaRepository programaRepository;
    @Autowired private CohorteRepository cohorteRepository;
    @Autowired private CronogramaRepository cronogramaRepository;

    @Autowired private TipoMaterialRepository tipoMaterialRepository;
    @Autowired private ModalidadRepository modalidadRepository;
    @Autowired private EstadoClaseEnVivoRepository estadoClaseEnVivoRepository;
    @Autowired private EstadoClaseClonIARepository estadoClaseClonIARepository;
    @Autowired private EstadoPagoRepository estadoPagoRepository;
    @Autowired private MetodoPagoRepository metodoPagoRepository;
    @Autowired private TipoAccionAuditoriaRepository tipoAccionAuditoriaRepository;
    @Autowired private TipoReporteRepository tipoReporteRepository;
    @Autowired private AdministradorRepository administradorRepository;
    @Autowired private ConfiguracionRepository configuracionRepository;
    @Autowired private DocenteRepository docenteRepository;
    @Autowired private AlumnoRepository alumnoRepository;
    @Autowired private InscripcionRepository inscripcionRepository;
    @Autowired private PagoRepository pagoRepository;
    @Autowired private DescuentoRepository descuentoRepository;
    @Autowired private ReporteRepository reporteRepository;
    @Autowired private TerminoGlosarioRepository terminoGlosarioRepository;
    @Autowired private ConsultaForoRepository consultaForoRepository;
    @Autowired private RespuestaForoRepository respuestaForoRepository;
    @Autowired private PoolRepository poolRepository;
    @Autowired private PreguntaRepository preguntaRepository;
    @Autowired private OpcionRespuestaRepository opcionRespuestaRepository;
    @Autowired private AutoevaluacionRepository autoevaluacionRepository;
    @Autowired private PoolAutoevaluacionRepository poolAutoevaluacionRepository;
    @Autowired private IntentoAutoevaluacionRepository intentoAutoevaluacionRepository;
    @Autowired private ProgresoRepository progresoRepository;
    @Autowired private ClaseEnVivoRepository claseEnVivoRepository;
    @Autowired private ClaseClonIARepository claseClonIARepository;
    @Autowired private AuditoriaRepository auditoriaRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Transactional
    public void insertarSemilla() {
        String adminEmail = "admin@idoneos.online";
        if (usuarioRepository.findByCorreoAndBajaFalse(adminEmail).isPresent() && inscripcionRepository.count() > 10) {
            System.out.println("✅ Idóneos Online: Base de datos ya poblada con semilla maestra.");
            return;
        }

        // ===============================================================
        // 1. ROLES DEL SISTEMA
        // ===============================================================
        Rol rolAdmin = rolRepository.findByNombre("Administrador")
                .orElseGet(() -> rolRepository.save(new Rol("Administrador")));
        Rol rolDocente = rolRepository.findByNombre("Docente")
                .orElseGet(() -> rolRepository.save(new Rol("Docente")));
        Rol rolAlumno = rolRepository.findByNombre("Alumno")
                .orElseGet(() -> rolRepository.save(new Rol("Alumno")));

        // ===============================================================
        // 2. CATÁLOGOS BASE
        // ===============================================================
        TipoMaterial tmGrabacion = tipoMaterialRepository.save(new TipoMaterial("Grabación"));
        TipoMaterial tmBibliografia = tipoMaterialRepository.save(new TipoMaterial("Bibliografía"));
        TipoMaterial tmPresentacion = tipoMaterialRepository.save(new TipoMaterial("Presentación"));
        TipoMaterial tmResumen = tipoMaterialRepository.save(new TipoMaterial("Resumen"));

        modalidadRepository.save(new Modalidad("En vivo"));
        modalidadRepository.save(new Modalidad("Grabada"));
        modalidadRepository.save(new Modalidad("Clon IA"));

        EstadoClaseEnVivo estProg = estadoClaseEnVivoRepository.save(new EstadoClaseEnVivo("Programada"));
        EstadoClaseEnVivo estEnVivo = estadoClaseEnVivoRepository.save(new EstadoClaseEnVivo("En vivo"));
        EstadoClaseEnVivo estFin = estadoClaseEnVivoRepository.save(new EstadoClaseEnVivo("Finalizada"));

        EstadoClaseClonIA estClonPend = estadoClaseClonIARepository.save(new EstadoClaseClonIA("Pendiente"));
        EstadoClaseClonIA estClonGen = estadoClaseClonIARepository.save(new EstadoClaseClonIA("Generada"));
        EstadoClaseClonIA estClonErr = estadoClaseClonIARepository.save(new EstadoClaseClonIA("Error"));

        EstadoPago estPagoPend = estadoPagoRepository.save(new EstadoPago("Pendiente"));
        EstadoPago estPagoAcred = estadoPagoRepository.save(new EstadoPago("Acreditado"));
        EstadoPago estPagoRech = estadoPagoRepository.save(new EstadoPago("Rechazado"));

        MetodoPago metodoTarjeta = metodoPagoRepository.save(new MetodoPago("Tarjeta de crédito"));
        MetodoPago metodoDebito = metodoPagoRepository.save(new MetodoPago("Tarjeta de débito"));
        MetodoPago metodoModo = metodoPagoRepository.save(new MetodoPago("MODO / Billetera Virtual"));

        TipoAccionAuditoria accionCrear = tipoAccionAuditoriaRepository.save(new TipoAccionAuditoria("Crear"));
        TipoAccionAuditoria accionModif = tipoAccionAuditoriaRepository.save(new TipoAccionAuditoria("Modificar"));
        TipoAccionAuditoria accionElim = tipoAccionAuditoriaRepository.save(new TipoAccionAuditoria("Eliminar"));
        TipoAccionAuditoria accionCons = tipoAccionAuditoriaRepository.save(new TipoAccionAuditoria("Consultar"));

        TipoReporte trAlumnos = tipoReporteRepository.save(new TipoReporte("Alumnos inscriptos"));
        TipoReporte trIngresos = tipoReporteRepository.save(new TipoReporte("Ingresos"));

        Nivel nivelBasico = nivelRepository.findByNombre("Básico")
                .orElseGet(() -> nivelRepository.save(new Nivel("Básico")));
        Nivel nivelIntermedio = nivelRepository.findByNombre("Intermedio")
                .orElseGet(() -> nivelRepository.save(new Nivel("Intermedio")));
        Nivel nivelAvanzado = nivelRepository.findByNombre("Avanzado")
                .orElseGet(() -> nivelRepository.save(new Nivel("Avanzado")));

        // ===============================================================
        // 3. USUARIOS (ADMINISTRADORES, DOCENTES, ALUMNOS)
        // ===============================================================
        Usuario adminUsuario = usuarioRepository.findByCorreoAndBajaFalse(adminEmail).orElseGet(() -> {
            Usuario u = new Usuario("Admin", "Idóneos", adminEmail, passwordEncoder.encode("123456"), rolAdmin);
            u.setDni("20111222");
            u.setEmailValidado(true);
            Administrador adm = new Administrador(u);
            u.setAdministrador(adm);
            return usuarioRepository.save(u);
        });
        Administrador adminActual = adminUsuario.getAdministrador();

        // Parámetros operativos (CU-99)
        if (configuracionRepository.count() == 0 && adminActual != null) {
            configuracionRepository.save(new Configuracion("autoevaluacion.intentos_maximos", "3", adminActual));
            configuracionRepository.save(new Configuracion("autoevaluacion.tiempo_limite_minutos", "30", adminActual));
            configuracionRepository.save(new Configuracion("autoevaluacion.nota_aprobacion", "6.0", adminActual));
            configuracionRepository.save(new Configuracion("evaluacion.preguntas_por_intento", "10", adminActual));
            configuracionRepository.save(new Configuracion("evaluacion.proporcion_opcion_multiple", "70", adminActual));
            configuracionRepository.save(new Configuracion("evaluacion.proporcion_verdadero_falso", "30", adminActual));
            configuracionRepository.save(new Configuracion("grabaciones.plazo_disponibilidad_meses", "4", adminActual));
            configuracionRepository.save(new Configuracion("grabaciones.aviso_previo_dias", "7", adminActual));
            configuracionRepository.save(new Configuracion("sesiones.max_concurrentes", "2", adminActual));
            configuracionRepository.save(new Configuracion("foro.tiempo_limite_edicion_minutos", "30", adminActual));
            configuracionRepository.save(new Configuracion("ollama.model", "llama3.1", adminActual));
            configuracionRepository.save(new Configuracion("ollama.url", "http://localhost:11434/api/generate", adminActual));
            configuracionRepository.save(new Configuracion("heygen.api_key", "hg_mock_live_key_9921", adminActual));
            configuracionRepository.save(new Configuracion("mercadopago.access_token", "APP_USR-mock-token", adminActual));
        }

        // Docentes de élite
        Usuario usuFausto = usuarioRepository.findByCorreoAndBajaFalse("fausto.spotorno@idoneos.online").orElseGet(() -> {
            Usuario u = new Usuario("Fausto", "Spotorno", "fausto.spotorno@idoneos.online", passwordEncoder.encode("123456"), rolDocente);
            u.setDni("23456789");
            u.setEmailValidado(true);
            Docente d = new Docente(u, 20);
            d.setBiografia("Economista UCA, Director de Maestría en UADE y Socio de Orlando J. Ferreres & Asoc.");
            d.setMatriculaCnv("12845");
            d.setAvatarId("avatar_fausto_v2");
            d.setVoiceId("voice_spotorno_es_ar");
            d.setFechaConsentimientoClon(LocalDateTime.now());
            d.setHabilitado(true);
            u.setDocente(d);
            return usuarioRepository.save(u);
        });
        Docente docenteFausto = usuFausto.getDocente();

        Usuario usuSebas = usuarioRepository.findByCorreoAndBajaFalse("sebastian.bordato@idoneos.online").orElseGet(() -> {
            Usuario u = new Usuario("Sebastián", "Bordato", "sebastian.bordato@idoneos.online", passwordEncoder.encode("123456"), rolDocente);
            u.setDni("24567890");
            u.setEmailValidado(true);
            Docente d = new Docente(u, 15);
            d.setBiografia("Contador Público UBA. Experto en Planificación Fiscal Corporativa y Mercado de Capitales.");
            d.setMatriculaCnv("15932");
            d.setAvatarId("avatar_sebas_v1");
            d.setVoiceId("voice_bordato_es_ar");
            d.setFechaConsentimientoClon(LocalDateTime.now());
            d.setHabilitado(true);
            u.setDocente(d);
            return usuarioRepository.save(u);
        });
        Docente docenteSebas = usuSebas.getDocente();

        // Alumno Principal para pruebas
        Usuario usuAlumnoPrincipal = usuarioRepository.findByCorreoAndBajaFalse("alumno@correo.com").orElseGet(() -> {
            Usuario u = new Usuario("Juan", "Pérez", "alumno@correo.com", passwordEncoder.encode("123456"), rolAlumno);
            u.setDni("38123456");
            u.setEmailValidado(true);
            Alumno al = new Alumno(u);
            u.setAlumno(al);
            return usuarioRepository.save(u);
        });
        Alumno alumnoPrincipal = usuAlumnoPrincipal.getAlumno();

        // ===============================================================
        // 4. CATEGORÍAS TEMÁTICAS
        // ===============================================================
        Categoria catMercado = categoriaRepository.findAll().stream()
                .filter(c -> "Mercado de Capitales".equals(c.getNombre())).findFirst()
                .orElseGet(() -> categoriaRepository.save(new Categoria("Mercado de Capitales", "Cursos sobre bonos, acciones, CEDEARs, ONs y derivados financieros.")));
        Categoria catEconomia = categoriaRepository.findAll().stream()
                .filter(c -> "Economía".equals(c.getNombre())).findFirst()
                .orElseGet(() -> categoriaRepository.save(new Categoria("Economía", "Análisis macroeconómico, proyecciones de coyuntura e inflación argentina.")));
        Categoria catFinanzas = categoriaRepository.findAll().stream()
                .filter(c -> "Finanzas Corporativas".equals(c.getNombre())).findFirst()
                .orElseGet(() -> categoriaRepository.save(new Categoria("Finanzas Corporativas", "Valuación de empresas, flujo de caja y finanzas para directivos.")));
        Categoria catImpuestos = categoriaRepository.findAll().stream()
                .filter(c -> "Impuestos y Contabilidad".equals(c.getNombre())).findFirst()
                .orElseGet(() -> categoriaRepository.save(new Categoria("Impuestos y Contabilidad", "Legislación tributaria argentina y optimización de impuestos.")));

        // ===============================================================
        // 5. CURSOS, PROGRAMAS, COHORTES Y CONTENIDO
        // ===============================================================
        Curso curso1 = cursoRepository.findByNombre("Mercado de Capitales Argentino").orElseGet(() -> {
            Curso c = new Curso("Mercado de Capitales Argentino",
                    "Aprende a operar Acciones, Bonos Soberanos, Obligaciones Negociables y Opciones en BYMA.",
                    150000f, catMercado, nivelIntermedio, docenteFausto);
            c.setEmiteCertificado(true);
            return cursoRepository.save(c);
        });

        Programa prog1 = programaRepository.findByCurso(curso1).stream().findFirst().orElseGet(() ->
                programaRepository.save(new Programa("Programa 2026 - Edición Élite", "Plan de formación avanzada en mercado argentino",
                        "Dominar los instrumentos bursátiles y su marco normativo.",
                        "Ley 26.831; Reglamentación CNV; Material BYMA.", curso1)));

        Cohorte cohorte1 = cohorteRepository.findByPrograma(prog1).stream().findFirst().orElseGet(() ->
                cohorteRepository.save(new Cohorte(
                        LocalDateTime.now().minusMonths(1),
                        LocalDateTime.now().plusMonths(3),
                        30, prog1)));

        Curso curso2 = cursoRepository.findByNombre("Análisis Macroeconómico de Coyuntura").orElseGet(() -> {
            Curso c = new Curso("Análisis Macroeconómico de Coyuntura",
                    "Variables macroeconómicas fundamentales: tipo de cambio, inflación, tasas y reservas del BCRA.",
                    0f, catEconomia, nivelBasico, docenteFausto);
            c.setEmiteCertificado(false);
            return cursoRepository.save(c);
        });

        Programa prog2 = programaRepository.findByCurso(curso2).stream().findFirst().orElseGet(() ->
                programaRepository.save(new Programa("Programa 2026 - Macro", "Fundamentos económicos",
                        "Comprender el ciclo macroeconómico argentino.", "Informes OJF y BCRA.", curso2)));

        Cohorte cohorte2 = cohorteRepository.findByPrograma(prog2).stream().findFirst().orElseGet(() ->
                cohorteRepository.save(new Cohorte(
                        LocalDateTime.now().minusMonths(1),
                        LocalDateTime.now().plusMonths(2),
                        50, prog2)));

        Curso curso3 = cursoRepository.findByNombre("Planificación Fiscal para Empresas").orElseGet(() -> {
            Curso c = new Curso("Planificación Fiscal para Empresas",
                    "Estrategias tributarias legales para pymes, régimen ganancias e IVA.",
                    220000f, catImpuestos, nivelAvanzado, docenteSebas);
            c.setEmiteCertificado(true);
            return cursoRepository.save(c);
        });

        Programa prog3 = programaRepository.findByCurso(curso3).stream().findFirst().orElseGet(() ->
                programaRepository.save(new Programa("Programa Tributario 2026", "Plan fiscal avanzado",
                        "Optimizar la carga impositiva en empresas argentinas.", "Ley de Impuesto a las Ganancias; Código Fiscal.", curso3)));

        Cohorte cohorte3 = cohorteRepository.findByPrograma(prog3).stream().findFirst().orElseGet(() ->
                cohorteRepository.save(new Cohorte(
                        LocalDateTime.now().minusWeeks(2),
                        LocalDateTime.now().plusMonths(4),
                        25, prog3)));

        // Unidades del Curso 1
        Unidad u1 = unidadRepository.save(new Unidad("Introducción al Mercado Financiero y CNV",
                "Estructura del mercado, agentes ALyC, BYMA y regulaciones CNV.",
                "Contenido detallado de la Unidad 1 sobre mercado de capitales."));
        Unidad u2 = unidadRepository.save(new Unidad("Renta Fija: Bonos y Obligaciones Negociables",
                "TIR, Paridad, Duration y curvas de rendimientos soberanos y corporativos.",
                "Contenido detallado de la Unidad 2 sobre análisis de renta fija."));
        Unidad u3 = unidadRepository.save(new Unidad("Renta Variable, CEDEARs y Valuación",
                "Análisis fundamental de empresas, CEDEARs de Wall Street y ratios P/E.",
                "Contenido detallado de la Unidad 3 sobre acciones y derivados."));

        cronogramaRepository.save(new Cronograma(1, 2, prog1, u1));
        cronogramaRepository.save(new Cronograma(2, 3, prog1, u2));
        cronogramaRepository.save(new Cronograma(3, 3, prog1, u3));

        // Materiales de Unidad 1
        materialRepository.save(new Material("Clase Magistral 1: Estructura del Mercado", docenteFausto, tmGrabacion, u1));
        materialRepository.save(new Material("Guía Oficial CNV para Idóneos", docenteFausto, tmBibliografia, u1));
        materialRepository.save(new Material("Presentación Diapositivas - Módulo 1", docenteFausto, tmPresentacion, u1));
        materialRepository.save(new Material("Resumen Ejecutivo de la Unidad", docenteFausto, tmResumen, u1));

        // Términos de Glosario (CU-31 a CU-34)
        terminoGlosarioRepository.save(new TerminoGlosario("ALyC", "Agente de Liquidación y Compensación regulado por la CNV.", u1));
        terminoGlosarioRepository.save(new TerminoGlosario("TIR", "Tasa Interna de Retorno de un bono o instrumento de renta fija.", u2));
        terminoGlosarioRepository.save(new TerminoGlosario("CEDEAR", "Certificado de Depósito Argentino representativo de acciones extranjeras.", u3));

        // Consultas y Foros (CU-35 a CU-42)
        ConsultaForo cForo = new ConsultaForo("¿Cómo calcular la paridad cambiaria al comprar CEDEARs?", alumnoPrincipal, u1);
        consultaForoRepository.save(cForo);
        RespuestaForo rForo = new RespuestaForo("Para calcular el tipo de cambio implícito (CCL), dividís el precio del CEDEAR en pesos por su ratio de conversión multiplicado por el precio del activo subyacente en dólares en Wall Street.", cForo, docenteFausto);
        respuestaForoRepository.save(rForo);

        // ===============================================================
        // 6. BANCOS DE PREGUNTAS (POOLS) Y AUTOEVALUACIONES (CU-53 a CU-64)
        // ===============================================================
        Pool pool1 = poolRepository.save(new Pool("Banco de Preguntas - Mercado y CNV", u1));

        Pregunta p1 = preguntaRepository.save(new Pregunta("¿Qué organismo regula y fiscaliza la oferta pública de valores en Argentina?", true, pool1));
        opcionRespuestaRepository.save(new OpcionRespuesta("Comisión Nacional de Valores (CNV)", true, p1));
        opcionRespuestaRepository.save(new OpcionRespuesta("Banco Central de la República Argentina (BCRA)", false, p1));
        opcionRespuestaRepository.save(new OpcionRespuesta("Unidad de Información Financiera (UIF)", false, p1));

        Pregunta p2 = preguntaRepository.save(new Pregunta("Un CEDEAR permite invertir en acciones internacionales desde el mercado local en pesos.", false, pool1));
        opcionRespuestaRepository.save(new OpcionRespuesta("Verdadero", true, p2));
        opcionRespuestaRepository.save(new OpcionRespuesta("Falso", false, p2));

        Autoevaluacion auto1 = new Autoevaluacion("Autoevaluación Unidad 1 - Fundamentos", 20, LocalDateTime.now().minusDays(10), u1);
        auto1.setIntentosPermitidos(3);
        autoevaluacionRepository.save(auto1);
        poolAutoevaluacionRepository.save(new PoolAutoevaluacion(pool1, auto1));

        // ===============================================================
        // 7. CLASES EN VIVO Y CLASES CON CLON IA (CU-65 a CU-80)
        // ===============================================================
        ClaseEnVivo claseVivo = new ClaseEnVivo("Taller en Vivo: Análisis de Tasas y Bonos", LocalDateTime.now().plusDays(3), 90, "rtmp://stream.idoneos.online/live", "live_fausto_k8172", docenteFausto, estProg, cohorte1);
        claseEnVivoRepository.save(claseVivo);

        ClaseClonIA claseClon = new ClaseClonIA("Estrategia de Liquidez con FaustIA", "Bienvenidos a la clase sobre optimización de caja y fondos comunes...", docenteFausto, estClonGen);
        claseClonIARepository.save(claseClon);

        // ===============================================================
        // 8. DESCUENTOS, INSCRIPCIONES, PAGOS Y PROGRESO (CU-43 a CU-52)
        // ===============================================================
        Descuento descLanzamiento = new Descuento();
        descLanzamiento.setNombre("Lanzamiento 2026");
        descLanzamiento.setPorcentaje(15.0f);
        descLanzamiento.setCursosRequeridos(0);
        descLanzamiento.setVigenciaDesde(LocalDateTime.now().minusMonths(2));
        descLanzamiento.setVigenciaHasta(LocalDateTime.now().plusMonths(6));
        descLanzamiento.setCantidadLimite(100);
        descLanzamiento.setCantidadUsada(12);
        descLanzamiento.setFechaCreacion(LocalDateTime.now().minusMonths(2));
        descuentoRepository.save(descLanzamiento);

        Descuento descComunidad = new Descuento();
        descComunidad.setNombre("Comunidad Idóneos");
        descComunidad.setPorcentaje(20.0f);
        descComunidad.setCursosRequeridos(1);
        descComunidad.setVigenciaDesde(LocalDateTime.now().minusMonths(1));
        descComunidad.setVigenciaHasta(LocalDateTime.now().plusMonths(6));
        descComunidad.setCantidadLimite(50);
        descComunidad.setCantidadUsada(8);
        descComunidad.setFechaCreacion(LocalDateTime.now().minusMonths(1));
        descuentoRepository.save(descComunidad);

        // Inscripción y pago del Alumno Principal
        Inscripcion inscPrincipal = new Inscripcion(cohorte1, alumnoPrincipal);
        inscPrincipal.setFecha(LocalDateTime.now().minusDays(15));
        inscPrincipal.setFechaVencimientoAcceso(LocalDateTime.now().plusMonths(6));
        inscripcionRepository.save(inscPrincipal);

        Pago pagoPrincipal = new Pago(127500f, inscPrincipal, estPagoAcred);
        pagoPrincipal.setFecha(LocalDateTime.now().minusDays(15));
        pagoPrincipal.setMetodoPago(metodoModo);
        pagoPrincipal.setDescuento(descLanzamiento);
        pagoPrincipal.setNombrePagador("Juan Pérez");
        pagoPrincipal.setDniPagador("38123456");
        pagoPrincipal.setDetalleEstado("accredited");
        pagoPrincipal.setFechaAprobacion(LocalDateTime.now().minusDays(15));
        pagoPrincipal.setNumeroComprobante("COMP-2026-0001");
        pagoPrincipal.setFechaEmisionComprobante(LocalDateTime.now().minusDays(15));
        pagoPrincipal.setComprobanteEnviado(true);
        pagoRepository.save(pagoPrincipal);

        // Progreso e Intentos para el Alumno Principal
        Progreso progU1 = new Progreso(inscPrincipal, u1, true);
        progU1.setFechaCompletada(LocalDateTime.now().minusDays(10));
        progresoRepository.save(progU1);

        Progreso progU2 = new Progreso(inscPrincipal, u2, false);
        progresoRepository.save(progU2);

        IntentoAutoevaluacion intento1 = new IntentoAutoevaluacion(inscPrincipal, auto1);
        intento1.setFechaEntrega(LocalDateTime.now().minusDays(10));
        intento1.setNota(9.5f);
        intentoAutoevaluacionRepository.save(intento1);

        // Población masiva de Alumnos e Historial para métricas y reportes
        String[][] alumnosDatos = {
                {"Carlos",    "Gómez",     "carlos.gomez@test.com",       "28111222"},
                {"María",     "López",     "maria.lopez@test.com",        "30222333"},
                {"Lucía",     "Fernández", "lucia.fernandez@test.com",    "32333444"},
                {"Martín",    "Rodríguez", "martin.rodriguez@test.com",   "34444555"},
                {"Sofía",     "Martínez",  "sofia.martinez@test.com",     "36555666"},
                {"Esteban",   "Paz",       "esteban.paz@test.com",        "29666777"},
                {"Camila",    "Díaz",      "camila.diaz@test.com",        "31777888"},
                {"Federico",  "Álvarez",   "federico.alvarez@test.com",   "33888999"},
                {"Valeria",   "Ríos",      "valeria.rios@test.com",       "35999000"},
                {"Gonzalo",   "Benítez",   "gonzalo.benitez@test.com",    "37000111"}
        };

        Cohorte[] cohortes = {cohorte1, cohorte1, cohorte2, cohorte3, cohorte1, cohorte2, cohorte3, cohorte1, cohorte1, cohorte2};
        int[] diasAtras = {25, 20, 18, 15, 12, 10, 8, 5, 3, 1};

        for (int i = 0; i < alumnosDatos.length; i++) {
            String nom = alumnosDatos[i][0];
            String ape = alumnosDatos[i][1];
            String mail = alumnosDatos[i][2];
            String dniVal = alumnosDatos[i][3];

            Usuario usu = new Usuario(nom, ape, mail, passwordEncoder.encode("123456"), rolAlumno);
            usu.setDni(dniVal);
            usu.setEmailValidado(true);
            Alumno alu = new Alumno(usu);
            usu.setAlumno(alu);
            usuarioRepository.save(usu);

            Cohorte coh = cohortes[i % cohortes.length];
            Inscripcion insc = new Inscripcion(coh, alu);
            LocalDateTime fechaInsc = LocalDateTime.now().minusDays(diasAtras[i]);
            insc.setFecha(fechaInsc);
            insc.setFechaVencimientoAcceso(fechaInsc.plusMonths(6));

            if (i == 0 || i == 4) {
                insc.setNumeroCertificado("CERT-2026-" + (1000 + i));
                insc.setNombreAlumno(nom + " " + ape);
                insc.setDniAlumno(dniVal);
                insc.setFechaEmisionCertificado(fechaInsc.plusDays(10));
                insc.setCertificadoEnviado(true);
            } else if (i == 1) {
                insc.setBaja(true);
            }

            inscripcionRepository.save(insc);

            float precioBase = coh.getPrograma().getCurso().getPrecio();
            if (precioBase > 0) {
                Descuento desc = (i % 2 == 0) ? descLanzamiento : null;
                float monto = (desc != null) ? precioBase * 0.85f : precioBase;
                Pago p = new Pago(monto, insc, estPagoAcred);
                p.setFecha(fechaInsc);
                p.setMetodoPago(metodoTarjeta);
                p.setDescuento(desc);
                p.setNombrePagador(nom + " " + ape);
                p.setDniPagador(dniVal);
                p.setDetalleEstado("accredited");
                p.setFechaAprobacion(fechaInsc);
                p.setNumeroComprobante("COMP-2026-" + (5000 + i));
                p.setFechaEmisionComprobante(fechaInsc);
                p.setComprobanteEnviado(true);
                pagoRepository.save(p);
            }
        }

        // ===============================================================
        // 9. AUDITORÍA Y REPORTES HISTÓRICOS (CU-95, CU-96 a CU-98)
        // ===============================================================
        auditoriaRepository.save(new Auditoria("Curso", curso1.getId(), adminUsuario, accionCrear));
        auditoriaRepository.save(new Auditoria("Cohorte", cohorte1.getId(), adminUsuario, accionCrear));
        auditoriaRepository.save(new Auditoria("Pago", pagoPrincipal.getId(), usuAlumnoPrincipal, accionCrear));

        Reporte repAlumnos = new Reporte(trAlumnos, adminActual, curso1);
        repAlumnos.setFechaGeneracion(LocalDateTime.now().minusDays(4));
        reporteRepository.save(repAlumnos);

        Reporte repIngresos = new Reporte(trIngresos, adminActual, curso1);
        repIngresos.setFechaGeneracion(LocalDateTime.now().minusDays(2));
        reporteRepository.save(repIngresos);

        System.out.println("🚀 Idóneos Online: ¡Semilla maestra cargada exitosamente con todos los 10 módulos poblados!");
    }
}
