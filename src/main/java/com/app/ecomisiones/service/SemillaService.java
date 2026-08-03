package com.app.ecomisiones.service;

import com.app.ecomisiones.model.*;
import com.app.ecomisiones.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio para insertar datos iniciales (Semilla) de Idóneos Online.
 */
@Service
public class SemillaService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private UnidadRepository unidadRepository;

    @Autowired
    private MaterialRepository materialRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Transactional
    public void insertarSemilla() {
        String adminEmail = "admin@idoneos.online";
        if (usuarioRepository.findByCorreoAndBajaFalse(adminEmail).isEmpty()) {
            
            // 1. Usuarios Administradores y Docentes
            Usuario admin = new Usuario("Admin", "Idóneos", adminEmail, passwordEncoder.encode("123456"), RolUsuario.Administrador);
            usuarioRepository.save(admin);

            Usuario docenteFausto = new Usuario("Fausto", "Spotorno", "fausto.spotorno@idoneos.online", passwordEncoder.encode("123456"), RolUsuario.Docente);
            docenteFausto.setHabilitadoClonIA(true);
            usuarioRepository.save(docenteFausto);

            Usuario docenteSebas = new Usuario("Sebastián", "Bordato", "sebastian.bordato@idoneos.online", passwordEncoder.encode("123456"), RolUsuario.Docente);
            docenteSebas.setHabilitadoClonIA(true);
            usuarioRepository.save(docenteSebas);

            Usuario alumnoDemo = new Usuario("Juan", "Pérez", "alumno@correo.com", passwordEncoder.encode("123456"), RolUsuario.Alumno);
            usuarioRepository.save(alumnoDemo);

            // 2. Categorías temáticas
            Categoria catMercado = new Categoria("Mercado de Capitales", "Cursos sobre bonos, acciones, CEDEARs, instrumentos de inversión y normativa CNV.");
            categoriaRepository.save(catMercado);

            Categoria catEconomia = new Categoria("Economía", "Análisis macroeconómico, coyuntura argentina, política monetaria y fiscal.");
            categoriaRepository.save(catEconomia);

            Categoria catFinanzas = new Categoria("Finanzas Corporativas", "Gestión financiera de empresas, evaluación de proyectos y valuación.");
            categoriaRepository.save(catFinanzas);

            Categoria catImpuestos = new Categoria("Impuestos y Contabilidad", "Legislación tributaria argentina, planificación fiscal y estados contables.");
            categoriaRepository.save(catImpuestos);

            // 3. Cursos iniciales
            Curso curso1 = new Curso(
                    "Introducción al Mercado de Capitales Argentino",
                    "Curso esencial para entender los instrumentos financieros en Argentina: Acciones, Bonos, ONs y Opciones. Dictado por Fausto Spotorno y Sebastián Bordato.",
                    150000f,
                    catMercado,
                    docenteFausto
            );
            curso1.setDocenteSupervisor(docenteSebas);
            cursoRepository.save(curso1);

            Curso curso2 = new Curso(
                    "Análisis Macroeconómico e Inflación en Argentina",
                    "Herramientas teóricas y prácticas para analizar variables macroeconómicas, interpretar datos oficiales y proyectar escenarios de negocio.",
                    0f, // Gratuito
                    catEconomia,
                    docenteFausto
            );
            cursoRepository.save(curso2);

            Curso curso3 = new Curso(
                    "Planificación Fiscal y Eficiencia Tributaria",
                    "Estradas impositivas para pymes y profesionales en el marco de la normativa tributaria argentina vigente.",
                    120000f,
                    catImpuestos,
                    docenteSebas
            );
            cursoRepository.save(curso3);

            // 4. Unidades del Curso 1
            Unidad u1 = new Unidad("Introducción al Sistema Financiero", "Estructura del mercado argentino, CNV, BYMA y agentes liquidadores.", 1, curso1);
            unidadRepository.save(u1);

            Unidad u2 = new Unidad("Renta Fija: Bonos y Obligaciones Negociables", "Cálculo de TIR, Duration, curvas de rendimientos y riesgo país.", 2, curso1);
            unidadRepository.save(u2);

            Unidad u3 = new Unidad("Renta Variable y CEDEARs", "Inversión en acciones locales e internacionales a través de CEDEARs.", 3, curso1);
            unidadRepository.save(u3);

            // 5. Materiales didácticos para Unidad 1
            Material m1 = new Material(TipoMaterial.GRABACION, "Clase Grabada - Módulo 1", "videos/u1_clase.mp4", u1);
            materialRepository.save(m1);

            Material m2 = new Material(TipoMaterial.BIBLIOGRAFIA, "Ley de Mercado de Capitales 26.831", "docs/ley_26831.pdf", u1);
            materialRepository.save(m2);

            Material m3 = new Material(TipoMaterial.GLOSARIO, "Glosario de Términos Financieros", "docs/glosario.pdf", u1);
            materialRepository.save(m3);

            System.out.println("Idóneos Online: Datos iniciales insertados correctamente.");
        } else {
            System.out.println("Idóneos Online: La semilla ya se encuentra insertada.");
        }
    }
}
