package com.app.idoneos.service;

import com.app.idoneos.model.*;
import com.app.idoneos.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Servicio para el semillado de datos iniciales del sistema (CU-93: Semillar datos iniciales).
 * Puebla catálogos, administradores, docentes, cursos, unidades y parámetros operativos clave-valor.
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

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * CU-93 — Semillado automático de la base de datos `idoneos.online`.
     */
    @Transactional
    public void insertarSemilla() {
        String adminEmail = "admin@idoneos.online";
        if (usuarioRepository.findByCorreoAndBajaFalse(adminEmail).isPresent()) {
            if (inscripcionRepository.count() > 0) {
                System.out.println("Idóneos Online: La semilla y datos de prueba ya se encuentran insertados.");
                return;
            }
        }

        // ---------------------------------------------------------------
        // Roles del sistema
        // ---------------------------------------------------------------
        Rol rolAdmin = rolRepository.findByNombre("Administrador")
                .orElseGet(() -> rolRepository.save(new Rol("Administrador")));
        Rol rolDocente = rolRepository.findByNombre("Docente")
                .orElseGet(() -> rolRepository.save(new Rol("Docente")));
        Rol rolAlumno = rolRepository.findByNombre("Alumno")
                .orElseGet(() -> rolRepository.save(new Rol("Alumno")));

        // ---------------------------------------------------------------
        // Catálogos de sistema
        // ---------------------------------------------------------------
        TipoMaterial tmGrabacion = tipoMaterialRepository.save(new TipoMaterial("Grabación"));
        TipoMaterial tmBibliografia = tipoMaterialRepository.save(new TipoMaterial("Bibliografía"));
        TipoMaterial tmPresentacion = tipoMaterialRepository.save(new TipoMaterial("Presentación"));
        TipoMaterial tmResumen = tipoMaterialRepository.save(new TipoMaterial("Resumen"));

        modalidadRepository.save(new Modalidad("En vivo"));
        modalidadRepository.save(new Modalidad("Grabada"));
        modalidadRepository.save(new Modalidad("Clon IA"));

        estadoClaseEnVivoRepository.save(new EstadoClaseEnVivo("Programada"));
        estadoClaseEnVivoRepository.save(new EstadoClaseEnVivo("En vivo"));
        estadoClaseEnVivoRepository.save(new EstadoClaseEnVivo("Finalizada"));

        estadoClaseClonIARepository.save(new EstadoClaseClonIA("Pendiente"));
        estadoClaseClonIARepository.save(new EstadoClaseClonIA("Generada"));
        estadoClaseClonIARepository.save(new EstadoClaseClonIA("Error"));

        estadoPagoRepository.save(new EstadoPago("Pendiente"));
        estadoPagoRepository.save(new EstadoPago("Acreditado"));
        estadoPagoRepository.save(new EstadoPago("Rechazado"));

        metodoPagoRepository.save(new MetodoPago("Tarjeta de crédito"));
        metodoPagoRepository.save(new MetodoPago("Tarjeta de débito"));
        metodoPagoRepository.save(new MetodoPago("Saldo de cuenta"));

        tipoAccionAuditoriaRepository.save(new TipoAccionAuditoria("Crear"));
        tipoAccionAuditoriaRepository.save(new TipoAccionAuditoria("Modificar"));
        tipoAccionAuditoriaRepository.save(new TipoAccionAuditoria("Eliminar"));
        tipoAccionAuditoriaRepository.save(new TipoAccionAuditoria("Consultar"));

        tipoReporteRepository.save(new TipoReporte("Alumnos inscriptos"));
        tipoReporteRepository.save(new TipoReporte("Ingresos"));

        // Niveles de dificultad
        Nivel nivelBasico = nivelRepository.findByNombre("Básico")
                .orElseGet(() -> nivelRepository.save(new Nivel("Básico")));
        Nivel nivelIntermedio = nivelRepository.findByNombre("Intermedio")
                .orElseGet(() -> nivelRepository.save(new Nivel("Intermedio")));
        Nivel nivelAvanzado = nivelRepository.findByNombre("Avanzado")
                .orElseGet(() -> nivelRepository.save(new Nivel("Avanzado")));

        // ---------------------------------------------------------------
        // Usuarios base
        // ---------------------------------------------------------------
        Usuario adminUsuario = usuarioRepository.findByCorreoAndBajaFalse(adminEmail).orElseGet(() -> {
            Usuario u = new Usuario("Admin", "Idóneos", adminEmail, passwordEncoder.encode("123456"), rolAdmin);
            u.setEmailValidado(true);
            Administrador adm = new Administrador(u);
            u.setAdministrador(adm);
            return usuarioRepository.save(u);
        });

        // Configuraciones base (CU-92)
        Administrador adminActual = administradorRepository.findByUsuario(adminUsuario)
                .orElse(adminUsuario.getAdministrador());
        if (adminActual != null && configuracionRepository.count() == 0) {
            configuracionRepository.save(new Configuracion("autoevaluacion.intentos_maximos", "3", adminActual));
            configuracionRepository.save(new Configuracion("autoevaluacion.tiempo_limite_minutos", "30", adminActual));
            configuracionRepository.save(new Configuracion("autoevaluacion.nota_aprobacion", "6.0", adminActual));
            configuracionRepository.save(new Configuracion("evaluacion.preguntas_por_intento", "10", adminActual));
            configuracionRepository.save(new Configuracion("evaluacion.proporcion_opcion_multiple", "70", adminActual));
            configuracionRepository.save(new Configuracion("evaluacion.proporcion_verdadero_falso", "30", adminActual));
            configuracionRepository.save(new Configuracion("grabaciones.plazo_disponibilidad_meses", "4", adminActual));
            configuracionRepository.save(new Configuracion("grabaciones.aviso_previo_dias", "7", adminActual));
            configuracionRepository.save(new Configuracion("sesiones.max_concurrentes", "1", adminActual));
            configuracionRepository.save(new Configuracion("foro.tiempo_limite_edicion_minutos", "30", adminActual));
            configuracionRepository.save(new Configuracion("ollama.model", "llama3.1", adminActual));
            configuracionRepository.save(new Configuracion("ollama.url", "http://localhost:11434/api/generate", adminActual));
        }

        Usuario usuFausto = usuarioRepository.findByCorreoAndBajaFalse("fausto.spotorno@idoneos.online").orElseGet(() -> {
            Usuario u = new Usuario("Fausto", "Spotorno", "fausto.spotorno@idoneos.online", passwordEncoder.encode("123456"), rolDocente);
            u.setEmailValidado(true);
            Docente d = new Docente(u, 20);
            d.setBiografia("Economista UBA, Magíster en Finanzas UTDT.");
            d.setFechaConsentimientoClon(LocalDateTime.now());
            d.setHabilitado(true);
            u.setDocente(d);
            return usuarioRepository.save(u);
        });
        Docente docenteFausto = usuFausto.getDocente();

        Usuario usuSebas = usuarioRepository.findByCorreoAndBajaFalse("sebastian.bordato@idoneos.online").orElseGet(() -> {
            Usuario u = new Usuario("Sebastián", "Bordato", "sebastian.bordato@idoneos.online", passwordEncoder.encode("123456"), rolDocente);
            u.setEmailValidado(true);
            Docente d = new Docente(u, 15);
            d.setBiografia("Contador Público UBA. Especialista en planificación fiscal.");
            d.setFechaConsentimientoClon(LocalDateTime.now());
            d.setHabilitado(true);
            u.setDocente(d);
            return usuarioRepository.save(u);
        });
        Docente docenteSebas = usuSebas.getDocente();

        Usuario usuAlumno = usuarioRepository.findByCorreoAndBajaFalse("alumno@correo.com").orElseGet(() -> {
            Usuario u = new Usuario("Juan", "Pérez", "alumno@correo.com", passwordEncoder.encode("123456"), rolAlumno);
            u.setEmailValidado(true);
            Alumno al = new Alumno(u);
            u.setAlumno(al);
            return usuarioRepository.save(u);
        });

        // ---------------------------------------------------------------
        // Categorías temáticas
        // ---------------------------------------------------------------
        Categoria catMercado = categoriaRepository.findAll().stream()
                .filter(c -> "Mercado de Capitales".equals(c.getNombre())).findFirst()
                .orElseGet(() -> categoriaRepository.save(new Categoria("Mercado de Capitales", "Cursos sobre bonos, acciones, CEDEARs e instrumentos de inversión.")));
        Categoria catEconomia = categoriaRepository.findAll().stream()
                .filter(c -> "Economía".equals(c.getNombre())).findFirst()
                .orElseGet(() -> categoriaRepository.save(new Categoria("Economía", "Análisis macroeconómico y coyuntura argentina.")));
        Categoria catFinanzas = categoriaRepository.findAll().stream()
                .filter(c -> "Finanzas Corporativas".equals(c.getNombre())).findFirst()
                .orElseGet(() -> categoriaRepository.save(new Categoria("Finanzas Corporativas", "Gestión financiera de empresas y valuación.")));
        Categoria catImpuestos = categoriaRepository.findAll().stream()
                .filter(c -> "Impuestos y Contabilidad".equals(c.getNombre())).findFirst()
                .orElseGet(() -> categoriaRepository.save(new Categoria("Impuestos y Contabilidad", "Legislación tributaria argentina.")));

        // ---------------------------------------------------------------
        // Cursos + Programas + Cohortes
        // ---------------------------------------------------------------
        Curso curso1 = cursoRepository.findByNombre("Mercado de Capitales Argentino").orElseGet(() -> {
            Curso c = new Curso("Mercado de Capitales Argentino",
                    "Instrumentos financieros en Argentina: Acciones, Bonos, ONs y Opciones.",
                    150000f, catMercado, nivelIntermedio, docenteFausto);
            c.setEmiteCertificado(true);
            return cursoRepository.save(c);
        });
        Programa prog1 = programaRepository.findByCurso(curso1).stream().findFirst().orElseGet(() ->
                programaRepository.save(new Programa("Programa Inicial 2026", "Plan de estudios principal del curso",
                        "Comprender el mercado financiero argentino y sus instrumentos.",
                        "Ley 26.831; CNV Normativa.", curso1)));
        Cohorte cohorte1 = cohorteRepository.findByPrograma(prog1).stream().findFirst().orElseGet(() ->
                cohorteRepository.save(new Cohorte(
                        LocalDateTime.now().minusMonths(3),
                        LocalDateTime.now().minusMonths(2),
                        24, prog1)));

        Curso curso2 = cursoRepository.findByNombre("Análisis Macroeconómico").orElseGet(() -> {
            Curso c = new Curso("Análisis Macroeconómico",
                    "Herramientas para analizar variables macroeconómicas.",
                    0f, catEconomia, nivelBasico, docenteFausto);
            return cursoRepository.save(c);
        });
        Programa prog2 = programaRepository.findByCurso(curso2).stream().findFirst().orElseGet(() ->
                programaRepository.save(new Programa("Programa Economía 2026", "Plan de estudio macroeconomía",
                        "Dominar los indicadores macroeconómicos clave.", "", curso2)));
        Cohorte cohorte2 = cohorteRepository.findByPrograma(prog2).stream().findFirst().orElseGet(() ->
                cohorteRepository.save(new Cohorte(
                        LocalDateTime.now().minusMonths(2),
                        LocalDateTime.now().minusMonths(1),
                        24, prog2)));

        Curso curso3 = cursoRepository.findByNombre("Planificación Fiscal y Tributaria").orElseGet(() -> {
            Curso c = new Curso("Planificación Fiscal y Tributaria",
                    "Estrategias impositivas para pymes y profesionales.",
                    220000f, catImpuestos, nivelAvanzado, docenteSebas);
            c.setEmiteCertificado(true);
            return cursoRepository.save(c);
        });
        Programa prog3 = programaRepository.findByCurso(curso3).stream().findFirst().orElseGet(() ->
                programaRepository.save(new Programa("Programa Fiscal 2026", "Plan de estudios impositivo",
                        "Planificación tributaria eficiente.", "", curso3)));
        Cohorte cohorte3 = cohorteRepository.findByPrograma(prog3).stream().findFirst().orElseGet(() ->
                cohorteRepository.save(new Cohorte(
                        LocalDateTime.now().minusMonths(2),
                        LocalDateTime.now().minusMonths(1),
                        16, prog3)));

        Curso curso4 = cursoRepository.findByNombre("Valuación de Empresas").orElseGet(() -> {
            Curso c = new Curso("Valuación de Empresas",
                    "DCF, múltiplos y análisis de balance.",
                    180000f, catFinanzas, nivelAvanzado, docenteFausto);
            c.setEmiteCertificado(true);
            return cursoRepository.save(c);
        });
        Programa prog4 = programaRepository.findByCurso(curso4).stream().findFirst().orElseGet(() ->
                programaRepository.save(new Programa("Programa Valuación 2026", "Plan valuación",
                        "Valuar empresas con métodos profesionales.", "", curso4)));
        Cohorte cohorte4 = cohorteRepository.findByPrograma(prog4).stream().findFirst().orElseGet(() ->
                cohorteRepository.save(new Cohorte(
                        LocalDateTime.now().minusMonths(2),
                        LocalDateTime.now().minusMonths(1),
                        16, prog4)));

        Curso curso5 = cursoRepository.findByNombre("Operativa Cripto y DeFi").orElseGet(() -> {
            Curso c = new Curso("Operativa Cripto y DeFi",
                    "Trading, DeFi y custodia institucional.",
                    120000f, catMercado, nivelIntermedio, docenteSebas);
            return cursoRepository.save(c);
        });
        Programa prog5 = programaRepository.findByCurso(curso5).stream().findFirst().orElseGet(() ->
                programaRepository.save(new Programa("Programa Cripto 2026", "Plan cripto",
                        "Operar con criptomonedas de forma segura.", "", curso5)));
        Cohorte cohorte5 = cohorteRepository.findByPrograma(prog5).stream().findFirst().orElseGet(() ->
                cohorteRepository.save(new Cohorte(
                        LocalDateTime.now().minusMonths(1),
                        LocalDateTime.now().plusWeeks(2),
                        20, prog5)));

        // ---------------------------------------------------------------
        // Unidades y Cronograma del Curso 1
        // ---------------------------------------------------------------
        Unidad u1 = unidadRepository.save(new Unidad("Introducción al Sistema Financiero",
                "Estructura del mercado argentino, CNV, BYMA.", "Contenido de la unidad 1."));
        Unidad u2 = unidadRepository.save(new Unidad("Renta Fija: Bonos y ONs",
                "Cálculo de TIR, Duration, curvas de rendimientos.", "Contenido de la unidad 2."));
        Unidad u3 = unidadRepository.save(new Unidad("Renta Variable y CEDEARs",
                "Inversión en acciones locales e internacionales.", "Contenido de la unidad 3."));

        cronogramaRepository.save(new Cronograma(1, 4, prog1, u1));
        cronogramaRepository.save(new Cronograma(2, 4, prog1, u2));
        cronogramaRepository.save(new Cronograma(3, 4, prog1, u3));

        // Materiales de Unidad 1
        materialRepository.save(new Material("Clase Grabada - Módulo 1", docenteFausto, tmGrabacion, u1));
        materialRepository.save(new Material("Ley de Mercado de Capitales 26.831", docenteFausto, tmBibliografia, u1));
        materialRepository.save(new Material("Diapositivas Unidad 1", docenteFausto, tmPresentacion, u1));

        // ---------------------------------------------------------------
        // Descuentos de prueba
        // ---------------------------------------------------------------
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

        // Estado de pago Acreditado y Método de pago
        EstadoPago estadoAcreditado = estadoPagoRepository.findByNombre("Acreditado")
                .orElseGet(() -> estadoPagoRepository.save(new EstadoPago("Acreditado")));
        MetodoPago metodoTarjeta = metodoPagoRepository.findByNombre("Tarjeta de crédito")
                .orElseGet(() -> metodoPagoRepository.save(new MetodoPago("Tarjeta de crédito")));

        // ---------------------------------------------------------------
        // Alumnos de prueba, inscripciones y pagos
        // ---------------------------------------------------------------
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
                {"Gonzalo",   "Benítez",   "gonzalo.benitez@test.com",    "37000111"},
                {"Florencia", "Morales",   "florencia.morales@test.com",  "28123456"},
                {"Matías",    "Torres",    "matias.torres@test.com",      "30234567"},
                {"Agustina",  "Herrera",   "agustina.herrera@test.com",   "32345678"},
                {"Ignacio",   "Castro",    "ignacio.castro@test.com",     "34456789"},
                {"Paula",     "Ortiz",     "paula.ortiz@test.com",        "36567890"}
        };

        Cohorte[] cohortes = {cohorte1, cohorte1, cohorte1, cohorte2, cohorte3,
                              cohorte4, cohorte4, cohorte5, cohorte1, cohorte1,
                              cohorte3, cohorte4, cohorte5, cohorte1, cohorte2};
        int[] diasAtras = {25, 22, 20, 18, 15, 14, 12, 10, 8, 6, 5, 3, 2, 1, 0};

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

            if (i == 0 || i == 5) {
                // Completada con certificado
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
                Descuento descAplicado = (i % 3 == 0) ? descLanzamiento : (i % 4 == 0) ? descComunidad : null;
                float montoFinal = precioBase;
                if (descAplicado != null) {
                    montoFinal = precioBase * (1.0f - (descAplicado.getPorcentaje() / 100.0f));
                }
                Pago p = new Pago(montoFinal, insc, estadoAcreditado);
                p.setFecha(fechaInsc);
                p.setMetodoPago(metodoTarjeta);
                p.setDescuento(descAplicado);
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

        // ---------------------------------------------------------------
        // Reportes de ejemplo
        // ---------------------------------------------------------------
        if (adminActual != null) {
            TipoReporte trAlumnos = tipoReporteRepository.findByNombre("Alumnos inscriptos").orElse(null);
            TipoReporte trIngresos = tipoReporteRepository.findByNombre("Ingresos").orElse(null);
            if (trAlumnos != null) {
                Reporte r1 = new Reporte(trAlumnos, adminActual, curso1);
                r1.setFechaGeneracion(LocalDateTime.now().minusDays(5));
                reporteRepository.save(r1);
            }
            if (trIngresos != null) {
                Reporte r2 = new Reporte(trIngresos, adminActual, curso1);
                r2.setFechaGeneracion(LocalDateTime.now().minusDays(2));
                reporteRepository.save(r2);
            }
        }

        System.out.println("✅ Idóneos Online: Datos iniciales y datos de prueba para reportes insertados correctamente.");
    }
}
