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

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * CU-93 — Semillado automático de la base de datos `idoneos.online`.
     */
    @Transactional
    public void insertarSemilla() {
        String adminEmail = "admin@idoneos.online";
        if (usuarioRepository.findByCorreoAndBajaFalse(adminEmail).isPresent()) {
            System.out.println("Idóneos Online: La semilla ya se encuentra insertada.");
            return;
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
        Usuario adminUsuario = new Usuario("Admin", "Idóneos", adminEmail, passwordEncoder.encode("123456"), RolUsuario.Administrador);
        adminUsuario.setEmailValidado(true);
        Administrador adminObj = new Administrador(adminUsuario);
        adminUsuario.setAdministrador(adminObj);
        usuarioRepository.save(adminUsuario);

        Usuario usuFausto = new Usuario("Fausto", "Spotorno", "fausto.spotorno@idoneos.online", passwordEncoder.encode("123456"), RolUsuario.Docente);
        usuFausto.setEmailValidado(true);
        Docente docenteFausto = new Docente(usuFausto);
        docenteFausto.setBiografia("Economista UBA, Magíster en Finanzas UTDT.");
        docenteFausto.setAniosExperiencia(20);
        docenteFausto.setFechaConsentimientoClon(LocalDateTime.now());
        usuFausto.setDocente(docenteFausto);
        usuarioRepository.save(usuFausto);

        Usuario usuSebas = new Usuario("Sebastián", "Bordato", "sebastian.bordato@idoneos.online", passwordEncoder.encode("123456"), RolUsuario.Docente);
        usuSebas.setEmailValidado(true);
        Docente docenteSebas = new Docente(usuSebas);
        docenteSebas.setBiografia("Contador Público UBA. Especialista en planificación fiscal.");
        docenteSebas.setAniosExperiencia(15);
        docenteSebas.setFechaConsentimientoClon(LocalDateTime.now());
        usuSebas.setDocente(docenteSebas);
        usuarioRepository.save(usuSebas);

        Usuario usuAlumno = new Usuario("Juan", "Pérez", "alumno@correo.com", passwordEncoder.encode("123456"), RolUsuario.Alumno);
        usuAlumno.setEmailValidado(true);
        Alumno alumnoObj = new Alumno(usuAlumno);
        usuAlumno.setAlumno(alumnoObj);
        usuarioRepository.save(usuAlumno);

        // Categorías temáticas
        Categoria catMercado = categoriaRepository.save(new Categoria("Mercado de Capitales", "Cursos sobre bonos, acciones, CEDEARs e instrumentos de inversión."));
        Categoria catEconomia = categoriaRepository.save(new Categoria("Economía", "Análisis macroeconómico y coyuntura argentina."));
        Categoria catFinanzas = categoriaRepository.save(new Categoria("Finanzas Corporativas", "Gestión financiera de empresas y valuación."));
        Categoria catImpuestos = categoriaRepository.save(new Categoria("Impuestos y Contabilidad", "Legislación tributaria argentina."));

        // Cursos demo
        Curso curso1 = new Curso(
                "Introducción al Mercado de Capitales Argentino",
                "Instrumentos financieros en Argentina: Acciones, Bonos, ONs y Opciones.",
                150000f, catMercado
        );
        curso1.setMesesAcceso(12);
        cursoRepository.save(curso1);

        Programa prog1 = programaRepository.save(new Programa("Programa Inicial 2026", "Plan de estudios principal del curso", 12, curso1));
        Dictado dictado1 = dictadoRepository.save(new Dictado(LocalDateTime.now(), LocalDateTime.now().plusMonths(6), 50, prog1));

        dictadoDocenteRepository.save(new DictadoDocente(dictado1, docenteFausto, false));
        dictadoDocenteRepository.save(new DictadoDocente(dictado1, docenteSebas, true));

        Curso curso2 = new Curso(
                "Análisis Macroeconómico e Inflación en Argentina",
                "Herramientas para analizar variables macroeconómicas.",
                0f, catEconomia
        );
        cursoRepository.save(curso2);

        Programa prog2 = programaRepository.save(new Programa("Programa Economía 2026", "Plan de estudio macroeconomía", 6, curso2));
        Dictado dictado2 = dictadoRepository.save(new Dictado(LocalDateTime.now(), LocalDateTime.now().plusMonths(6), 100, prog2));

        dictadoDocenteRepository.save(new DictadoDocente(dictado2, docenteFausto, false));

        Curso curso3 = new Curso(
                "Planificación Fiscal y Eficiencia Tributaria",
                "Estrategias impositivas para pymes y profesionales.",
                200f, catImpuestos
        );
        curso3.setMesesAcceso(6);
        cursoRepository.save(curso3);

        Programa prog3 = programaRepository.save(new Programa("Programa Fiscal 2026", "Plan de estudios impositivo", 6, curso3));
        Dictado dictado3 = dictadoRepository.save(new Dictado(LocalDateTime.now(), LocalDateTime.now().plusMonths(6), 40, prog3));

        dictadoDocenteRepository.save(new DictadoDocente(dictado3, docenteSebas, false));

        // Unidades y Materiales
        Unidad u1 = unidadRepository.save(new Unidad("Introducción al Sistema Financiero", "Estructura del mercado argentino, CNV, BYMA.", 1, curso1));
        Unidad u2 = unidadRepository.save(new Unidad("Renta Fija: Bonos y ONs", "Cálculo de TIR, Duration, curvas de rendimientos.", 2, curso1));
        Unidad u3 = unidadRepository.save(new Unidad("Renta Variable y CEDEARs", "Inversión en acciones locales e internacionales.", 3, curso1));

        materialRepository.save(new Material(tmGrabacion, "Clase Grabada - Módulo 1", "videos/u1_clase.mp4", u1));
        materialRepository.save(new Material(tmBibliografia, "Ley de Mercado de Capitales 26.831", "docs/ley_26831.pdf", u1));
        materialRepository.save(new Material(tmPresentacion, "Diapositivas Unidad 1", "slides/u1_slides.pdf", u1));

        System.out.println("✅ Idóneos Online: Datos iniciales insertados correctamente.");
    }
}
