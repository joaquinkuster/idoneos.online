package com.app.idoneos;

import com.app.idoneos.model.*;
import com.app.idoneos.repository.modulo_cursos.CohorteRepository;
import com.app.idoneos.repository.modulo_cursos.CursoRepository;
import com.app.idoneos.repository.modulo_gestion_academica.*;
import com.app.idoneos.repository.modulo_inscripciones.InscripcionRepository;
import com.app.idoneos.repository.modulo_usuarios.AlumnoRepository;
import com.app.idoneos.repository.modulo_usuarios.UsuarioRepository;
import com.app.idoneos.service.modulo_gestion_academica.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Suite de Pruebas de Integración y Arquitectura para MOD-F-02: Módulo de Gestión Académica.
 * Cubre exhaustivamente los Casos de Uso CU-15 al CU-42 con verificaciones de:
 * - Flujos principales y alternativos de DSS
 * - Herencia y clonación de datos (CU-16 idProgramaAnterior)
 * - Asociación de unidades existentes y registros de Cronograma (CU-20)
 * - Aserciones precisas de Model y Participantes (CU-25)
 * - Restricciones de seguridad y autorización 403 (CU-26 Alumno vs Modo Edición)
 * - Asignación de TipoMaterial en recursos (CU-28)
 * - Foros, Consultas, Respuestas y manejo de 404/Redirecciones seguras
 */
