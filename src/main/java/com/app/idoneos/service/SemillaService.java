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
    @Autowired private CategoriaRepository categoriaRepository;
    @Autowired private CursoRepository cursoRepository;
    @Autowired private UnidadRepository unidadRepository;
    @Autowired private MaterialRepository materialRepository;

    @Autowired private TipoMaterialRepository tipoMaterialRepository;
    @Autowired private ModalidadRepository modalidadRepository;
    @Autowired private EstadoClaseEnVivoRepository estadoClaseEnVivoRepository;
    @Autowired private EstadoClaseClonIARepository estadoClaseClonIARepository;
    @Autowired private EstadoPagoRepository estadoPagoRepository;
    @Autowired private MetodoPagoRepository metodoPagoRepository;
    @Autowired private TipoAccionAuditoriaRepository tipoAccionAuditoriaRepository;
    @Autowired private TipoReporteRepository tipoReporteRepository;
    @Autowired private ConfiguracionRepository configuracionRepository;
    @Autowired private DocenteRepository docenteRepository;
    @Autowired private AlumnoRepository alumnoRepository;
    @Autowired private AdministradorRepository administradorRepository;
    @Autowired private DictadoDocenteRepository dictadoDocenteRepository;
    @Autowired private ProgramaRepository programaRepository;
    @Autowired private DictadoRepository dictadoRepository;
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

        // Catálogos de sistema
        TipoMaterial tmGrabacion = tipoMaterialRepository.save(new TipoMaterial("Grabación"));
        TipoMaterial tmBibliografia = tipoMaterialRepository.save(new TipoMaterial("Bibliografía"));
        TipoMaterial tmPresentacion = tipoMaterialRepository.save(new TipoMaterial("Presentación"));
        TipoMaterial tmResumen = tipoMaterialRepository.save(new TipoMaterial("Resumen"));

        modalidadRepository.save(new Modalidad("En vivo"));
        modalidadRepository.save(new Modalidad("Grabada"));
        Modalidad modClonIA = modalidadRepository.save(new Modalidad("Clon IA"));

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

        // Configuraciones base (CU-92)
        configuracionRepository.save(new Configuracion("autoevaluacion.intentos_maximos", "3"));
        configuracionRepository.save(new Configuracion("autoevaluacion.tiempo_limite_minutos", "30"));
        configuracionRepository.save(new Configuracion("autoevaluacion.nota_aprobacion", "6.0"));
        configuracionRepository.save(new Configuracion("evaluacion.preguntas_por_intento", "10"));
        configuracionRepository.save(new Configuracion("evaluacion.proporcion_opcion_multiple", "70"));
        configuracionRepository.save(new Configuracion("evaluacion.proporcion_verdadero_falso", "30"));
        configuracionRepository.save(new Configuracion("grabaciones.plazo_disponibilidad_meses", "4"));
        configuracionRepository.save(new Configuracion("grabaciones.aviso_previo_dias", "7"));
        configuracionRepository.save(new Configuracion("sesiones.max_concurrentes", "1"));
        configuracionRepository.save(new Configuracion("foro.tiempo_limite_edicion_minutos", "30"));
        configuracionRepository.save(new Configuracion("ollama.model", "llama3.1"));
        configuracionRepository.save(new Configuracion("ollama.url", "http://localhost:11434/api/generate"));

        // Usuarios base
        Usuario adminUsuario = usuarioRepository.findByCorreoAndBajaFalse(adminEmail).orElseGet(() -> {
            Usuario u = new Usuario("Admin", "Idóneos", adminEmail, passwordEncoder.encode("123456"), RolUsuario.Administrador);
            u.setEmailValidado(true);
            Administrador adm = new Administrador(u);
            u.setAdministrador(adm);
            return usuarioRepository.save(u);
        });

        Usuario usuFausto = usuarioRepository.findByCorreoAndBajaFalse("fausto.spotorno@idoneos.online").orElseGet(() -> {
            Usuario u = new Usuario("Fausto", "Spotorno", "fausto.spotorno@idoneos.online", passwordEncoder.encode("123456"), RolUsuario.Docente);
            u.setEmailValidado(true);
            Docente d = new Docente(u);
            d.setBiografia("Economista UBA, Magíster en Finanzas UTDT.");
            d.setAniosExperiencia(20);
            d.setFechaConsentimientoClon(LocalDateTime.now());
            u.setDocente(d);
            return usuarioRepository.save(u);
        });
        Docente docenteFausto = usuFausto.getDocente();

        Usuario usuSebas = usuarioRepository.findByCorreoAndBajaFalse("sebastian.bordato@idoneos.online").orElseGet(() -> {
            Usuario u = new Usuario("Sebastián", "Bordato", "sebastian.bordato@idoneos.online", passwordEncoder.encode("123456"), RolUsuario.Docente);
            u.setEmailValidado(true);
            Docente d = new Docente(u);
            d.setBiografia("Contador Público UBA. Especialista en planificación fiscal.");
            d.setAniosExperiencia(15);
            d.setFechaConsentimientoClon(LocalDateTime.now());
            u.setDocente(d);
            return usuarioRepository.save(u);
        });
        Docente docenteSebas = usuSebas.getDocente();

        Usuario usuAlumno = usuarioRepository.findByCorreoAndBajaFalse("alumno@correo.com").orElseGet(() -> {
            Usuario u = new Usuario("Juan", "Pérez", "alumno@correo.com", passwordEncoder.encode("123456"), RolUsuario.Alumno);
            u.setEmailValidado(true);
            Alumno al = new Alumno(u);
            u.setAlumno(al);
            return usuarioRepository.save(u);
        });

        // Categorías temáticas
        Categoria catMercado = categoriaRepository.findAll().stream().filter(c -> "Mercado de Capitales".equals(c.getNombre())).findFirst()
                .orElseGet(() -> categoriaRepository.save(new Categoria("Mercado de Capitales", "Cursos sobre bonos, acciones, CEDEARs e instrumentos de inversión.")));
        Categoria catEconomia = categoriaRepository.findAll().stream().filter(c -> "Economía".equals(c.getNombre())).findFirst()
                .orElseGet(() -> categoriaRepository.save(new Categoria("Economía", "Análisis macroeconómico y coyuntura argentina.")));
        Categoria catFinanzas = categoriaRepository.findAll().stream().filter(c -> "Finanzas Corporativas".equals(c.getNombre())).findFirst()
                .orElseGet(() -> categoriaRepository.save(new Categoria("Finanzas Corporativas", "Gestión financiera de empresas y valuación.")));
        Categoria catImpuestos = categoriaRepository.findAll().stream().filter(c -> "Impuestos y Contabilidad".equals(c.getNombre())).findFirst()
                .orElseGet(() -> categoriaRepository.save(new Categoria("Impuestos y Contabilidad", "Legislación tributaria argentina.")));

        // Cursos demo
        Curso curso1 = cursoRepository.findByNombre("Mercado de Capitales Argentino").orElseGet(() -> {
            Curso c = new Curso("Mercado de Capitales Argentino", "Instrumentos financieros en Argentina: Acciones, Bonos, ONs y Opciones.", 150000f, catMercado);
            c.setMesesAcceso(12);
            c.setPublicado(true);
            return cursoRepository.save(c);
        });
        Programa prog1 = programaRepository.findByCurso(curso1).stream().findFirst().orElseGet(() ->
                programaRepository.save(new Programa("Programa Inicial 2026", "Plan de estudios principal del curso", 12, curso1)));
        Dictado dictado1 = dictadoRepository.findByPrograma(prog1).stream().findFirst().orElseGet(() -> {
            Dictado d = dictadoRepository.save(new Dictado(LocalDateTime.now().minusMonths(3), LocalDateTime.now().plusMonths(6), 50, prog1));
            dictadoDocenteRepository.save(new DictadoDocente(d, docenteFausto, false));
            dictadoDocenteRepository.save(new DictadoDocente(d, docenteSebas, true));
            return d;
        });

        Curso curso2 = cursoRepository.findByNombre("Análisis Macroeconómico").orElseGet(() -> {
            Curso c = new Curso("Análisis Macroeconómico", "Herramientas para analizar variables macroeconómicas.", 0f, catEconomia);
            c.setPublicado(true);
            return cursoRepository.save(c);
        });
        Programa prog2 = programaRepository.findByCurso(curso2).stream().findFirst().orElseGet(() ->
                programaRepository.save(new Programa("Programa Economía 2026", "Plan de estudio macroeconomía", 6, curso2)));
        Dictado dictado2 = dictadoRepository.findByPrograma(prog2).stream().findFirst().orElseGet(() -> {
            Dictado d = dictadoRepository.save(new Dictado(LocalDateTime.now().minusMonths(2), LocalDateTime.now().plusMonths(6), 100, prog2));
            dictadoDocenteRepository.save(new DictadoDocente(d, docenteFausto, false));
            return d;
        });

        Curso curso3 = cursoRepository.findByNombre("Planificación Fiscal y Tributaria").orElseGet(() -> {
            Curso c = new Curso("Planificación Fiscal y Tributaria", "Estrategias impositivas para pymes y profesionales.", 220000f, catImpuestos);
            c.setMesesAcceso(6);
            c.setPublicado(true);
            return cursoRepository.save(c);
        });
        Programa prog3 = programaRepository.findByCurso(curso3).stream().findFirst().orElseGet(() ->
                programaRepository.save(new Programa("Programa Fiscal 2026", "Plan de estudios impositivo", 6, curso3)));
        Dictado dictado3 = dictadoRepository.findByPrograma(prog3).stream().findFirst().orElseGet(() -> {
            Dictado d = dictadoRepository.save(new Dictado(LocalDateTime.now().minusMonths(2), LocalDateTime.now().plusMonths(6), 40, prog3));
            dictadoDocenteRepository.save(new DictadoDocente(d, docenteSebas, false));
            return d;
        });

        // Cursos adicionales para rankings y comparativas ricas
        Curso curso4 = cursoRepository.findByNombre("Valuación de Empresas").orElseGet(() -> {
            Curso c = new Curso("Valuación de Empresas", "DCF, múltiplos y análisis de balance.", 180000f, catFinanzas);
            c.setMesesAcceso(12);
            c.setPublicado(true);
            return cursoRepository.save(c);
        });
        Programa prog4 = programaRepository.findByCurso(curso4).stream().findFirst().orElseGet(() ->
                programaRepository.save(new Programa("Programa Valuación 2026", "Plan valuación", 12, curso4)));
        Dictado dictado4 = dictadoRepository.findByPrograma(prog4).stream().findFirst().orElseGet(() -> {
            Dictado d = dictadoRepository.save(new Dictado(LocalDateTime.now().minusMonths(2), LocalDateTime.now().plusMonths(4), 50, prog4));
            dictadoDocenteRepository.save(new DictadoDocente(d, docenteFausto, false));
            return d;
        });

        Curso curso5 = cursoRepository.findByNombre("Operativa Cripto y DeFi").orElseGet(() -> {
            Curso c = new Curso("Operativa Cripto y DeFi", "Trading, DeFi y custodia institucional.", 120000f, catMercado);
            c.setMesesAcceso(6);
            c.setPublicado(true);
            return cursoRepository.save(c);
        });
        Programa prog5 = programaRepository.findByCurso(curso5).stream().findFirst().orElseGet(() ->
                programaRepository.save(new Programa("Programa Cripto 2026", "Plan cripto", 6, curso5)));
        Dictado dictado5 = dictadoRepository.findByPrograma(prog5).stream().findFirst().orElseGet(() -> {
            Dictado d = dictadoRepository.save(new Dictado(LocalDateTime.now().minusMonths(1), LocalDateTime.now().plusMonths(5), 60, prog5));
            dictadoDocenteRepository.save(new DictadoDocente(d, docenteSebas, false));
            return d;
        });

        // Unidades y Materiales del Curso 1
        Unidad u1 = unidadRepository.save(new Unidad("Introducción al Sistema Financiero", "Estructura del mercado argentino, CNV, BYMA.", 1, curso1));
        Unidad u2 = unidadRepository.save(new Unidad("Renta Fija: Bonos y ONs", "Cálculo de TIR, Duration, curvas de rendimientos.", 2, curso1));
        Unidad u3 = unidadRepository.save(new Unidad("Renta Variable y CEDEARs", "Inversión en acciones locales e internacionales.", 3, curso1));

        materialRepository.save(new Material(tmGrabacion, "Clase Grabada - Módulo 1", "videos/u1_clase.mp4", u1));
        materialRepository.save(new Material(tmBibliografia, "Ley de Mercado de Capitales 26.831", "docs/ley_26831.pdf", u1));
        materialRepository.save(new Material(tmPresentacion, "Diapositivas Unidad 1", "slides/u1_slides.pdf", u1));

        // Descuentos de prueba
        Descuento descLanzamiento = new Descuento();
        descLanzamiento.setNombre("Lanzamiento 2026");
        descLanzamiento.setPorcentaje(15.0f);
        descLanzamiento.setVigenciaDesde(LocalDateTime.now().minusMonths(2));
        descLanzamiento.setVigenciaHasta(LocalDateTime.now().plusMonths(6));
        descLanzamiento.setCantidadLimite(100);
        descLanzamiento.setCantidadUsada(12);
        descLanzamiento.setFechaCreacion(LocalDateTime.now().minusMonths(2));
        descuentoRepository.save(descLanzamiento);

        Descuento descComunidad = new Descuento();
        descComunidad.setNombre("Comunidad Idóneos");
        descComunidad.setPorcentaje(20.0f);
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

        // Generar múltiples alumnos de prueba con inscripciones y pagos distribuidos en los últimos 30 días
        String[][] alumnosDatos = {
                {"Carlos", "Gómez", "carlos.gomez@test.com", "28111222"},
                {"María", "López", "maria.lopez@test.com", "30222333"},
                {"Lucía", "Fernández", "lucia.fernandez@test.com", "32333444"},
                {"Martín", "Rodríguez", "martin.rodriguez@test.com", "34444555"},
                {"Sofía", "Martínez", "sofia.martinez@test.com", "36555666"},
                {"Esteban", "Paz", "esteban.paz@test.com", "29666777"},
                {"Camila", "Díaz", "camila.diaz@test.com", "31777888"},
                {"Federico", "Álvarez", "federico.alvarez@test.com", "33888999"},
                {"Valeria", "Ríos", "valeria.rios@test.com", "35999000"},
                {"Gonzalo", "Benítez", "gonzalo.benitez@test.com", "37000111"},
                {"Florencia", "Morales", "florencia.morales@test.com", "28123456"},
                {"Matías", "Torres", "matias.torres@test.com", "30234567"},
                {"Agustina", "Herrera", "agustina.herrera@test.com", "32345678"},
                {"Ignacio", "Castro", "ignacio.castro@test.com", "34456789"},
                {"Paula", "Ortiz", "paula.ortiz@test.com", "36567890"}
        };

        Dictado[] dictados = {dictado1, dictado1, dictado1, dictado2, dictado3, dictado4, dictado4, dictado5, dictado1, dictado1, dictado3, dictado4, dictado5, dictado1, dictado2};
        int[] diasAtras = {25, 22, 20, 18, 15, 14, 12, 10, 8, 6, 5, 3, 2, 1, 0};

        for (int i = 0; i < alumnosDatos.length; i++) {
            String nom = alumnosDatos[i][0];
            String ape = alumnosDatos[i][1];
            String mail = alumnosDatos[i][2];
            String dniVal = alumnosDatos[i][3];

            Usuario usu = new Usuario(nom, ape, mail, passwordEncoder.encode("123456"), RolUsuario.Alumno);
            usu.setDni(dniVal);
            usu.setEmailValidado(true);
            Alumno alu = new Alumno(usu);
            usu.setAlumno(alu);
            usuarioRepository.save(usu);

            Dictado d = dictados[i % dictados.length];
            Inscripcion insc = new Inscripcion(alu, d);
            LocalDateTime fechaInsc = LocalDateTime.now().minusDays(diasAtras[i]);
            insc.setFecha(fechaInsc);
            insc.setFechaVencimientoAcceso(fechaInsc.plusMonths(6));

            // Variabilidad de estados para probar los 3 estados del gráfico de CU-87:
            if (i == 0 || i == 5) {
                // Completada con certificado emitido
                insc.setNumeroCertificado("CERT-2026-" + (1000 + i));
                insc.setFechaEmisionCertificado(fechaInsc.plusDays(10));
                insc.setCertificadoEnviado(true);
            } else if (i == 1) {
                // Dada de baja
                insc.setBaja(true);
            } else {
                // Vigente normal
                insc.setBaja(false);
            }

            // Aplicar descuento a algunas inscripciones
            if (i % 3 == 0) {
                insc.setDescuento(descLanzamiento);
            } else if (i % 4 == 0) {
                insc.setDescuento(descComunidad);
            }

            inscripcionRepository.save(insc);

            // Generar pago acreditado si el curso tiene precio > 0
            float precioBase = d.getPrograma().getCurso().getPrecio();
            if (precioBase > 0) {
                float montoFinal = precioBase;
                if (insc.getDescuento() != null) {
                    montoFinal = precioBase * (1.0f - (insc.getDescuento().getPorcentaje() / 100.0f));
                }
                Pago p = new Pago(montoFinal, insc, estadoAcreditado);
                p.setFecha(fechaInsc);
                p.setMetodoPago(metodoTarjeta);
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

        // Registrar un par de reportes previos de ejemplo en el historial
        Administrador adminActual = administradorRepository.findById(adminUsuario.getId()).orElseGet(adminUsuario::getAdministrador);
        TipoReporte trAlumnos = tipoReporteRepository.findByNombre("Alumnos inscriptos").orElse(null);
        TipoReporte trIngresos = tipoReporteRepository.findByNombre("Ingresos").orElse(null);
        if (trAlumnos != null && adminActual != null) {
            Reporte r1 = new Reporte(trAlumnos, adminActual);
            r1.setFechaGeneracion(LocalDateTime.now().minusDays(5));
            reporteRepository.save(r1);
        }
        if (trIngresos != null && adminActual != null) {
            Reporte r2 = new Reporte(trIngresos, adminActual);
            r2.setFechaGeneracion(LocalDateTime.now().minusDays(2));
            reporteRepository.save(r2);
        }

        System.out.println("✅ Idóneos Online: Datos iniciales y datos de prueba para reportes insertados correctamente.");
    }
}
