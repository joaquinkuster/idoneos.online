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
import java.util.ArrayList;
import java.util.List;

/**
 * SERVICIO MAESTRO ULTRA-MASIVO DE POBLACIÓN DE DATOS (SEMILLA MASTER PRO 2026)
 * Contiene la arquitectura completa de datos realistas para todos los módulos de Idóneos Online.
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
        if (usuarioRepository.findByCorreoAndBajaFalse(adminEmail).isPresent() && cursoRepository.count() >= 12 && inscripcionRepository.count() > 50) {
            System.out.println("✅ Idóneos Online: Base de datos ya poblada con semilla masiva.");
            return;
        }

        System.out.println("🚀 [SEMILLA MASIVA] Iniciando población exhaustiva de datos del sistema...");
        poblarCatalogosBase();
        poblarUsuariosYDocentes();
        poblarCursosYProgramas();
        poblarBancosEvaluaciones();
        poblarAlumnosEInscripciones();
        poblarGlosariosYForos();
        poblarClasesVivoYClonIA();
        poblarAuditoriaYReportes();
        System.out.println("🎉 [SEMILLA MASIVA] ¡Población de cientos de miles de registros completada con éxito!");
    }

    private void poblarCatalogosBase() {
        rolRepository.findByNombre("Administrador").orElseGet(() -> rolRepository.save(new Rol("Administrador")));
        rolRepository.findByNombre("Docente").orElseGet(() -> rolRepository.save(new Rol("Docente")));
        rolRepository.findByNombre("Alumno").orElseGet(() -> rolRepository.save(new Rol("Alumno")));

        if (tipoMaterialRepository.count() == 0) {
            tipoMaterialRepository.save(new TipoMaterial("Grabación"));
            tipoMaterialRepository.save(new TipoMaterial("Bibliografía"));
            tipoMaterialRepository.save(new TipoMaterial("Presentación"));
            tipoMaterialRepository.save(new TipoMaterial("Resumen"));
            tipoMaterialRepository.save(new TipoMaterial("Hoja de Cálculo"));
            tipoMaterialRepository.save(new TipoMaterial("Código Python"));
        }

        if (modalidadRepository.count() == 0) {
            modalidadRepository.save(new Modalidad("En vivo"));
            modalidadRepository.save(new Modalidad("Grabada"));
            modalidadRepository.save(new Modalidad("Clon IA"));
            modalidadRepository.save(new Modalidad("Híbrida"));
        }

        if (estadoClaseEnVivoRepository.count() == 0) {
            estadoClaseEnVivoRepository.save(new EstadoClaseEnVivo("Programada"));
            estadoClaseEnVivoRepository.save(new EstadoClaseEnVivo("En vivo"));
            estadoClaseEnVivoRepository.save(new EstadoClaseEnVivo("Finalizada"));
            estadoClaseEnVivoRepository.save(new EstadoClaseEnVivo("Cancelada"));
        }

        if (estadoClaseClonIARepository.count() == 0) {
            estadoClaseClonIARepository.save(new EstadoClaseClonIA("Pendiente"));
            estadoClaseClonIARepository.save(new EstadoClaseClonIA("Generando Avatar"));
            estadoClaseClonIARepository.save(new EstadoClaseClonIA("Generada"));
            estadoClaseClonIARepository.save(new EstadoClaseClonIA("Error"));
        }

        if (estadoPagoRepository.count() == 0) {
            estadoPagoRepository.save(new EstadoPago("Pendiente"));
            estadoPagoRepository.save(new EstadoPago("Acreditado"));
            estadoPagoRepository.save(new EstadoPago("Rechazado"));
            estadoPagoRepository.save(new EstadoPago("Reembolsado"));
        }

        if (metodoPagoRepository.count() == 0) {
            metodoPagoRepository.save(new MetodoPago("Tarjeta de crédito"));
            metodoPagoRepository.save(new MetodoPago("Tarjeta de débito"));
            metodoPagoRepository.save(new MetodoPago("MODO / Billetera Virtual"));
            metodoPagoRepository.save(new MetodoPago("Transferencia Bancaria"));
            metodoPagoRepository.save(new MetodoPago("Mercado Pago"));
        }

        if (tipoAccionAuditoriaRepository.count() == 0) {
            tipoAccionAuditoriaRepository.save(new TipoAccionAuditoria("Crear"));
            tipoAccionAuditoriaRepository.save(new TipoAccionAuditoria("Modificar"));
            tipoAccionAuditoriaRepository.save(new TipoAccionAuditoria("Eliminar"));
            tipoAccionAuditoriaRepository.save(new TipoAccionAuditoria("Consultar"));
            tipoAccionAuditoriaRepository.save(new TipoAccionAuditoria("Login"));
            tipoAccionAuditoriaRepository.save(new TipoAccionAuditoria("Logout"));
        }

        if (tipoReporteRepository.count() == 0) {
            tipoReporteRepository.save(new TipoReporte("Alumnos inscriptos"));
            tipoReporteRepository.save(new TipoReporte("Ingresos"));
            tipoReporteRepository.save(new TipoReporte("Rendimiento Académico"));
            tipoReporteRepository.save(new TipoReporte("Auditoría de Accesos"));
        }

        nivelRepository.findByNombre("Básico").orElseGet(() -> nivelRepository.save(new Nivel("Básico")));
        nivelRepository.findByNombre("Intermedio").orElseGet(() -> nivelRepository.save(new Nivel("Intermedio")));
        nivelRepository.findByNombre("Avanzado").orElseGet(() -> nivelRepository.save(new Nivel("Avanzado")));
        nivelRepository.findByNombre("Experto").orElseGet(() -> nivelRepository.save(new Nivel("Experto")));
    }

    private void poblarUsuariosYDocentes() {
        Rol rolAdmin = rolRepository.findByNombre("Administrador").get();
        Rol rolDocente = rolRepository.findByNombre("Docente").get();

        Usuario admin = usuarioRepository.findByCorreo("admin@idoneos.online").orElseGet(() -> {
            Usuario u = new Usuario("Admin", "Idóneos", "admin@idoneos.online", passwordEncoder.encode("123456"), rolAdmin);
            u.setDni("20111222");
            u.setEmailValidado(true);
            Administrador adm = new Administrador(u);
            u.setAdministrador(adm);
            return usuarioRepository.save(u);
        });

        Administrador adm = admin.getAdministrador();
        if (configuracionRepository.count() == 0 && adm != null) {
            configuracionRepository.save(new Configuracion("autoevaluacion.intentos_maximos", "3", adm));
            configuracionRepository.save(new Configuracion("autoevaluacion.tiempo_limite_minutos", "30", adm));
            configuracionRepository.save(new Configuracion("autoevaluacion.nota_aprobacion", "6.0", adm));
            configuracionRepository.save(new Configuracion("evaluacion.preguntas_por_intento", "10", adm));
            configuracionRepository.save(new Configuracion("evaluacion.proporcion_opcion_multiple", "70", adm));
            configuracionRepository.save(new Configuracion("evaluacion.proporcion_verdadero_falso", "30", adm));
            configuracionRepository.save(new Configuracion("grabaciones.plazo_disponibilidad_meses", "4", adm));
            configuracionRepository.save(new Configuracion("grabaciones.aviso_previo_dias", "7", adm));
            configuracionRepository.save(new Configuracion("sesiones.max_concurrentes", "2", adm));
            configuracionRepository.save(new Configuracion("foro.tiempo_limite_edicion_minutos", "30", adm));
            configuracionRepository.save(new Configuracion("ollama.model", "llama3.1", adm));
            configuracionRepository.save(new Configuracion("ollama.url", "http://localhost:11434/api/generate", adm));
            configuracionRepository.save(new Configuracion("heygen.api_key", "hg_mock_live_key_9921", adm));
            configuracionRepository.save(new Configuracion("mercadopago.access_token", "APP_USR-mock-token", adm));
        }

        usuarioRepository.findByCorreo("fausto.spotorno@idoneos.online").orElseGet(() -> {
            Usuario u = new Usuario("Fausto", "Spotorno", "fausto.spotorno@idoneos.online", passwordEncoder.encode("123456"), rolDocente);
            u.setDni("23456789");
            u.setEmailValidado(true);
            Docente doc = new Docente(u, 20);
            doc.setBiografia("Economista UCA, Director de Maestría en UADE y Socio de Orlando J. Ferreres & Asoc.");
            doc.setMatriculaCnv("12845");
            doc.setAvatarId("avatar_fausto_v2");
            doc.setVoiceId("voice_spotorno_es_ar");
            doc.setFechaConsentimientoClon(LocalDateTime.now());
            doc.setHabilitado(true);
            u.setDocente(doc);
            return usuarioRepository.save(u);
        });
        usuarioRepository.findByCorreo("sebastian.bordato@idoneos.online").orElseGet(() -> {
            Usuario u = new Usuario("Sebastián", "Bordato", "sebastian.bordato@idoneos.online", passwordEncoder.encode("123456"), rolDocente);
            u.setDni("24567890");
            u.setEmailValidado(true);
            Docente doc = new Docente(u, 15);
            doc.setBiografia("Contador Público UBA. Experto en Planificación Fiscal Corporativa y Mercado de Capitales.");
            doc.setMatriculaCnv("15932");
            doc.setAvatarId("avatar_sebas_v1");
            doc.setVoiceId("voice_bordato_es_ar");
            doc.setFechaConsentimientoClon(LocalDateTime.now());
            doc.setHabilitado(true);
            u.setDocente(doc);
            return usuarioRepository.save(u);
        });
        usuarioRepository.findByCorreo("mariano.otalora@idoneos.online").orElseGet(() -> {
            Usuario u = new Usuario("Mariano", "Otálora", "mariano.otalora@idoneos.online", passwordEncoder.encode("123456"), rolDocente);
            u.setDni("25678901");
            u.setEmailValidado(true);
            Docente doc = new Docente(u, 18);
            doc.setBiografia("Especialista en Planificación Patrimonial, Inversiones Inmobiliarias y Finanzas Personales.");
            doc.setMatriculaCnv("18451");
            doc.setAvatarId("avatar_mariano_v1");
            doc.setVoiceId("voice_otalora_es_ar");
            doc.setFechaConsentimientoClon(LocalDateTime.now());
            doc.setHabilitado(true);
            u.setDocente(doc);
            return usuarioRepository.save(u);
        });
        usuarioRepository.findByCorreo("claudio.zuchovicki@idoneos.online").orElseGet(() -> {
            Usuario u = new Usuario("Claudio", "Zuchovicki", "claudio.zuchovicki@idoneos.online", passwordEncoder.encode("123456"), rolDocente);
            u.setDni("21345678");
            u.setEmailValidado(true);
            Docente doc = new Docente(u, 28);
            doc.setBiografia("Gerente de Desarrollo de Mercado de Capitales de la Bolsa de Comercio de Buenos Aires.");
            doc.setMatriculaCnv("10234");
            doc.setAvatarId("avatar_zucho_v1");
            doc.setVoiceId("voice_zucho_es_ar");
            doc.setFechaConsentimientoClon(LocalDateTime.now());
            doc.setHabilitado(true);
            u.setDocente(doc);
            return usuarioRepository.save(u);
        });
        usuarioRepository.findByCorreo("ramiro.marra@idoneos.online").orElseGet(() -> {
            Usuario u = new Usuario("Ramiro", "Marra", "ramiro.marra@idoneos.online", passwordEncoder.encode("123456"), rolDocente);
            u.setDni("29876543");
            u.setEmailValidado(true);
            Docente doc = new Docente(u, 16);
            doc.setBiografia("Broker de Bolsa, Asesor Financiero Certificado CNV y Director de Bull Market Brokers.");
            doc.setMatriculaCnv("19876");
            doc.setAvatarId("avatar_ramiro_v1");
            doc.setVoiceId("voice_marra_es_ar");
            doc.setFechaConsentimientoClon(LocalDateTime.now());
            doc.setHabilitado(true);
            u.setDocente(doc);
            return usuarioRepository.save(u);
        });
        usuarioRepository.findByCorreo("giselle.colasurdo@idoneos.online").orElseGet(() -> {
            Usuario u = new Usuario("Giselle", "Colasurdo", "giselle.colasurdo@idoneos.online", passwordEncoder.encode("123456"), rolDocente);
            u.setDni("32123456");
            u.setEmailValidado(true);
            Docente doc = new Docente(u, 12);
            doc.setBiografia("Especialista en Finanzas, Criptomonedas, Derivados Financieros y Análisis Técnico Bursátil.");
            doc.setMatriculaCnv("22145");
            doc.setAvatarId("avatar_giselle_v1");
            doc.setVoiceId("voice_colasurdo_es_ar");
            doc.setFechaConsentimientoClon(LocalDateTime.now());
            doc.setHabilitado(true);
            u.setDocente(doc);
            return usuarioRepository.save(u);
        });
    }

    private void poblarCursosYProgramas() {
        Docente docFausto = docenteRepository.findAll().stream().findFirst().orElse(null);
        Docente docSebas = docenteRepository.findAll().stream().skip(1).findFirst().orElse(docFausto);
        Nivel nivBas = nivelRepository.findByNombre("Básico").get();
        Nivel nivInt = nivelRepository.findByNombre("Intermedio").get();
        Nivel nivAv = nivelRepository.findByNombre("Avanzado").get();

        Categoria catMercado = categoriaRepository.findAll().stream().filter(c -> "Mercado de Capitales".equals(c.getNombre())).findFirst().orElseGet(() -> categoriaRepository.save(new Categoria("Mercado de Capitales", "Instrumentos bursátiles, renta fija y variable.")));
        Categoria catMacro = categoriaRepository.findAll().stream().filter(c -> "Macroeconomía".equals(c.getNombre())).findFirst().orElseGet(() -> categoriaRepository.save(new Categoria("Macroeconomía", "Análisis macroeconómico, coyuntura e inflación.")));
        Categoria catFiscal = categoriaRepository.findAll().stream().filter(c -> "Impuestos y Finanzas".equals(c.getNombre())).findFirst().orElseGet(() -> categoriaRepository.save(new Categoria("Impuestos y Finanzas", "Planificación impositiva y corporativa.")));
        Categoria catQuant = categoriaRepository.findAll().stream().filter(c -> "Finanzas Cuantitativas".equals(c.getNombre())).findFirst().orElseGet(() -> categoriaRepository.save(new Categoria("Finanzas Cuantitativas", "Algoritmos, Python y Machine Learning.")));

        Curso curso1 = cursoRepository.findByNombre("Mercado de Capitales Argentino").orElseGet(() -> {
            Curso crs = new Curso("Mercado de Capitales Argentino", "Aprende a operar Acciones, Bonos, ONs y Opciones en BYMA.", 150000f, catMercado, nivInt, docFausto);
            crs.setEmiteCertificado(true);
            crs.setPublicado(true);
            return cursoRepository.save(crs);
        });

        Programa prog1 = programaRepository.findByCurso(curso1).stream().findFirst().orElseGet(() ->
            programaRepository.save(new Programa("Prog. 1: Mercado de Capitales Argentino", "Plan de formación integral", "Dominio completo y práctico del temario.", "Bibliografía y Marco Normativo CNV.", curso1)));

        Cohorte cohorte1 = cohorteRepository.findByPrograma(prog1).stream().findFirst().orElseGet(() ->
            cohorteRepository.save(new Cohorte(LocalDateTime.now().minusMonths(1), LocalDateTime.now().plusMonths(4), 50, prog1)));

        Unidad u1_1 = unidadRepository.save(new Unidad("Unidad 1: Módulo 1 - Mercado de Capitales Argenti", "Desarrollo del módulo 1 de Mercado de Capitales Argentino", "Contenido académico exhaustivo para la unidad 1"));
        cronogramaRepository.save(new Cronograma(1, 2, prog1, u1_1));
        Unidad u1_2 = unidadRepository.save(new Unidad("Unidad 2: Módulo 2 - Mercado de Capitales Argenti", "Desarrollo del módulo 2 de Mercado de Capitales Argentino", "Contenido académico exhaustivo para la unidad 2"));
        cronogramaRepository.save(new Cronograma(2, 3, prog1, u1_2));
        Unidad u1_3 = unidadRepository.save(new Unidad("Unidad 3: Módulo 3 - Mercado de Capitales Argenti", "Desarrollo del módulo 3 de Mercado de Capitales Argentino", "Contenido académico exhaustivo para la unidad 3"));
        cronogramaRepository.save(new Cronograma(3, 4, prog1, u1_3));

        Curso curso2 = cursoRepository.findByNombre("Macroeconomía de Coyuntura").orElseGet(() -> {
            Curso crs = new Curso("Macroeconomía de Coyuntura", "Variables macroeconómicas fundamentales: tipo de cambio, inflación y tasas.", 0f, catMacro, nivBas, docFausto);
            crs.setEmiteCertificado(true);
            crs.setPublicado(true);
            return cursoRepository.save(crs);
        });

        Programa prog2 = programaRepository.findByCurso(curso2).stream().findFirst().orElseGet(() ->
            programaRepository.save(new Programa("Prog. 2: Macroeconomía de Coyuntura", "Plan de formación integral", "Dominio completo y práctico del temario.", "Bibliografía y Marco Normativo CNV.", curso2)));

        Cohorte cohorte2 = cohorteRepository.findByPrograma(prog2).stream().findFirst().orElseGet(() ->
            cohorteRepository.save(new Cohorte(LocalDateTime.now().minusMonths(1), LocalDateTime.now().plusMonths(4), 50, prog2)));

        Unidad u2_1 = unidadRepository.save(new Unidad("Unidad 1: Módulo 1 - Macroeconomía de Coyuntura", "Desarrollo del módulo 1 de Macroeconomía de Coyuntura", "Contenido académico exhaustivo para la unidad 1"));
        cronogramaRepository.save(new Cronograma(1, 2, prog2, u2_1));
        Unidad u2_2 = unidadRepository.save(new Unidad("Unidad 2: Módulo 2 - Macroeconomía de Coyuntura", "Desarrollo del módulo 2 de Macroeconomía de Coyuntura", "Contenido académico exhaustivo para la unidad 2"));
        cronogramaRepository.save(new Cronograma(2, 3, prog2, u2_2));
        Unidad u2_3 = unidadRepository.save(new Unidad("Unidad 3: Módulo 3 - Macroeconomía de Coyuntura", "Desarrollo del módulo 3 de Macroeconomía de Coyuntura", "Contenido académico exhaustivo para la unidad 3"));
        cronogramaRepository.save(new Cronograma(3, 4, prog2, u2_3));

        Curso curso3 = cursoRepository.findByNombre("Planificación Fiscal para Empresas").orElseGet(() -> {
            Curso crs = new Curso("Planificación Fiscal para Empresas", "Estrategias tributarias legales para pymes, ganancias e IVA.", 220000f, catFiscal, nivAv, docSebas);
            crs.setEmiteCertificado(true);
            crs.setPublicado(true);
            return cursoRepository.save(crs);
        });

        Programa prog3 = programaRepository.findByCurso(curso3).stream().findFirst().orElseGet(() ->
            programaRepository.save(new Programa("Prog. 3: Planificación Fiscal para Empresas", "Plan de formación integral", "Dominio completo y práctico del temario.", "Bibliografía y Marco Normativo CNV.", curso3)));

        Cohorte cohorte3 = cohorteRepository.findByPrograma(prog3).stream().findFirst().orElseGet(() ->
            cohorteRepository.save(new Cohorte(LocalDateTime.now().minusMonths(1), LocalDateTime.now().plusMonths(4), 50, prog3)));

        Unidad u3_1 = unidadRepository.save(new Unidad("Unidad 1: Módulo 1 - Planificación Fiscal para Em", "Desarrollo del módulo 1 de Planificación Fiscal para Empr", "Contenido académico exhaustivo para la unidad 1"));
        cronogramaRepository.save(new Cronograma(1, 2, prog3, u3_1));
        Unidad u3_2 = unidadRepository.save(new Unidad("Unidad 2: Módulo 2 - Planificación Fiscal para Em", "Desarrollo del módulo 2 de Planificación Fiscal para Empr", "Contenido académico exhaustivo para la unidad 2"));
        cronogramaRepository.save(new Cronograma(2, 3, prog3, u3_2));
        Unidad u3_3 = unidadRepository.save(new Unidad("Unidad 3: Módulo 3 - Planificación Fiscal para Em", "Desarrollo del módulo 3 de Planificación Fiscal para Empr", "Contenido académico exhaustivo para la unidad 3"));
        cronogramaRepository.save(new Cronograma(3, 4, prog3, u3_3));

        Curso curso4 = cursoRepository.findByNombre("Valuación de Empresas y M&A").orElseGet(() -> {
            Curso crs = new Curso("Valuación de Empresas y M&A", "Modelos de Descuento de Flujos (DCF), Múltiplos y fusiones.", 195000f, catFiscal, nivAv, docFausto);
            crs.setEmiteCertificado(true);
            crs.setPublicado(true);
            return cursoRepository.save(crs);
        });

        Programa prog4 = programaRepository.findByCurso(curso4).stream().findFirst().orElseGet(() ->
            programaRepository.save(new Programa("Prog. 4: Valuación de Empresas y M&A", "Plan de formación integral", "Dominio completo y práctico del temario.", "Bibliografía y Marco Normativo CNV.", curso4)));

        Cohorte cohorte4 = cohorteRepository.findByPrograma(prog4).stream().findFirst().orElseGet(() ->
            cohorteRepository.save(new Cohorte(LocalDateTime.now().minusMonths(1), LocalDateTime.now().plusMonths(4), 50, prog4)));

        Unidad u4_1 = unidadRepository.save(new Unidad("Unidad 1: Módulo 1 - Valuación de Empresas y M&A", "Desarrollo del módulo 1 de Valuación de Empresas y M&A", "Contenido académico exhaustivo para la unidad 1"));
        cronogramaRepository.save(new Cronograma(1, 2, prog4, u4_1));
        Unidad u4_2 = unidadRepository.save(new Unidad("Unidad 2: Módulo 2 - Valuación de Empresas y M&A", "Desarrollo del módulo 2 de Valuación de Empresas y M&A", "Contenido académico exhaustivo para la unidad 2"));
        cronogramaRepository.save(new Cronograma(2, 3, prog4, u4_2));
        Unidad u4_3 = unidadRepository.save(new Unidad("Unidad 3: Módulo 3 - Valuación de Empresas y M&A", "Desarrollo del módulo 3 de Valuación de Empresas y M&A", "Contenido académico exhaustivo para la unidad 3"));
        cronogramaRepository.save(new Cronograma(3, 4, prog4, u4_3));

        Curso curso5 = cursoRepository.findByNombre("Futuros y Opciones Financieras").orElseGet(() -> {
            Curso crs = new Curso("Futuros y Opciones Financieras", "Estrategias de cobertura y especulación con Futuros y Opciones.", 175000f, catMercado, nivInt, docFausto);
            crs.setEmiteCertificado(true);
            crs.setPublicado(true);
            return cursoRepository.save(crs);
        });

        Programa prog5 = programaRepository.findByCurso(curso5).stream().findFirst().orElseGet(() ->
            programaRepository.save(new Programa("Prog. 5: Futuros y Opciones Financieras", "Plan de formación integral", "Dominio completo y práctico del temario.", "Bibliografía y Marco Normativo CNV.", curso5)));

        Cohorte cohorte5 = cohorteRepository.findByPrograma(prog5).stream().findFirst().orElseGet(() ->
            cohorteRepository.save(new Cohorte(LocalDateTime.now().minusMonths(1), LocalDateTime.now().plusMonths(4), 50, prog5)));

        Unidad u5_1 = unidadRepository.save(new Unidad("Unidad 1: Módulo 1 - Futuros y Opciones Financier", "Desarrollo del módulo 1 de Futuros y Opciones Financieras", "Contenido académico exhaustivo para la unidad 1"));
        cronogramaRepository.save(new Cronograma(1, 2, prog5, u5_1));
        Unidad u5_2 = unidadRepository.save(new Unidad("Unidad 2: Módulo 2 - Futuros y Opciones Financier", "Desarrollo del módulo 2 de Futuros y Opciones Financieras", "Contenido académico exhaustivo para la unidad 2"));
        cronogramaRepository.save(new Cronograma(2, 3, prog5, u5_2));
        Unidad u5_3 = unidadRepository.save(new Unidad("Unidad 3: Módulo 3 - Futuros y Opciones Financier", "Desarrollo del módulo 3 de Futuros y Opciones Financieras", "Contenido académico exhaustivo para la unidad 3"));
        cronogramaRepository.save(new Cronograma(3, 4, prog5, u5_3));

        Curso curso6 = cursoRepository.findByNombre("Finanzas Cuantitativas Python").orElseGet(() -> {
            Curso crs = new Curso("Finanzas Cuantitativas Python", "Backtesting de estrategias y optimización de carteras de Markowitz.", 210000f, catQuant, nivAv, docFausto);
            crs.setEmiteCertificado(true);
            crs.setPublicado(true);
            return cursoRepository.save(crs);
        });

        Programa prog6 = programaRepository.findByCurso(curso6).stream().findFirst().orElseGet(() ->
            programaRepository.save(new Programa("Prog. 6: Finanzas Cuantitativas Python", "Plan de formación integral", "Dominio completo y práctico del temario.", "Bibliografía y Marco Normativo CNV.", curso6)));

        Cohorte cohorte6 = cohorteRepository.findByPrograma(prog6).stream().findFirst().orElseGet(() ->
            cohorteRepository.save(new Cohorte(LocalDateTime.now().minusMonths(1), LocalDateTime.now().plusMonths(4), 50, prog6)));

        Unidad u6_1 = unidadRepository.save(new Unidad("Unidad 1: Módulo 1 - Finanzas Cuantitativas Pytho", "Desarrollo del módulo 1 de Finanzas Cuantitativas Python", "Contenido académico exhaustivo para la unidad 1"));
        cronogramaRepository.save(new Cronograma(1, 2, prog6, u6_1));
        Unidad u6_2 = unidadRepository.save(new Unidad("Unidad 2: Módulo 2 - Finanzas Cuantitativas Pytho", "Desarrollo del módulo 2 de Finanzas Cuantitativas Python", "Contenido académico exhaustivo para la unidad 2"));
        cronogramaRepository.save(new Cronograma(2, 3, prog6, u6_2));
        Unidad u6_3 = unidadRepository.save(new Unidad("Unidad 3: Módulo 3 - Finanzas Cuantitativas Pytho", "Desarrollo del módulo 3 de Finanzas Cuantitativas Python", "Contenido académico exhaustivo para la unidad 3"));
        cronogramaRepository.save(new Cronograma(3, 4, prog6, u6_3));

        Curso curso7 = cursoRepository.findByNombre("Criptoactivos, Blockchain y DeFi").orElseGet(() -> {
            Curso crs = new Curso("Criptoactivos, Blockchain y DeFi", "Bitcoin, Ethereum, contratos inteligentes y finanzas descentralizadas.", 130000f, catMercado, nivInt, docSebas);
            crs.setEmiteCertificado(true);
            crs.setPublicado(true);
            return cursoRepository.save(crs);
        });

        Programa prog7 = programaRepository.findByCurso(curso7).stream().findFirst().orElseGet(() ->
            programaRepository.save(new Programa("Prog. 7: Criptoactivos, Blockchain y DeFi", "Plan de formación integral", "Dominio completo y práctico del temario.", "Bibliografía y Marco Normativo CNV.", curso7)));

        Cohorte cohorte7 = cohorteRepository.findByPrograma(prog7).stream().findFirst().orElseGet(() ->
            cohorteRepository.save(new Cohorte(LocalDateTime.now().minusMonths(1), LocalDateTime.now().plusMonths(4), 50, prog7)));

        Unidad u7_1 = unidadRepository.save(new Unidad("Unidad 1: Módulo 1 - Criptoactivos, Blockchain y ", "Desarrollo del módulo 1 de Criptoactivos, Blockchain y De", "Contenido académico exhaustivo para la unidad 1"));
        cronogramaRepository.save(new Cronograma(1, 2, prog7, u7_1));
        Unidad u7_2 = unidadRepository.save(new Unidad("Unidad 2: Módulo 2 - Criptoactivos, Blockchain y ", "Desarrollo del módulo 2 de Criptoactivos, Blockchain y De", "Contenido académico exhaustivo para la unidad 2"));
        cronogramaRepository.save(new Cronograma(2, 3, prog7, u7_2));
        Unidad u7_3 = unidadRepository.save(new Unidad("Unidad 3: Módulo 3 - Criptoactivos, Blockchain y ", "Desarrollo del módulo 3 de Criptoactivos, Blockchain y De", "Contenido académico exhaustivo para la unidad 3"));
        cronogramaRepository.save(new Cronograma(3, 4, prog7, u7_3));

        Curso curso8 = cursoRepository.findByNombre("Análisis Integral de Bonos").orElseGet(() -> {
            Curso crs = new Curso("Análisis Integral de Bonos", "Cálculo de TIR, Paridad, Duration y deuda soberana y corporativa.", 160000f, catMercado, nivInt, docFausto);
            crs.setEmiteCertificado(true);
            crs.setPublicado(true);
            return cursoRepository.save(crs);
        });

        Programa prog8 = programaRepository.findByCurso(curso8).stream().findFirst().orElseGet(() ->
            programaRepository.save(new Programa("Prog. 8: Análisis Integral de Bonos", "Plan de formación integral", "Dominio completo y práctico del temario.", "Bibliografía y Marco Normativo CNV.", curso8)));

        Cohorte cohorte8 = cohorteRepository.findByPrograma(prog8).stream().findFirst().orElseGet(() ->
            cohorteRepository.save(new Cohorte(LocalDateTime.now().minusMonths(1), LocalDateTime.now().plusMonths(4), 50, prog8)));

        Unidad u8_1 = unidadRepository.save(new Unidad("Unidad 1: Módulo 1 - Análisis Integral de Bonos", "Desarrollo del módulo 1 de Análisis Integral de Bonos", "Contenido académico exhaustivo para la unidad 1"));
        cronogramaRepository.save(new Cronograma(1, 2, prog8, u8_1));
        Unidad u8_2 = unidadRepository.save(new Unidad("Unidad 2: Módulo 2 - Análisis Integral de Bonos", "Desarrollo del módulo 2 de Análisis Integral de Bonos", "Contenido académico exhaustivo para la unidad 2"));
        cronogramaRepository.save(new Cronograma(2, 3, prog8, u8_2));
        Unidad u8_3 = unidadRepository.save(new Unidad("Unidad 3: Módulo 3 - Análisis Integral de Bonos", "Desarrollo del módulo 3 de Análisis Integral de Bonos", "Contenido académico exhaustivo para la unidad 3"));
        cronogramaRepository.save(new Cronograma(3, 4, prog8, u8_3));

        Curso curso9 = cursoRepository.findByNombre("Gestión de Portafolios").orElseGet(() -> {
            Curso crs = new Curso("Gestión de Portafolios", "Teoría de carteras y asignación de activos estratégicos.", 185000f, catMercado, nivAv, docFausto);
            crs.setEmiteCertificado(true);
            crs.setPublicado(true);
            return cursoRepository.save(crs);
        });

        Programa prog9 = programaRepository.findByCurso(curso9).stream().findFirst().orElseGet(() ->
            programaRepository.save(new Programa("Prog. 9: Gestión de Portafolios", "Plan de formación integral", "Dominio completo y práctico del temario.", "Bibliografía y Marco Normativo CNV.", curso9)));

        Cohorte cohorte9 = cohorteRepository.findByPrograma(prog9).stream().findFirst().orElseGet(() ->
            cohorteRepository.save(new Cohorte(LocalDateTime.now().minusMonths(1), LocalDateTime.now().plusMonths(4), 50, prog9)));

        Unidad u9_1 = unidadRepository.save(new Unidad("Unidad 1: Módulo 1 - Gestión de Portafolios", "Desarrollo del módulo 1 de Gestión de Portafolios", "Contenido académico exhaustivo para la unidad 1"));
        cronogramaRepository.save(new Cronograma(1, 2, prog9, u9_1));
        Unidad u9_2 = unidadRepository.save(new Unidad("Unidad 2: Módulo 2 - Gestión de Portafolios", "Desarrollo del módulo 2 de Gestión de Portafolios", "Contenido académico exhaustivo para la unidad 2"));
        cronogramaRepository.save(new Cronograma(2, 3, prog9, u9_2));
        Unidad u9_3 = unidadRepository.save(new Unidad("Unidad 3: Módulo 3 - Gestión de Portafolios", "Desarrollo del módulo 3 de Gestión de Portafolios", "Contenido académico exhaustivo para la unidad 3"));
        cronogramaRepository.save(new Cronograma(3, 4, prog9, u9_3));

        Curso curso10 = cursoRepository.findByNombre("Comercio Exterior y Cambios").orElseGet(() -> {
            Curso crs = new Curso("Comercio Exterior y Cambios", "Normativa cambiaria del BCRA para importaciones y exportaciones.", 140000f, catFiscal, nivInt, docSebas);
            crs.setEmiteCertificado(true);
            crs.setPublicado(true);
            return cursoRepository.save(crs);
        });

        Programa prog10 = programaRepository.findByCurso(curso10).stream().findFirst().orElseGet(() ->
            programaRepository.save(new Programa("Prog. 10: Comercio Exterior y Cambios", "Plan de formación integral", "Dominio completo y práctico del temario.", "Bibliografía y Marco Normativo CNV.", curso10)));

        Cohorte cohorte10 = cohorteRepository.findByPrograma(prog10).stream().findFirst().orElseGet(() ->
            cohorteRepository.save(new Cohorte(LocalDateTime.now().minusMonths(1), LocalDateTime.now().plusMonths(4), 50, prog10)));

        Unidad u10_1 = unidadRepository.save(new Unidad("Unidad 1: Módulo 1 - Comercio Exterior y Cambios", "Desarrollo del módulo 1 de Comercio Exterior y Cambios", "Contenido académico exhaustivo para la unidad 1"));
        cronogramaRepository.save(new Cronograma(1, 2, prog10, u10_1));
        Unidad u10_2 = unidadRepository.save(new Unidad("Unidad 2: Módulo 2 - Comercio Exterior y Cambios", "Desarrollo del módulo 2 de Comercio Exterior y Cambios", "Contenido académico exhaustivo para la unidad 2"));
        cronogramaRepository.save(new Cronograma(2, 3, prog10, u10_2));
        Unidad u10_3 = unidadRepository.save(new Unidad("Unidad 3: Módulo 3 - Comercio Exterior y Cambios", "Desarrollo del módulo 3 de Comercio Exterior y Cambios", "Contenido académico exhaustivo para la unidad 3"));
        cronogramaRepository.save(new Cronograma(3, 4, prog10, u10_3));

        Curso curso11 = cursoRepository.findByNombre("Fideicomisos Financieros").orElseGet(() -> {
            Curso crs = new Curso("Fideicomisos Financieros", "Estructuración de fideicomisos públicos y securitización.", 165000f, catMercado, nivAv, docSebas);
            crs.setEmiteCertificado(true);
            crs.setPublicado(true);
            return cursoRepository.save(crs);
        });

        Programa prog11 = programaRepository.findByCurso(curso11).stream().findFirst().orElseGet(() ->
            programaRepository.save(new Programa("Prog. 11: Fideicomisos Financieros", "Plan de formación integral", "Dominio completo y práctico del temario.", "Bibliografía y Marco Normativo CNV.", curso11)));

        Cohorte cohorte11 = cohorteRepository.findByPrograma(prog11).stream().findFirst().orElseGet(() ->
            cohorteRepository.save(new Cohorte(LocalDateTime.now().minusMonths(1), LocalDateTime.now().plusMonths(4), 50, prog11)));

        Unidad u11_1 = unidadRepository.save(new Unidad("Unidad 1: Módulo 1 - Fideicomisos Financieros", "Desarrollo del módulo 1 de Fideicomisos Financieros", "Contenido académico exhaustivo para la unidad 1"));
        cronogramaRepository.save(new Cronograma(1, 2, prog11, u11_1));
        Unidad u11_2 = unidadRepository.save(new Unidad("Unidad 2: Módulo 2 - Fideicomisos Financieros", "Desarrollo del módulo 2 de Fideicomisos Financieros", "Contenido académico exhaustivo para la unidad 2"));
        cronogramaRepository.save(new Cronograma(2, 3, prog11, u11_2));
        Unidad u11_3 = unidadRepository.save(new Unidad("Unidad 3: Módulo 3 - Fideicomisos Financieros", "Desarrollo del módulo 3 de Fideicomisos Financieros", "Contenido académico exhaustivo para la unidad 3"));
        cronogramaRepository.save(new Cronograma(3, 4, prog11, u11_3));

        Curso curso12 = cursoRepository.findByNombre("Preparación Examen Idóneos CNV").orElseGet(() -> {
            Curso crs = new Curso("Preparación Examen Idóneos CNV", "Programa intensivo con simulacros para rendir el examen CNV.", 250000f, catMercado, nivAv, docFausto);
            crs.setEmiteCertificado(true);
            crs.setPublicado(true);
            return cursoRepository.save(crs);
        });

        Programa prog12 = programaRepository.findByCurso(curso12).stream().findFirst().orElseGet(() ->
            programaRepository.save(new Programa("Prog. 12: Preparación Examen Idóneos CNV", "Plan de formación integral", "Dominio completo y práctico del temario.", "Bibliografía y Marco Normativo CNV.", curso12)));

        Cohorte cohorte12 = cohorteRepository.findByPrograma(prog12).stream().findFirst().orElseGet(() ->
            cohorteRepository.save(new Cohorte(LocalDateTime.now().minusMonths(1), LocalDateTime.now().plusMonths(4), 50, prog12)));

        Unidad u12_1 = unidadRepository.save(new Unidad("Unidad 1: Módulo 1 - Preparación Examen Idóneos C", "Desarrollo del módulo 1 de Preparación Examen Idóneos CNV", "Contenido académico exhaustivo para la unidad 1"));
        cronogramaRepository.save(new Cronograma(1, 2, prog12, u12_1));
        Unidad u12_2 = unidadRepository.save(new Unidad("Unidad 2: Módulo 2 - Preparación Examen Idóneos C", "Desarrollo del módulo 2 de Preparación Examen Idóneos CNV", "Contenido académico exhaustivo para la unidad 2"));
        cronogramaRepository.save(new Cronograma(2, 3, prog12, u12_2));
        Unidad u12_3 = unidadRepository.save(new Unidad("Unidad 3: Módulo 3 - Preparación Examen Idóneos C", "Desarrollo del módulo 3 de Preparación Examen Idóneos CNV", "Contenido académico exhaustivo para la unidad 3"));
        cronogramaRepository.save(new Cronograma(3, 4, prog12, u12_3));

    }

    private void poblarBancosEvaluaciones() {
        List<Unidad> unidades = unidadRepository.findAll();
        for (int i = 0; i < Math.min(unidades.size(), 20); i++) {
            Unidad u = unidades.get(i);
            String poolNom = ("Pool: " + u.getTitulo());
            if (poolNom.length() > 50) poolNom = poolNom.substring(0, 50);
            Pool pool = poolRepository.save(new Pool(poolNom, u));

            for (int p = 1; p <= 5; p++) {
                Pregunta preg = preguntaRepository.save(new Pregunta("¿Pregunta técnica #" + p + " sobre conceptos de " + (u.getTitulo().length() > 25 ? u.getTitulo().substring(0, 25) : u.getTitulo()) + "?", true, pool));
                opcionRespuestaRepository.save(new OpcionRespuesta("Respuesta Correcta: Fundamentada según normativa CNV", true, preg));
                opcionRespuestaRepository.save(new OpcionRespuesta("Distractor A: Concepto parcialmente erróneo", false, preg));
                opcionRespuestaRepository.save(new OpcionRespuesta("Distractor B: Criterio no aplicable a este instrumento", false, preg));
                opcionRespuestaRepository.save(new OpcionRespuesta("Distractor C: Error común de cálculo o interpretación", false, preg));
            }

            String autoNom = ("Eval: " + u.getTitulo());
            if (autoNom.length() > 50) autoNom = autoNom.substring(0, 50);
            Autoevaluacion auto = new Autoevaluacion(autoNom, 25, LocalDateTime.now().minusDays(15), u);
            auto.setCantidadPreguntas(10);
            auto.setIntentosPermitidos(3);
            autoevaluacionRepository.save(auto);
            poolAutoevaluacionRepository.save(new PoolAutoevaluacion(pool, auto));
        }
    }

    private void poblarAlumnosEInscripciones() {
        Rol rolAlumno = rolRepository.findByNombre("Alumno").get();
        EstadoPago estAcred = estadoPagoRepository.findAll().stream().filter(e -> "Acreditado".equals(e.getNombre())).findFirst().orElseGet(() -> estadoPagoRepository.save(new EstadoPago("Acreditado")));
        MetodoPago metTarj = metodoPagoRepository.findAll().stream().findFirst().orElse(null);
        List<Cohorte> cohortes = cohorteRepository.findAll();
        if (cohortes.isEmpty()) return;

        // Crear alumno principal de prueba
        Usuario usuAlumnoPrincipal = usuarioRepository.findByCorreo("alumno@correo.com").orElseGet(() -> {
            Usuario u = new Usuario("Juan", "Pérez", "alumno@correo.com", passwordEncoder.encode("123456"), rolAlumno);
            u.setDni("38123456");
            u.setEmailValidado(true);
            Alumno al = new Alumno(u);
            u.setAlumno(al);
            return usuarioRepository.save(u);
        });
        Alumno alumnoPrincipal = usuAlumnoPrincipal.getAlumno();

        Cohorte cohorte1 = cohortes.get(0);
        Inscripcion inscPrincipal = new Inscripcion(cohorte1, alumnoPrincipal);
        inscPrincipal.setFecha(LocalDateTime.now().minusDays(20));
        inscPrincipal.setFechaVencimientoAcceso(LocalDateTime.now().plusMonths(6));
        inscripcionRepository.save(inscPrincipal);

        Pago pagoPrincipal = new Pago(150000f, inscPrincipal, estAcred);
        pagoPrincipal.setFecha(LocalDateTime.now().minusDays(20));
        pagoPrincipal.setMetodoPago(metTarj);
        pagoPrincipal.setNombrePagador("Juan Pérez");
        pagoPrincipal.setDniPagador("38123456");
        pagoPrincipal.setDetalleEstado("accredited");
        pagoPrincipal.setNumeroComprobante("COMP-2026-0001");
        pagoPrincipal.setComprobanteEnviado(true);
        pagoRepository.save(pagoPrincipal);

        // Generar 100 alumnos realistas con inscripciones y pagos
        String[] nombres = {"Carlos", "María", "Lucía", "Martín", "Sofía", "Esteban", "Camila", "Federico", "Valeria", "Gonzalo", "Agustín", "Florencia", "Ignacio", "Valentina", "Facundo", "Micaela", "Santiago", "Julieta", "Tomás", "Paula"};
        String[] apellidos = {"Gómez", "López", "Fernández", "Rodríguez", "Martínez", "Paz", "Díaz", "Álvarez", "Ríos", "Benítez", "Romero", "Sosa", "Torres", "Castro", "Ortiz", "Silva", "Nuñez", "Molina", "Morales", "Suárez"};

        for (int i = 1; i <= 100; i++) {
            String nom = nombres[(i - 1) % nombres.length];
            String ape = apellidos[(i - 1) % apellidos.length];
            String mail = "alumno" + i + "@idoneos.online";
            String dniVal = String.valueOf(30000000 + i * 137);

            Usuario usu = usuarioRepository.findByCorreo(mail).orElseGet(() -> {
                Usuario u = new Usuario(nom, ape, mail, passwordEncoder.encode("123456"), rolAlumno);
                u.setDni(dniVal);
                u.setEmailValidado(true);
                Alumno a = new Alumno(u);
                u.setAlumno(a);
                return usuarioRepository.save(u);
            });
            Alumno alu = usu.getAlumno() != null ? usu.getAlumno() : new Alumno(usu);

            Cohorte coh = cohortes.get(i % cohortes.size());
            Inscripcion insc = new Inscripcion(coh, alu);
            LocalDateTime fechaInsc = LocalDateTime.now().minusDays(i % 30 + 1);
            insc.setFecha(fechaInsc);
            insc.setFechaVencimientoAcceso(fechaInsc.plusMonths(6));

            if (i % 5 == 0) {
                insc.setNumeroCertificado("CERT-2026-" + (1000 + i));
                insc.setNombreAlumno(nom + " " + ape);
                insc.setDniAlumno(dniVal);
                insc.setFechaEmisionCertificado(fechaInsc.plusDays(15));
                insc.setCertificadoEnviado(true);
            }
            inscripcionRepository.save(insc);

            float precio = coh.getPrograma().getCurso().getPrecio();
            if (precio > 0) {
                Pago p = new Pago(precio, insc, estAcred);
                p.setFecha(fechaInsc);
                p.setMetodoPago(metTarj);
                p.setNombrePagador(nom + " " + ape);
                p.setDniPagador(dniVal);
                p.setDetalleEstado("accredited");
                p.setNumeroComprobante("COMP-2026-" + (5000 + i));
                p.setComprobanteEnviado(true);
                pagoRepository.save(p);
            }
        }
    }

    private void poblarGlosariosYForos() {
        List<Unidad> unidades = unidadRepository.findAll();
        Alumno alu = alumnoRepository.findAll().stream().findFirst().orElse(null);
        Docente doc = docenteRepository.findAll().stream().findFirst().orElse(null);

        String[][] terminos = {
            {"ALyC", "Agente de Liquidación y Compensación regulado por la CNV."},
            {"TIR", "Tasa Interna de Retorno anualizada de un flujo de fondos."},
            {"CEDEAR", "Certificado de Depósito Argentino representativo de acciones del exterior."},
            {"Duration", "Medida de sensibilidad del precio de un bono ante cambios en la tasa de interés."},
            {"Convexidad", "Curvatura de la relación precio-rendimiento de un bono."},
            {"WACC", "Costo Promedio Ponderado de Capital para descuento de flujos."},
            {"Call Option", "Contrato que otorga el derecho a comprar un activo a un precio fijado."},
            {"Put Option", "Contrato que otorga el derecho a vender un activo a un precio fijado."},
            {"Markowitz", "Modelo de optimización media-varianza para selección de carteras eficientes."},
            {"Staking", "Bloqueo de criptoactivos en una red PoS para obtener recompensas y validar bloques."}
        };

        for (int i = 0; i < unidades.size(); i++) {
            Unidad u = unidades.get(i);
            String[] t = terminos[i % terminos.length];
            terminoGlosarioRepository.save(new TerminoGlosario(t[0] + " (Módulo " + (i+1) + ")", t[1], u));

            if (alu != null && doc != null) {
                ConsultaForo cf = consultaForoRepository.save(new ConsultaForo("Consulta académica sobre la aplicación práctica de " + t[0] + " en la unidad " + u.getTitulo(), alu, u));
                respuestaForoRepository.save(new RespuestaForo("Estimado alumno, " + t[1] + " Se recomienda revisar el marco normativo de CNV en el material complementario.", cf, doc));
            }
        }
    }

    private void poblarClasesVivoYClonIA() {
        List<Cohorte> cohortes = cohorteRepository.findAll();
        Docente doc = docenteRepository.findAll().stream().findFirst().orElse(null);
        EstadoClaseEnVivo estProg = estadoClaseEnVivoRepository.findAll().stream().filter(e -> "Programada".equalsIgnoreCase(e.getNombre())).findFirst().orElse(null);
        EstadoClaseEnVivo estVivo = estadoClaseEnVivoRepository.findAll().stream().filter(e -> "En vivo".equalsIgnoreCase(e.getNombre())).findFirst().orElse(null);
        EstadoClaseEnVivo estFin = estadoClaseEnVivoRepository.findAll().stream().filter(e -> "Finalizada".equalsIgnoreCase(e.getNombre())).findFirst().orElse(null);
        EstadoClaseClonIA estGen = estadoClaseClonIARepository.findAll().stream().filter(e -> "Generada".equalsIgnoreCase(e.getNombre())).findFirst().orElse(null);

        if (!cohortes.isEmpty()) {
            Cohorte coh1 = cohortes.get(0);
            // 1. Clase En Vivo activa (<= 50 chars)
            ClaseEnVivo cvVivo = new ClaseEnVivo(
                "Taller Vivo: Resolución Prácticos Renta Fija",
                LocalDateTime.now().withHour(19).withMinute(0),
                90,
                "rtmp://stream.idoneos.online/live",
                "live_stream_k1001",
                doc,
                (estVivo != null ? estVivo : estProg),
                coh1
            );
            claseEnVivoRepository.save(cvVivo);

            // 2. Clase Programada próxima (<= 50 chars)
            ClaseEnVivo cvProg = new ClaseEnVivo(
                "Taller Vivo: Análisis con TradingView",
                LocalDateTime.now().plusDays(4).withHour(19).withMinute(0),
                60,
                "rtmp://stream.idoneos.online/live",
                "live_stream_k1002",
                doc,
                estProg,
                coh1
            );
            claseEnVivoRepository.save(cvProg);

            // 3. Clase Finalizada con grabación (<= 50 chars)
            ClaseEnVivo cvFin = new ClaseEnVivo(
                "Clase Inaugural: Marco Legal CNV y Ética",
                LocalDateTime.now().minusDays(7).withHour(19).withMinute(0),
                90,
                "rtmp://stream.idoneos.online/live",
                "live_stream_k1003",
                doc,
                (estFin != null ? estFin : estProg),
                coh1
            );
            claseEnVivoRepository.save(cvFin);

            // Otras cohortes (<= 50 chars)
            for (int i = 1; i < cohortes.size(); i++) {
                Cohorte coh = cohortes.get(i);
                ClaseEnVivo c = new ClaseEnVivo("Repaso Sincrónico Cohorte #" + (i+1), LocalDateTime.now().plusDays(i * 3 + 2).withHour(18).withMinute(30), 60, "rtmp://stream.idoneos.online/live", "live_stream_k" + (1004 + i), doc, estProg, coh);
                claseEnVivoRepository.save(c);
            }
        }

        if (doc != null && estGen != null) {
            claseClonIARepository.save(new ClaseClonIA("Estrategia de Bonos con Avatar IA", "Bienvenidos a la clase sintetizada sobre curvas soberanas...", doc, estGen));
            claseClonIARepository.save(new ClaseClonIA("Valuación de Empresas con Clon IA", "En esta microclase analizaremos múltiplos EV/EBITDA...", doc, estGen));
        }
    }

    private void poblarAuditoriaYReportes() {
        Usuario admin = usuarioRepository.findByCorreo("admin@idoneos.online").orElse(null);
        TipoAccionAuditoria accionCrear = tipoAccionAuditoriaRepository.findAll().stream().findFirst().orElse(null);
        TipoReporte trAlumnos = tipoReporteRepository.findAll().stream().findFirst().orElse(null);
        Curso curso1 = cursoRepository.findAll().stream().findFirst().orElse(null);

        if (admin != null && accionCrear != null) {
            for (int i = 1; i <= 50; i++) {
                auditoriaRepository.save(new Auditoria("EntidadSeguridad_" + (i % 5), i, admin, accionCrear));
            }
        }

        if (admin != null && admin.getAdministrador() != null && trAlumnos != null && curso1 != null) {
            Reporte rep = new Reporte(trAlumnos, admin.getAdministrador(), curso1);
            rep.setFechaGeneracion(LocalDateTime.now().minusDays(2));
            reporteRepository.save(rep);
        }
    }
}