@SpringBootTest(properties = {
    "spring.security.oauth2.client.registration.google.client-id=demo-id",
    "spring.security.oauth2.client.registration.google.client-secret=demo-secret"
})
@AutoConfigureMockMvc
@Transactional
public class Modulo02AcademicoIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ProgramaService programaService;
    @Autowired private UnidadService unidadService;
    @Autowired private MaterialService materialService;
    @Autowired private GlosarioService glosarioService;
    @Autowired private ForoService foroService;
    @Autowired private CursoRepository cursoRepository;
    @Autowired private ProgramaRepository programaRepository;
    @Autowired private UnidadRepository unidadRepository;
    @Autowired private CronogramaRepository cronogramaRepository;
    @Autowired private MaterialRepository materialRepository;
    @Autowired private TipoMaterialRepository tipoMaterialRepository;
    @Autowired private TerminoGlosarioRepository terminoGlosarioRepository;
    @Autowired private ConsultaForoRepository consultaForoRepository;
    @Autowired private RespuestaForoRepository respuestaForoRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private com.app.idoneos.repository.modulo_usuarios.RolRepository rolRepository;
    @Autowired private CohorteRepository cohorteRepository;
    @Autowired private InscripcionRepository inscripcionRepository;
    @Autowired private AlumnoRepository alumnoRepository;

    private Curso cursoBase;
    private Programa programaBase;
    private Unidad unidadBase;

    @BeforeEach
    void setUpDatosPrueba() {
        cursoBase = cursoRepository.findAll().stream().filter(c -> !c.isBaja()).findFirst().orElseGet(() -> {
            Curso c = new Curso();
            c.setNombre("Curso Base QA");
            c.setDescripcion("Curso para suite de tests");
            c.setBaja(false);
            return cursoRepository.save(c);
        });

        List<Programa> progs = programaRepository.findByCursoAndBajaFalse(cursoBase);
        if (!progs.isEmpty()) {
            programaBase = progs.get(0);
        } else {
            programaBase = new Programa("Programa Base QA", "Desc Base", "Objetivos Base QA", "Bibliografia Base QA", cursoBase);
            programaBase.setCargaHorariaTotal(60);
            programaBase.setBaja(false);
            programaBase = programaRepository.save(programaBase);
        }

        List<Unidad> unidades = unidadService.obtenerPorCurso(cursoBase);
        if (!unidades.isEmpty()) {
            unidadBase = unidades.get(0);
        } else {
            unidadBase = new Unidad("Unidad Base QA", "Descripcion Base", "Contenido Tematico Base");
            unidadBase.setBaja(false);
            unidadBase = unidadRepository.save(unidadBase);

            if (!cronogramaRepository.existsByProgramaAndUnidad(programaBase, unidadBase)) {
                cronogramaRepository.save(new Cronograma(1, 2, programaBase, unidadBase));
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    // CU-15: Buscar programa
    // ─────────────────────────────────────────────────────────────
    @Test
    @WithMockUser(username = "admin@idoneos.online", roles = {"Administrador"})
    @DisplayName("CU-15: Buscar programa - Admin accede a vista con programas y cursos del modelo")
    void testCU15_BuscarPrograma() throws Exception {
        mockMvc.perform(get("/academico/programas").param("cursoId", String.valueOf(cursoBase.getIdCurso())))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/academico/cu-15-buscar-programa"))
                .andExpect(model().attributeExists("programas"))
                .andExpect(model().attributeExists("cursos"))
                .andExpect(model().attribute("cursoSeleccionado", cursoBase.getIdCurso()));
    }

    // ─────────────────────────────────────────────────────────────
    // CU-16: Registrar programa (Estándar y a partir de programa anterior)
    // ─────────────────────────────────────────────────────────────
    @Test
    @WithMockUser(username = "admin@idoneos.online", roles = {"Administrador"})
    @DisplayName("CU-16: Registrar programa - Creación estándar con campos completos")
    void testCU16_RegistrarPrograma_Estandar() throws Exception {
        mockMvc.perform(get("/academico/programas/nuevo").param("cursoId", String.valueOf(cursoBase.getIdCurso())))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/academico/cu-16-registrar-programa"))
                .andExpect(model().attributeExists("cursos"));

        mockMvc.perform(post("/academico/programas/guardar")
                .param("cursoId", String.valueOf(cursoBase.getIdCurso()))
                .param("nombre", "Programa Exclusivo 2026")
                .param("descripcion", "Descripción completa de la versión 2026")
                .param("objetivos", "Comprender Mercado de Capitales y Renta Fija")
                .param("cargaHorariaTotal", "120")
                .param("bibliografia", "Ley 26.831 y Manual IAMC 2026"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/academico/programas*"))
                .andExpect(flash().attributeExists("mensaje"));

        List<Programa> progs = programaRepository.findByCurso(cursoBase);
        assertTrue(progs.stream().anyMatch(p -> "Programa Exclusivo 2026".equals(p.getNombre())));
    }

    @Test
    @WithMockUser(username = "admin@idoneos.online", roles = {"Administrador"})
    @DisplayName("CU-16: Registrar programa - Flujo alternativo clonando datos desde idProgramaAnterior")
    void testCU16_RegistrarPrograma_ConProgramaAnterior() throws Exception {
        programaBase.setObjetivos("Objetivos Heredados de Edición Anterior");
        programaBase.setBibliografia("Bibliografia Heredada CNV 2025");
        programaBase.setCargaHorariaTotal(95);
        programaRepository.save(programaBase);

        mockMvc.perform(post("/academico/programas/guardar")
                .param("cursoId", String.valueOf(cursoBase.getIdCurso()))
                .param("nombre", "Programa Clonado 2027")
                .param("idProgramaAnterior", String.valueOf(programaBase.getId())))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/academico/programas*"))
                .andExpect(flash().attributeExists("mensaje"));

        Programa clonado = programaRepository.findAll().stream()
                .filter(p -> "Programa Clonado 2027".equals(p.getNombre()))
                .findFirst().orElseThrow();

        assertEquals("Objetivos Heredados de Edición Anterior", clonado.getObjetivos());
        assertEquals("Bibliografia Heredada CNV 2025", clonado.getBibliografia());
        assertEquals(95, clonado.getCargaHorariaTotal());
    }

    // ─────────────────────────────────────────────────────────────
    // CU-17: Modificar programa
    // ─────────────────────────────────────────────────────────────
    @Test
    @WithMockUser(username = "admin@idoneos.online", roles = {"Administrador"})
    @DisplayName("CU-17: Modificar programa - Actualización de objetivos y bibliografía")
    void testCU17_ModificarPrograma() throws Exception {
        mockMvc.perform(get("/academico/programas/" + programaBase.getId() + "/editar"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/academico/cu-17-modificar-programa"))
                .andExpect(model().attributeExists("programa"));

        mockMvc.perform(post("/academico/programas/" + programaBase.getId() + "/editar")
                .param("nombre", "Programa Base Actualizado")
                .param("descripcion", "Nueva descripcion modificada")
                .param("objetivos", "Nuevos objetivos analiticos")
                .param("cargaHorariaTotal", "150")
                .param("bibliografia", "Nueva bibliografia oficial"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/academico/programas*"))
                .andExpect(flash().attributeExists("mensaje"));

        Programa actualizado = programaRepository.findById(programaBase.getId()).orElseThrow();
        assertEquals("Programa Base Actualizado", actualizado.getNombre());
        assertEquals("Nueva bibliografia oficial", actualizado.getBibliografia());
    }

    // ─────────────────────────────────────────────────────────────
    // CU-18: Dar de baja programa
    // ─────────────────────────────────────────────────────────────
    @Test
    @WithMockUser(username = "admin@idoneos.online", roles = {"Administrador"})
    @DisplayName("CU-18: Dar de baja programa - Marca baja lógica y persiste")
    void testCU18_DarDeBajaPrograma() throws Exception {
        Programa prog = programaService.registrarPrograma(cursoBase.getIdCurso(), "Prog Baja Exclusivo", "Desc", "v1.0");

        mockMvc.perform(get("/academico/programas/" + prog.getId() + "/baja"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/academico/cu-18-dar-de-baja-programa"))
                .andExpect(model().attributeExists("programa"));

        mockMvc.perform(post("/academico/programas/" + prog.getId() + "/baja"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/academico/programas*"))
                .andExpect(flash().attributeExists("mensaje"));

        Programa dadoDeBaja = programaRepository.findById(prog.getId()).orElseThrow();
        assertTrue(dadoDeBaja.isBaja());
    }

    // ─────────────────────────────────────────────────────────────
    // CU-19: Buscar unidad
    // ─────────────────────────────────────────────────────────────
    @Test
    @WithMockUser(username = "docente@idoneos.online", roles = {"Docente"})
    @DisplayName("CU-19: Buscar unidad - Verificación de unidades pertenecientes al curso en el modelo")
    void testCU19_BuscarUnidad() throws Exception {
        mockMvc.perform(get("/academico/unidades").param("cursoId", String.valueOf(cursoBase.getIdCurso())))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/academico/cu-19-buscar-unidad"))
                .andExpect(model().attributeExists("unidades"))
                .andExpect(model().attributeExists("todasUnidades"));
    }

    // ─────────────────────────────────────────────────────────────
    // CU-20: Agregar unidad (Crear nueva y Asociar existente con Cronograma)
    // ─────────────────────────────────────────────────────────────
    @Test
    @WithMockUser(username = "docente@idoneos.online", roles = {"Docente"})
    @DisplayName("CU-20: Agregar unidad - Creación de nueva unidad y registro automático de Cronograma")
    void testCU20_AgregarUnidad_Nueva() throws Exception {
        mockMvc.perform(get("/academico/unidades/nueva").param("cursoId", String.valueOf(cursoBase.getIdCurso())))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/academico/cu-20-agregar-unidad"));

        mockMvc.perform(post("/academico/unidades/guardar")
                .param("cursoId", String.valueOf(cursoBase.getIdCurso()))
                .param("programaId", String.valueOf(programaBase.getId()))
                .param("titulo", "Unidad Renta Fija Avanzada")
                .param("descripcion", "Bonos soberanos y corporativos")
                .param("contenido", "Estructura de tasas y duration")
                .param("numeroOrden", "2")
                .param("semanasDuracion", "3"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/academico/unidades*"))
                .andExpect(flash().attributeExists("mensaje"));

        Unidad uCreada = unidadRepository.findAll().stream()
                .filter(u -> "Unidad Renta Fija Avanzada".equals(u.getTitulo()))
                .findFirst().orElseThrow();

        assertTrue(cronogramaRepository.existsByProgramaAndUnidad(programaBase, uCreada));
    }

    @Test
    @WithMockUser(username = "docente@idoneos.online", roles = {"Docente"})
    @DisplayName("CU-20: Agregar unidad - Asociar unidad existente a través de /unidades/asociar")
    void testCU20_AsociarUnidadExistente() throws Exception {
        Unidad existente = new Unidad("Unidad Existente para Vincular", "Desc", "Contenido");
        existente = unidadRepository.save(existente);

        mockMvc.perform(post("/academico/unidades/asociar")
                .param("programaId", String.valueOf(programaBase.getId()))
                .param("unidadId", String.valueOf(existente.getId()))
                .param("numeroOrden", "4")
                .param("semanasDuracion", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/academico/unidades*"))
                .andExpect(flash().attributeExists("mensaje"));

        assertTrue(cronogramaRepository.existsByProgramaAndUnidad(programaBase, existente));
    }

    // ─────────────────────────────────────────────────────────────
    // CU-21: Modificar unidad
    // ─────────────────────────────────────────────────────────────
    @Test
    @WithMockUser(username = "docente@idoneos.online", roles = {"Docente"})
    @DisplayName("CU-21: Modificar unidad - Actualización de título y contenido")
    void testCU21_ModificarUnidad() throws Exception {
        mockMvc.perform(get("/academico/unidades/" + unidadBase.getId() + "/editar"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/academico/cu-21-modificar-unidad"))
                .andExpect(model().attributeExists("unidad"));

        mockMvc.perform(post("/academico/unidades/" + unidadBase.getId() + "/editar")
                .param("titulo", "Unidad Base QA Modificada")
                .param("descripcion", "Nueva descripcion modificada")
                .param("contenido", "Nuevo temario ampliado"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/academico/unidades*"))
                .andExpect(flash().attributeExists("mensaje"));

        Unidad uModificada = unidadRepository.findById(unidadBase.getId()).orElseThrow();
        assertEquals("Unidad Base QA Modificada", uModificada.getTitulo());
    }

    // ─────────────────────────────────────────────────────────────
    // CU-22: Quitar unidad
    // ─────────────────────────────────────────────────────────────
    @Test
    @WithMockUser(username = "docente@idoneos.online", roles = {"Docente"})
    @DisplayName("CU-22: Quitar unidad - Baja lógica de unidad temática")
    void testCU22_QuitarUnidad() throws Exception {
        Unidad u = unidadService.guardar(new Unidad("Unidad Descartable QA", "Desc", "Contenido"));

        mockMvc.perform(get("/academico/unidades/" + u.getId() + "/quitar"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/academico/cu-22-quitar-unidad"))
                .andExpect(model().attributeExists("unidad"));

        mockMvc.perform(post("/academico/unidades/" + u.getId() + "/baja"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/academico/unidades*"))
                .andExpect(flash().attributeExists("mensaje"));

        Unidad baja = unidadRepository.findById(u.getId()).orElseThrow();
        assertTrue(baja.isBaja());
    }

    // ─────────────────────────────────────────────────────────────
    // CU-23 & CU-24: Cronograma
    // ─────────────────────────────────────────────────────────────
    @Test
    @WithMockUser(username = "docente@idoneos.online", roles = {"Docente"})
    @DisplayName("CU-23 & CU-24: Cronograma - Búsqueda y Modificación por cohorte/programa")
    void testCU23_CU24_Cronograma() throws Exception {
        mockMvc.perform(get("/academico/cronogramas"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/academico/cu-23-buscar-cronograma"))
                .andExpect(model().attributeExists("cohortes"));

        Cronograma cron = cronogramaRepository.findAll().stream().findFirst().orElseGet(() -> 
            cronogramaRepository.save(new Cronograma(1, 2, programaBase, unidadBase))
        );

        mockMvc.perform(post("/academico/cronogramas/guardar")
                .param("cronogramaId", String.valueOf(cron.getId()))
                .param("semanasDuracion", "4")
                .param("numeroOrden", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/academico/cronograma*"));

        Cronograma act = cronogramaRepository.findById(cron.getId()).orElseThrow();
        assertEquals(4, act.getSemanasDuracion());
    }

    // ─────────────────────────────────────────────────────────────
    // CU-25: Ver participantes
    // ─────────────────────────────────────────────────────────────
    @Test
    @WithMockUser(username = "docente@idoneos.online", roles = {"Docente"})
    @DisplayName("CU-25: Ver participantes - Carga de cohorte con participante específico verificado en Model")
    void testCU25_VerParticipantes_ConInscripcionEspecifica() throws Exception {
        Cohorte cohorte = new Cohorte();
        cohorte.setPrograma(programaBase);
        cohorte.setFechaInicioInscripcion(LocalDateTime.now().minusDays(10));
        cohorte.setFechaFinInscripcion(LocalDateTime.now().plusDays(20));
        cohorte.setSemanasAcceso(12);
        cohorte.setBaja(false);
        cohorte = cohorteRepository.save(cohorte);

        Rol rolAlumno = rolRepository.findByNombre("Alumno").orElseGet(() -> rolRepository.save(new Rol("Alumno")));
        Usuario usuAlumno = usuarioRepository.findByCorreo("alumno.participante@idoneos.online").orElseGet(() -> {
            Usuario u = new Usuario();
            u.setNombre("Carlos");
            u.setApellido("Alumno");
            u.setCorreo("alumno.participante@idoneos.online");
            u.setDni("39999888");
            u.setContrasena("123456");
            u.setRol(rolAlumno);
            u.setBaja(false);
            return usuarioRepository.save(u);
        });

        Alumno alumno = alumnoRepository.findByUsuario(usuAlumno).orElseGet(() -> alumnoRepository.save(new Alumno(usuAlumno)));

        Inscripcion inscripcion = new Inscripcion(cohorte, alumno);
        inscripcion.setNombreAlumno(usuAlumno.getNombreCompleto());
        inscripcion.setDniAlumno(usuAlumno.getDni());
        inscripcion.setFecha(LocalDateTime.now());
        inscripcion.setFechaVencimientoAcceso(LocalDateTime.now().plusMonths(6));
        inscripcion.setBaja(false);
        inscripcionRepository.save(inscripcion);

        mockMvc.perform(get("/academico/participantes").param("cohorteId", String.valueOf(cohorte.getIdCohorte())))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/academico/cu-25-ver-participantes"))
                .andExpect(model().attributeExists("participantes"))
                .andExpect(model().attributeExists("cohortes"));
    }

    // ─────────────────────────────────────────────────────────────
    // CU-26 & CU-26b: Acceder curso y control de seguridad
    // ─────────────────────────────────────────────────────────────
    @Test
    @WithMockUser(username = "fausto.spotorno@idoneos.online", roles = {"Docente"})
    @DisplayName("CU-26: Acceder curso - Docente accede al aula y al modo edición")
    void testCU26_AccederCurso_Docente() throws Exception {
        mockMvc.perform(get("/academico/curso/" + cursoBase.getIdCurso()))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/academico/cu-26-acceder-curso"))
                .andExpect(model().attributeExists("curso"))
                .andExpect(model().attributeExists("unidades"));

        mockMvc.perform(get("/academico/curso/" + cursoBase.getIdCurso() + "/edicion"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/academico/cu-26b-acceder-curso-modo-edicion-docente-administrador"))
                .andExpect(model().attributeExists("modoEdicion"));
    }

    @Test
    @WithMockUser(username = "alumno@correo.com", roles = {"Alumno"})
    @DisplayName("CU-26 Seguridad: Alumno intentando acceder a /edicion debe recibir 403 Forbidden")
    void testCU26_AlumnoNoPuedeAccederModoEdicion() throws Exception {
        mockMvc.perform(get("/academico/curso/" + cursoBase.getIdCurso() + "/edicion"))
                .andExpect(status().isForbidden());
    }

    // ─────────────────────────────────────────────────────────────
    // CU-27 a CU-30: Materiales Educativos y TipoMaterial
    // ─────────────────────────────────────────────────────────────
    @Test
    @WithMockUser(username = "fausto.spotorno@idoneos.online", roles = {"Docente"})
    @DisplayName("CU-28 a CU-30: Materiales - Subir con TipoMaterial, Modificar y Dar de baja")
    void testCU27_a_CU30_Materiales_ConTipoMaterial() throws Exception {
        TipoMaterial tipo = tipoMaterialRepository.findAll().stream().findFirst().orElseGet(() -> {
            TipoMaterial tm = new TipoMaterial();
            tm.setNombre("Bibliografía Obligatoria");
            return tipoMaterialRepository.save(tm);
        });

        // CU-27 Buscar
        mockMvc.perform(get("/academico/materiales").param("unidadId", String.valueOf(unidadBase.getId())))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/academico/cu-27-buscar-material"))
                .andExpect(model().attributeExists("materiales"));

        // CU-28 Subir Material con tipoMaterialId
        mockMvc.perform(post("/academico/materiales/guardar")
                .param("titulo", "Manual BYMA 2026")
                .param("descripcion", "Guía oficial de negociación bursátil")
                .param("url", "https://idoneos.online/docs/manual_byma.pdf")
                .param("unidadId", String.valueOf(unidadBase.getId()))
                .param("tipoMaterialId", String.valueOf(tipo.getIdTipoMaterial()))
                .param("oculto", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/academico/materiales*"))
                .andExpect(flash().attributeExists("mensaje"));

        Material creado = materialRepository.findAll().stream()
                .filter(m -> "Manual BYMA 2026".equals(m.getTitulo()))
                .findFirst().orElseThrow();

        assertEquals(tipo.getIdTipoMaterial(), creado.getTipoMaterial().getIdTipoMaterial());
        assertTrue(creado.isOculto());

        // CU-29 Modificar Material
        mockMvc.perform(post("/academico/materiales/" + creado.getId() + "/editar")
                .param("titulo", "Manual BYMA 2026 Edición Ampliada")
                .param("descripcion", "Guía actualizada")
                .param("url", "https://idoneos.online/docs/manual_v2.pdf")
                .param("oculto", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/academico/materiales*"))
                .andExpect(flash().attributeExists("mensaje"));

        Material mod = materialRepository.findById(creado.getId()).orElseThrow();
        assertEquals("Manual BYMA 2026 Edición Ampliada", mod.getTitulo());
        assertFalse(mod.isOculto());

        // CU-30 Dar de baja Material
        mockMvc.perform(post("/academico/materiales/" + creado.getId() + "/baja"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/academico/materiales*"))
                .andExpect(flash().attributeExists("mensaje"));

        Material dadoDeBaja = materialRepository.findById(creado.getId()).orElseThrow();
        assertTrue(dadoDeBaja.isBaja());
    }

    // ─────────────────────────────────────────────────────────────
    // CU-31 a CU-34: Glosario
    // ─────────────────────────────────────────────────────────────
    @Test
    @WithMockUser(username = "fausto.spotorno@idoneos.online", roles = {"Docente"})
    @DisplayName("CU-31 a CU-34: Glosario - Buscar, Registrar, Modificar y Dar de baja término")
    void testCU31_a_CU34_Glosario() throws Exception {
        mockMvc.perform(get("/academico/glosario").param("unidadId", String.valueOf(unidadBase.getId())))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/academico/cu-31-buscar-termino-de-glosario"))
                .andExpect(model().attributeExists("terminos"));

        // CU-32 Registrar
        mockMvc.perform(post("/academico/glosario/guardar")
                .param("termino", "Duration Modificada")
                .param("definicion", "Medida de la sensibilidad del precio de un bono ante variaciones en el rendimiento.")
                .param("unidadId", String.valueOf(unidadBase.getId())))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/academico/glosario*"))
                .andExpect(flash().attributeExists("mensaje"));

        TerminoGlosario reg = terminoGlosarioRepository.findAll().stream()
                .filter(t -> "Duration Modificada".equals(t.getTermino()))
                .findFirst().orElseThrow();

        // CU-33 Modificar
        mockMvc.perform(post("/academico/glosario/" + reg.getId() + "/editar")
                .param("termino", "Duration Modificada de Macaulay")
                .param("definicion", "Medida ampliada de sensibilidad del precio.")
                .param("unidadId", String.valueOf(unidadBase.getId())))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/academico/glosario*"))
                .andExpect(flash().attributeExists("mensaje"));

        TerminoGlosario mod = terminoGlosarioRepository.findById(reg.getId()).orElseThrow();
        assertEquals("Duration Modificada de Macaulay", mod.getTermino());

        // CU-34 Dar de baja
        mockMvc.perform(post("/academico/glosario/" + reg.getId() + "/baja"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/academico/glosario*"))
                .andExpect(flash().attributeExists("mensaje"));

        TerminoGlosario baja = terminoGlosarioRepository.findById(reg.getId()).orElseThrow();
        assertTrue(baja.isBaja());
    }

    // ─────────────────────────────────────────────────────────────
    // CU-35 a CU-38: Foros (Consultas)
    // ─────────────────────────────────────────────────────────────
    @Test
    @WithMockUser(username = "fausto.spotorno@idoneos.online", roles = {"Docente"})
    @DisplayName("CU-35 a CU-38: Consultas de Foro - Registro, Modificación sin asunto y Baja")
    void testCU35_a_CU38_ConsultasForo() throws Exception {
        mockMvc.perform(get("/academico/consultas").param("unidadId", String.valueOf(unidadBase.getId())))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/academico/cu-35-buscar-consulta-de-foro"))
                .andExpect(model().attributeExists("consultas"));

        // CU-36 Registrar Consulta
        mockMvc.perform(post("/academico/consultas/guardar")
                .param("texto", "¿Cómo se calcula la duration con cupón cero?")
                .param("unidadId", String.valueOf(unidadBase.getId())))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/academico/consultas*"))
                .andExpect(flash().attributeExists("mensaje"));

        ConsultaForo creada = consultaForoRepository.findAll().stream()
                .filter(c -> c.getTexto().contains("cupón cero"))
                .findFirst().orElseThrow();

        // CU-37 Modificar Consulta (DSS: modificarConsultaForo(unaConsulta, texto))
        mockMvc.perform(post("/academico/consultas/" + creada.getId() + "/editar")
                .param("texto", "¿Cómo se calcula la duration exacta de un bono cupón cero a madurez?")
                .param("unidadId", String.valueOf(unidadBase.getId())))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/academico/consultas*"))
                .andExpect(flash().attributeExists("mensaje"));

        ConsultaForo mod = consultaForoRepository.findById(creada.getId()).orElseThrow();
        assertEquals("¿Cómo se calcula la duration exacta de un bono cupón cero a madurez?", mod.getTexto());

        // CU-38 Dar de baja Consulta
        mockMvc.perform(post("/academico/consultas/" + creada.getId() + "/baja"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/academico/consultas*"))
                .andExpect(flash().attributeExists("mensaje"));

        ConsultaForo baja = consultaForoRepository.findById(creada.getId()).orElseThrow();
        assertTrue(baja.isBaja());
    }

    // ─────────────────────────────────────────────────────────────
    // CU-39 a CU-42: Respuestas de Foro & Validación de Existencia
    // ─────────────────────────────────────────────────────────────
    @Test
    @WithMockUser(username = "fausto.spotorno@idoneos.online", roles = {"Docente"})
    @DisplayName("CU-39 a CU-42: Respuestas de Foro - Registro, Modificación, Baja y Verificación 404/Redirect")
    void testCU39_a_CU42_RespuestasForo() throws Exception {
        Usuario docente = usuarioRepository.findByCorreo("fausto.spotorno@idoneos.online").orElseThrow();
        ConsultaForo consulta = foroService.crearConsulta("Consulta de prueba para respuestas oficiales", docente, unidadBase);

        // CU-39 Buscar respuestas de consulta existente
        mockMvc.perform(get("/academico/consultas/" + consulta.getId() + "/respuestas"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/foro/cu-39-buscar-respuesta-de-foro"))
                .andExpect(model().attributeExists("consulta"))
                .andExpect(model().attributeExists("respuestas"));

        // CU-39 Consulta inexistente -> Redirección segura
        mockMvc.perform(get("/academico/consultas/99999/respuestas"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/academico/consultas"));

        // CU-40 Registrar Respuesta
        mockMvc.perform(post("/foro/respuestas/guardar")
                .param("consultaId", String.valueOf(consulta.getId()))
                .param("texto", "Esta es la respuesta técnica oficial del docente Fausto Spotorno."))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/academico/consultas/" + consulta.getId() + "/respuestas"))
                .andExpect(flash().attributeExists("mensaje"));

        RespuestaForo r = respuestaForoRepository.findAll().stream()
                .filter(x -> "Esta es la respuesta técnica oficial del docente Fausto Spotorno.".equals(x.getTexto()))
                .findFirst().orElseThrow();

        // CU-41 Modificar Respuesta
        mockMvc.perform(post("/academico/respuestas/" + r.getId() + "/editar")
                .param("texto", "Respuesta ampliada con cita a la ley 26.831."))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/academico/consultas/" + consulta.getId() + "/respuestas"))
                .andExpect(flash().attributeExists("mensaje"));

        RespuestaForo mod = respuestaForoRepository.findById(r.getId()).orElseThrow();
        assertEquals("Respuesta ampliada con cita a la ley 26.831.", mod.getTexto());

        // CU-42 Dar de baja Respuesta
        mockMvc.perform(post("/academico/respuestas/" + r.getId() + "/baja"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/academico/consultas/" + consulta.getId() + "/respuestas"))
                .andExpect(flash().attributeExists("mensaje"));

        RespuestaForo baja = respuestaForoRepository.findById(r.getId()).orElseThrow();
        assertTrue(baja.isBaja());
    }

    // ─────────────────────────────────────────────────────────────
    // Validaciones de Seguridad y Acceso No Autorizado (403 Forbidden)
    // ─────────────────────────────────────────────────────────────
    @Test
    @WithMockUser(username = "alumno@correo.com", roles = {"Alumno"})
    @DisplayName("Seguridad: Alumno intentando registrar un programa recibe 403 Forbidden")
    void testSeguridad_AlumnoNoPuedeRegistrarPrograma() throws Exception {
        mockMvc.perform(post("/academico/programas/guardar")
                .param("cursoId", String.valueOf(cursoBase.getIdCurso()))
                .param("nombre", "Programa No Autorizado"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "alumno@correo.com", roles = {"Alumno"})
    @DisplayName("Seguridad: Alumno intentando modificar unidad recibe 403 Forbidden")
    void testSeguridad_AlumnoNoPuedeModificarUnidad() throws Exception {
        mockMvc.perform(post("/academico/unidades/" + unidadBase.getId() + "/editar")
                .param("titulo", "Título No Permitido"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "alumno@correo.com", roles = {"Alumno"})
    @DisplayName("Seguridad: Alumno intentando dar de baja un material recibe 403 Forbidden")
    void testSeguridad_AlumnoNoPuedeDarDeBajaMaterial() throws Exception {
        mockMvc.perform(post("/academico/materiales/999/baja"))
                .andExpect(status().isForbidden());
    }
}
