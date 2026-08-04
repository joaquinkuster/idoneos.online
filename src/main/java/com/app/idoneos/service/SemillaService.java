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
 * Servicio para insertar datos iniciales (Semilla) de Idóneos Online.
 * Crea catálogos, usuarios base y cursos de demostración.
 */
@Service
public class SemillaService {

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private CategoriaRepository categoriaRepository;
    @Autowired private CursoRepository cursoRepository;
    @Autowired private UnidadRepository unidadRepository;
    @Autowired private MaterialRepository materialRepository;

    // Nuevos repositorios de catálogos
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
    @Autowired private DocenteCursoRepository docenteCursoRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Transactional
    public void insertarSemilla() {
        String adminEmail = "admin@idoneos.online";
        if (usuarioRepository.findByCorreoAndBajaFalse(adminEmail).isPresent()) {
            System.out.println("Idóneos Online: La semilla ya se encuentra insertada.");
            return;
        }

        // ── 1. Catálogos de sistema ────────────────────────────────────────────

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

        tipoAccionAuditoriaRepository.save(new TipoAccionAuditoria("Crear"));
        tipoAccionAuditoriaRepository.save(new TipoAccionAuditoria("Modificar"));
        tipoAccionAuditoriaRepository.save(new TipoAccionAuditoria("Eliminar"));
        tipoAccionAuditoriaRepository.save(new TipoAccionAuditoria("Consultar"));

        tipoReporteRepository.save(new TipoReporte("Alumnos inscriptos"));
        tipoReporteRepository.save(new TipoReporte("Tráfico"));
        tipoReporteRepository.save(new TipoReporte("Ingresos"));

        // Configuración inicial del sistema (claves-valor según Módulo de Configuración)
        configuracionRepository.save(new Configuracion("plataforma.nombre", "Idóneos Online"));
        configuracionRepository.save(new Configuracion("evaluacion.umbral_aprobacion", "60"));
        configuracionRepository.save(new Configuracion("evaluacion.preguntas_por_intento", "10"));
        configuracionRepository.save(new Configuracion("evaluacion.proporcion_opcion_multiple", "70"));   // % de preguntas V/M
        configuracionRepository.save(new Configuracion("evaluacion.proporcion_verdadero_falso", "30"));    // % de preguntas V/F
        configuracionRepository.save(new Configuracion("grabaciones.plazo_disponibilidad_meses", "4"));
        configuracionRepository.save(new Configuracion("grabaciones.aviso_previo_dias", "7"));
        configuracionRepository.save(new Configuracion("sesiones.max_concurrentes", "1"));
        configuracionRepository.save(new Configuracion("foro.tiempo_limite_edicion_minutos", "30"));
        // IA local: Ollama corre en localhost:11434 — no requiere API key externa
        configuracionRepository.save(new Configuracion("ollama.model", "llama3.1"));
        configuracionRepository.save(new Configuracion("ollama.url", "http://localhost:11434/api/generate"));

        // ── 2. Usuarios base ───────────────────────────────────────────────────

        Usuario adminUsuario = new Usuario("Admin", "Idóneos", adminEmail, passwordEncoder.encode("123456"), RolUsuario.Administrador);
        Administrador adminObj = new Administrador(adminUsuario);
        adminUsuario.setAdministrador(adminObj);
        usuarioRepository.save(adminUsuario);

        Usuario usuFausto = new Usuario("Fausto", "Spotorno", "fausto.spotorno@idoneos.online", passwordEncoder.encode("123456"), RolUsuario.Docente);
        Docente docenteFausto = new Docente(usuFausto);
        docenteFausto.setBiografia("Economista UBA, Magíster en Finanzas UTDT. Ex-Director de Relevamiento Económico de Orlando J. Ferreres & Asociados.");
        docenteFausto.setAniosExperiencia(20);
        docenteFausto.setFechaConsentimientoClon(LocalDateTime.now()); // habilitado para Clon IA
        usuFausto.setDocente(docenteFausto);
        usuarioRepository.save(usuFausto);

        Usuario usuSebas = new Usuario("Sebastián", "Bordato", "sebastian.bordato@idoneos.online", passwordEncoder.encode("123456"), RolUsuario.Docente);
        Docente docenteSebas = new Docente(usuSebas);
        docenteSebas.setBiografia("Contador Público UBA. Especialista en planificación fiscal y mercados financieros.");
        docenteSebas.setAniosExperiencia(15);
        docenteSebas.setFechaConsentimientoClon(LocalDateTime.now());
        usuSebas.setDocente(docenteSebas);
        usuarioRepository.save(usuSebas);

        Usuario usuAlumno = new Usuario("Juan", "Pérez", "alumno@correo.com", passwordEncoder.encode("123456"), RolUsuario.Alumno);
        Alumno alumnoObj = new Alumno(usuAlumno);
        usuAlumno.setAlumno(alumnoObj);
        usuarioRepository.save(usuAlumno);

        // ── 3. Categorías temáticas ────────────────────────────────────────────

        Categoria catMercado = categoriaRepository.save(new Categoria("Mercado de Capitales", "Cursos sobre bonos, acciones, CEDEARs, instrumentos de inversión y normativa CNV."));
        Categoria catEconomia = categoriaRepository.save(new Categoria("Economía", "Análisis macroeconómico, coyuntura argentina, política monetaria y fiscal."));
        Categoria catFinanzas = categoriaRepository.save(new Categoria("Finanzas Corporativas", "Gestión financiera de empresas, evaluación de proyectos y valuación."));
        Categoria catImpuestos = categoriaRepository.save(new Categoria("Impuestos y Contabilidad", "Legislación tributaria argentina, planificación fiscal y estados contables."));

        // ── 4. Cursos demo ─────────────────────────────────────────────────────

        Curso curso1 = new Curso(
                "Introducción al Mercado de Capitales Argentino",
                "Instrumentos financieros en Argentina: Acciones, Bonos, ONs y Opciones.",
                150000f, catMercado
        );
        curso1.setMesesAcceso(12);
        cursoRepository.save(curso1);
        docenteCursoRepository.save(new DocenteCurso(docenteFausto, curso1, false)); // titular
        docenteCursoRepository.save(new DocenteCurso(docenteSebas, curso1, true));   // supervisor

        Curso curso2 = new Curso(
                "Análisis Macroeconómico e Inflación en Argentina",
                "Herramientas para analizar variables macroeconómicas y proyectar escenarios.",
                0f, catEconomia  // Gratuito
        );
        cursoRepository.save(curso2);
        docenteCursoRepository.save(new DocenteCurso(docenteFausto, curso2, false));

        Curso curso3 = new Curso(
                "Planificación Fiscal y Eficiencia Tributaria",
                "Estrategias impositivas para pymes y profesionales.",
                120000f, catImpuestos
        );
        curso3.setMesesAcceso(6);
        cursoRepository.save(curso3);
        docenteCursoRepository.save(new DocenteCurso(docenteSebas, curso3, false));

        // ── 5. Unidades del Curso 1 ────────────────────────────────────────────

        Unidad u1 = unidadRepository.save(new Unidad("Introducción al Sistema Financiero", "Estructura del mercado argentino, CNV, BYMA y agentes liquidadores.", 1, curso1));
        Unidad u2 = unidadRepository.save(new Unidad("Renta Fija: Bonos y ONs", "Cálculo de TIR, Duration, curvas de rendimientos y riesgo país.", 2, curso1));
        Unidad u3 = unidadRepository.save(new Unidad("Renta Variable y CEDEARs", "Inversión en acciones locales e internacionales.", 3, curso1));

        // ── 6. Materiales Unidad 1 ─────────────────────────────────────────────

        materialRepository.save(new Material(tmGrabacion, "Clase Grabada - Módulo 1", "videos/u1_clase.mp4", u1));
        materialRepository.save(new Material(tmBibliografia, "Ley de Mercado de Capitales 26.831", "docs/ley_26831.pdf", u1));
        materialRepository.save(new Material(tmPresentacion, "Diapositivas Unidad 1", "slides/u1_slides.pdf", u1));

        System.out.println("✅ Idóneos Online: Datos iniciales insertados correctamente.");
    }
}
