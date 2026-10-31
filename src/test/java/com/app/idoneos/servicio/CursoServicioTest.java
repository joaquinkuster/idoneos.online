package com.app.idoneos.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.app.idoneos.modelo.Cohorte;
import com.app.idoneos.modelo.Curso;
import com.app.idoneos.modelo.Docente;
import com.app.idoneos.modelo.Material;
import com.app.idoneos.modelo.Modalidad;
import com.app.idoneos.modelo.TipoMaterial;
import com.app.idoneos.modelo.DocenteCurso;
import com.app.idoneos.servicio.Categoria.CategoriaServicioImpl;
import com.app.idoneos.servicio.Curso.CursoServicioImpl;
import com.app.idoneos.servicio.Docente.DocenteServicioImpl;
import com.app.idoneos.servicio.Modalidad.ModalidadServicioImpl;
import com.app.idoneos.servicio.Nivel.NivelServicioImpl;

/**
 * Pruebas de las reglas de negocio de los cursos (CU-01 a CU-06), sobre los datos de la semilla.
 * Cada prueba se ejecuta en una transacción que se revierte al finalizar.
 */
@SpringBootTest
@Transactional
class CursoServicioTest {

    private static final String FAUSTO = "fausto.spotorno@idoneos.online";
    private static final String SEBASTIAN = "sebastian.bordato@idoneos.online";
    private static final String MARIANO = "mariano.otalora@idoneos.online";

    @Autowired
    private CursoServicioImpl cursoServicio;

    @Autowired
    private CategoriaServicioImpl categoriaServicio;

    @Autowired
    private NivelServicioImpl nivelServicio;

    @Autowired
    private ModalidadServicioImpl modalidadServicio;

    @Autowired
    private DocenteServicioImpl docenteServicio;

    @Autowired
    private com.app.idoneos.repositorio.MaterialRepositorio materialRepositorio;

    @Autowired
    private com.app.idoneos.repositorio.TipoMaterialRepositorio tipoMaterialRepositorio;

    private Integer idDocente(String correo) {
        return docenteServicio.buscarHabilitados().stream()
                .filter(docente -> docente.getUsuario().getCorreo().equals(correo)).findFirst().orElseThrow()
                .getIdDocente();
    }

    private Integer idModalidad(String nombre) {
        return modalidadServicio.obtenerTodo().stream().filter(m -> m.getNombre().equals(nombre)).findFirst()
                .orElseThrow().getIdModalidad();
    }

    private Integer idCategoria(String nombre) {
        return categoriaServicio.obtenerTodo().stream().filter(c -> c.getNombre().equals(nombre)).findFirst()
                .orElseThrow().getIdCategoria();
    }

    private Integer idNivel(String nombre) {
        return nivelServicio.obtenerTodo().stream().filter(n -> n.getNombre().equals(nombre)).findFirst().orElseThrow()
                .getIdNivel();
    }

    private Curso curso(String nombre) {
        return cursoServicio.buscarPorNombre(nombre).orElseThrow();
    }

    @Test
    @DisplayName("CU-03: registra un curso con sus modalidades y su equipo docente")
    void registrarCursoConDatosValidos() {
        Curso curso = cursoServicio.registrarCurso("Curso de Prueba", "Descripción", 1000f, 600, 90, null,
                idCategoria("Mercado de Capitales"), idNivel("Básico"), true,
                List.of(idModalidad(Modalidad.GRABADA), idModalidad(Modalidad.EN_VIVO)), idDocente(FAUSTO),
                List.of(idDocente(SEBASTIAN), idDocente(MARIANO)));

        assertEquals("Curso de Prueba", curso.getNombre());
        assertEquals(2, curso.getModalidades().size());
        assertEquals(FAUSTO, curso.getDocenteTitular().getUsuario().getCorreo());
        assertEquals(List.of(SEBASTIAN, MARIANO),
                curso.getDocentesAyudantes().stream().map(d -> d.getUsuario().getCorreo()).toList());
        assertFalse(curso.esInactivo());
    }

    @Test
    @DisplayName("CU-03: la duración es obligatoria y tiene un máximo")
    void registrarCursoConDuracionInvalida() {
        List<Integer> modalidades = List.of(idModalidad(Modalidad.GRABADA));
        IllegalArgumentException falta = assertThrows(IllegalArgumentException.class,
                () -> cursoServicio.registrarCurso("Curso Nuevo", null, 10f, 600, null, null, idCategoria("Macroeconomía"),
                        idNivel("Básico"), false, modalidades, idDocente(FAUSTO), null));
        assertTrue(falta.getMessage().contains("duración"), falta.getMessage());

        IllegalArgumentException cero = assertThrows(IllegalArgumentException.class,
                () -> cursoServicio.registrarCurso("Curso Nuevo", null, 10f, 600, 0, null, idCategoria("Macroeconomía"),
                        idNivel("Básico"), false, modalidades, idDocente(FAUSTO), null));
        assertTrue(cero.getMessage().contains("La duración no es válida"), cero.getMessage());

        // el acceso de una cohorte incluye la duración del curso: no puede superar el máximo de acceso
        IllegalArgumentException duracion = assertThrows(IllegalArgumentException.class,
                () -> cursoServicio.registrarCurso("Curso Nuevo", null, 10f, 600, Cohorte.DIAS_ACCESO_MAXIMOS + 1, null,
                        idCategoria("Macroeconomía"), idNivel("Básico"), false, modalidades, idDocente(FAUSTO), null));
        assertTrue(duracion.getMessage().contains("La duración no es válida"), duracion.getMessage());
    }

    @Test
    @DisplayName("CU-03: el nivel y la carga horaria son opcionales al registrar: los completa el docente")
    void registrarCursoSinNivelNiCargaHoraria() {
        Curso curso = cursoServicio.registrarCurso("Curso Sin Nivel", null, 10f, null, 28, null, idCategoria("Macroeconomía"),
                null, true, List.of(idModalidad(Modalidad.GRABADA)), idDocente(FAUSTO), null);

        assertNull(curso.getNivel());
        assertNull(curso.getCargaHorariaMinutos());
        assertEquals(28, curso.getDuracionDias());
        assertEquals(List.of("nivel", "objetivos", "carga horaria"), curso.getFaltantesDeDatosAcademicos());
    }

    @Test
    @DisplayName("El docente completa los datos académicos del curso: nivel, descripción, objetivos y carga horaria")
    void completarDatosDelCurso() {
        Curso curso = cursoServicio.registrarCurso("Curso Con Datos", null, 10f, null, 28, null, idCategoria("Macroeconomía"),
                null, true, List.of(idModalidad(Modalidad.GRABADA)), idDocente(FAUSTO), null);
        Docente titular = curso.getDocenteTitular();

        // la carga horaria es obligatoria porque el curso emite certificado
        IllegalArgumentException sinCarga = assertThrows(IllegalArgumentException.class,
                () -> cursoServicio.completarDatosDelCurso(curso.getIdCurso(), titular, idNivel("Básico"), "Descripción", "Objetivos", null));
        assertTrue(sinCarga.getMessage().contains("carga horaria"), sinCarga.getMessage());
        assertThrows(IllegalArgumentException.class,
                () -> cursoServicio.completarDatosDelCurso(curso.getIdCurso(), titular, null, null, "Objetivos", 600));
        assertThrows(IllegalArgumentException.class,
                () -> cursoServicio.completarDatosDelCurso(curso.getIdCurso(), titular, idNivel("Básico"), null, " ", 600));

        Curso completo = cursoServicio.completarDatosDelCurso(curso.getIdCurso(), titular, idNivel("Básico"), "Descripción",
                "Objetivos del curso", 600);

        assertEquals("Objetivos del curso", completo.getObjetivos());
        assertEquals(600, completo.getCargaHorariaMinutos());
        assertTrue(completo.tieneDatosAcademicosCompletos());
    }

    @Test
    @DisplayName("CU-03: la carga horaria no puede ser mayor al tiempo de la duración del curso (24 horas por día)")
    void registrarCursoConCargaHorariaMayorALaDuracion() {
        List<Integer> modalidades = List.of(idModalidad(Modalidad.GRABADA));
        // 25 horas en 1 día no entran
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> cursoServicio.registrarCurso("Curso Nuevo", null, 10f, 25 * 60, 1, null, idCategoria("Macroeconomía"),
                        idNivel("Básico"), false, modalidades, idDocente(FAUSTO), null));
        assertEquals("Error! La carga horaria no puede ser mayor al tiempo de la duración del curso.", error.getMessage());

        // 24 horas en 1 día sí
        Curso curso = cursoServicio.registrarCurso("Curso Nuevo", null, 10f, 24 * 60, 1, null, idCategoria("Macroeconomía"),
                idNivel("Básico"), false, modalidades, idDocente(FAUSTO), null);
        assertEquals(24 * 60, curso.getCargaHorariaMinutos());

        // al modificar, subir la carga horaria por encima de lo que admite la duración también se rechaza
        assertThrows(IllegalArgumentException.class,
                () -> cursoServicio.modificarCurso(curso.getIdCurso(), curso.getNombre(), curso.getDescripcion(), curso.getPrecio(),
                        25 * 60, 1, null, idCategoria("Macroeconomía"), idNivel("Básico"), false, modalidades,
                        idDocente(FAUSTO), null));
    }

    @Test
    @DisplayName("CU-04: la duración no puede quedar por debajo del cronograma de un programa del curso")
    void modificarCursoNoPermiteDuracionMenorAlCronograma() {
        Curso curso = curso("Macroeconomía de Coyuntura");
        int cronograma = curso.getProgramaVigente().getDuracionTotalDias();
        assertTrue(cronograma > 1);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> cursoServicio.modificarCurso(curso.getIdCurso(), curso.getNombre(), curso.getDescripcion(),
                        curso.getPrecio(), curso.getCargaHorariaMinutos(), cronograma - 1, null,
                        curso.getCategoria().getIdCategoria(), curso.getNivel().getIdNivel(), curso.getEmiteCertificado(),
                        curso.getModalidades().stream().map(Modalidad::getIdModalidad).toList(),
                        curso.getDocenteTitular().getIdDocente(),
                        curso.getDocentesAyudantes().stream().map(Docente::getIdDocente).toList()));
        assertTrue(error.getMessage().contains("no puede ser menor a la duración del cronograma"), error.getMessage());

        // justo en la duración del cronograma, se guarda
        Curso modificado = cursoServicio.modificarCurso(curso.getIdCurso(), curso.getNombre(), curso.getDescripcion(),
                curso.getPrecio(), curso.getCargaHorariaMinutos(), cronograma, null, curso.getCategoria().getIdCategoria(),
                curso.getNivel().getIdNivel(), curso.getEmiteCertificado(),
                curso.getModalidades().stream().map(Modalidad::getIdModalidad).toList(),
                curso.getDocenteTitular().getIdDocente(),
                curso.getDocentesAyudantes().stream().map(Docente::getIdDocente).toList());
        assertEquals(cronograma, modificado.getDuracionDias());
    }

    @Test
    @DisplayName("CU-04: con inscripciones activas no se puede modificar la duración")
    void modificarCursoConInscripcionesNoPermiteCambiarLaDuracion() {
        Curso curso = curso("Mercado de Capitales Argentino");
        assertTrue(cursoServicio.tieneInscripcionesActivas(curso));

        assertThrows(IllegalArgumentException.class,
                () -> cursoServicio.modificarCurso(curso.getIdCurso(), curso.getNombre(), curso.getDescripcion(),
                        curso.getPrecio(), curso.getCargaHorariaMinutos(), curso.getDuracionDias() + 1, null,
                        curso.getCategoria().getIdCategoria(), curso.getNivel().getIdNivel(), curso.getEmiteCertificado(),
                        curso.getModalidades().stream().map(Modalidad::getIdModalidad).toList(),
                        curso.getDocenteTitular().getIdDocente(),
                        curso.getDocentesAyudantes().stream().map(Docente::getIdDocente).toList()));
    }

    @Test
    @DisplayName("CU-03: informa los campos obligatorios que faltan")
    void registrarCursoSinCamposObligatorios() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> cursoServicio.registrarCurso("", null, null, null, null, null, null, null, false, null, null, null));
        assertTrue(error.getMessage().contains("nombre"));
        assertTrue(error.getMessage().contains("docente titular"));
    }

    @Test
    @DisplayName("CU-03: rechaza un precio negativo")
    void registrarCursoConPrecioNegativo() {
        assertThrows(IllegalArgumentException.class,
                () -> cursoServicio.registrarCurso("Curso Nuevo", null, -1f, 600, 90, null, idCategoria("Macroeconomía"),
                        idNivel("Básico"), false, List.of(idModalidad(Modalidad.GRABADA)), idDocente(FAUSTO), null));
    }

    @Test
    @DisplayName("CU-03: el docente titular no puede ser también ayudante")
    void registrarCursoConTitularYAyudanteIguales() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> cursoServicio.registrarCurso("Curso Nuevo", null, 10f, 600, 90, null, idCategoria("Macroeconomía"),
                        idNivel("Básico"), false, List.of(idModalidad(Modalidad.GRABADA)), idDocente(FAUSTO),
                        List.of(idDocente(MARIANO), idDocente(FAUSTO))));
        assertTrue(error.getMessage().contains("titular"));
    }

    @Test
    @DisplayName("CU-03: rechaza un precio superior al máximo permitido")
    void registrarCursoConPrecioSuperiorAlMaximo() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> cursoServicio.registrarCurso("Curso Nuevo", null, Curso.PRECIO_MAXIMO + 1, 600, 90, null,
                        idCategoria("Macroeconomía"), idNivel("Básico"), false,
                        List.of(idModalidad(Modalidad.GRABADA)), idDocente(FAUSTO), null));
        assertTrue(error.getMessage().contains("10.000.000"));
    }

    @Test
    @DisplayName("CU-03: acepta el precio máximo permitido")
    void registrarCursoConElPrecioMaximo() {
        Curso curso = cursoServicio.registrarCurso("Curso Caro", null, Curso.PRECIO_MAXIMO, 600, 90, null,
                idCategoria("Macroeconomía"), idNivel("Básico"), false, List.of(idModalidad(Modalidad.GRABADA)),
                idDocente(FAUSTO), null);
        assertEquals(Curso.PRECIO_MAXIMO, curso.getPrecio());
    }

    @Test
    @DisplayName("CU-03: un docente repetido entre los ayudantes se registra una sola vez")
    void registrarCursoConAyudantesRepetidos() {
        Curso curso = cursoServicio.registrarCurso("Curso Nuevo", null, 10f, 600, 90, null, idCategoria("Macroeconomía"),
                idNivel("Básico"), false, List.of(idModalidad(Modalidad.GRABADA)), idDocente(FAUSTO),
                List.of(idDocente(MARIANO), idDocente(MARIANO)));
        assertEquals(1, curso.getDocentesAyudantes().size());
    }

    @Test
    @DisplayName("CU-04: con inscripciones activas solo se puede modificar el precio, el equipo docente y la imagen")
    void modificarCursoConInscripcionesNoPermiteCambiarElNombre() {
        Curso curso = curso("Mercado de Capitales Argentino");
        assertTrue(cursoServicio.tieneInscripcionesActivas(curso));

        assertThrows(IllegalArgumentException.class,
                () -> cursoServicio.modificarCurso(curso.getIdCurso(), "Otro nombre", curso.getDescripcion(), 1f, curso.getCargaHorariaMinutos(), curso.getDuracionDias(), null,
                        curso.getCategoria().getIdCategoria(), curso.getNivel().getIdNivel(),
                        curso.getEmiteCertificado(),
                        curso.getModalidades().stream().map(Modalidad::getIdModalidad).toList(), idDocente(FAUSTO),
                        List.of(idDocente(SEBASTIAN), idDocente(MARIANO))));
    }

    @Test
    @DisplayName("CU-04: con inscripciones activas se puede modificar el precio")
    void modificarCursoConInscripcionesPermiteCambiarElPrecio() {
        Curso curso = curso("Mercado de Capitales Argentino");

        Curso modificado = cursoServicio.modificarCurso(curso.getIdCurso(), curso.getNombre(), curso.getDescripcion(),
                175000f, curso.getCargaHorariaMinutos(), curso.getDuracionDias(), null, curso.getCategoria().getIdCategoria(), curso.getNivel().getIdNivel(),
                curso.getEmiteCertificado(), curso.getModalidades().stream().map(Modalidad::getIdModalidad).toList(),
                idDocente(FAUSTO), List.of(idDocente(SEBASTIAN), idDocente(MARIANO)));

        assertEquals(175000f, modificado.getPrecio());
        assertTrue(modificado.getUltimaModificacion() != null);
    }

    @Test
    @DisplayName("CU-04: no se puede desvincular a un docente con clases o material activo en el curso")
    void modificarCursoNoPermiteDesvincularDocenteConMaterial() {
        Curso curso = curso("Mercado de Capitales Argentino");

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> cursoServicio.modificarCurso(curso.getIdCurso(), curso.getNombre(), curso.getDescripcion(), 1f, curso.getCargaHorariaMinutos(), curso.getDuracionDias(), null, curso.getCategoria().getIdCategoria(), curso.getNivel().getIdNivel(),
                        curso.getEmiteCertificado(),
                        curso.getModalidades().stream().map(Modalidad::getIdModalidad).toList(), idDocente(SEBASTIAN),
                        null));
        assertTrue(error.getMessage().contains("Fausto"));
    }

    @Test
    @DisplayName("CU-04: no se puede quitar la modalidad En vivo si el curso tiene cohortes con fechas de dictado")
    void noSePuedeQuitarEnVivoConCohortesConDictado() {
        Curso curso = curso("CEDEARs: Invertir en Empresas del Mundo");
        assertFalse(curso.getCohortesConFechasDeDictado().isEmpty());

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> modificarModalidades(curso, List.of(idModalidad(Modalidad.GRABADA))));
        assertTrue(error.getMessage().startsWith("Error! No puede quitarse la modalidad 'En vivo': el curso tiene "),
                error.getMessage());
        assertTrue(error.getMessage().contains("con fechas de dictado"), error.getMessage());
    }

    @Test
    @DisplayName("CU-04: no se puede quitar la modalidad Grabada si el curso tiene grabaciones publicadas")
    void noSePuedeQuitarGrabadaConGrabacionesPublicadas() {
        Curso curso = curso("Fondos Comunes de Inversión");
        var tema = curso.getTemas().stream().filter(u -> !u.getBaja()).findFirst().orElseThrow();
        var participacion = curso.getParticipacionesDocente().stream().findFirst().orElseThrow();
        var grabacion = tipoMaterialRepositorio.findByNombre(TipoMaterial.GRABACION).orElseThrow();
        var material = materialRepositorio.save(new Material(participacion, grabacion, tema, "Grabación de prueba"));
        tema.getMateriales().add(material);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> modificarModalidades(curso, List.of(idModalidad(Modalidad.CLON_IA))));
        assertEquals("Error! No puede quitarse la modalidad 'Grabada': el curso tiene 1 grabación publicada. "
                + "Dé de baja u ocultá ese material antes.", error.getMessage());

        // Oculta, ya no impide quitar la modalidad
        material.setOculto(true);
        assertEquals(1, modificarModalidades(curso, List.of(idModalidad(Modalidad.CLON_IA))).getModalidades().size());
    }

    private Curso modificarModalidades(Curso curso, List<Integer> idsModalidades) {
        return cursoServicio.modificarCurso(curso.getIdCurso(), curso.getNombre(), curso.getDescripcion(),
                curso.getPrecio(), curso.getCargaHorariaMinutos(), curso.getDuracionDias(), null, curso.getCategoria().getIdCategoria(), curso.getNivel().getIdNivel(),
                curso.getEmiteCertificado(), idsModalidades, curso.getDocenteTitular().getIdDocente(),
                curso.getDocentesAyudantes().stream().map(Docente::getIdDocente).toList());
    }

    @Test
    @DisplayName("CU-04: sin inscripciones se pueden modificar todos los datos y el equipo docente")
    void modificarCursoSinInscripciones() {
        Curso curso = curso("Introducción a las Finanzas");

        Curso modificado = cursoServicio.modificarCurso(curso.getIdCurso(), "Introducción a las Finanzas II", "Nueva",
                50000f, 300, 120, null, idCategoria("Macroeconomía"), idNivel("Avanzado"), true,
                List.of(idModalidad(Modalidad.EN_VIVO)), idDocente(MARIANO), List.of(idDocente(FAUSTO), idDocente(SEBASTIAN)));

        assertEquals("Introducción a las Finanzas II", modificado.getNombre());
        assertEquals(MARIANO, modificado.getDocenteTitular().getUsuario().getCorreo());
        assertEquals(2, modificado.getDocentesAyudantes().size());
        assertTrue(modificado.getDocentesAyudantes().stream().anyMatch(d -> d.getUsuario().getCorreo().equals(FAUSTO)));
        assertTrue(modificado.incluyeModalidad(Modalidad.EN_VIVO));
        assertFalse(modificado.incluyeModalidad(Modalidad.GRABADA));
    }

    @Test
    @DisplayName("CU-05: no se puede dar de baja un curso con programas o temas activos")
    void darDeBajaCursoConProgramasActivos() {
        Curso curso = curso("Macroeconomía de Coyuntura");

        assertThrows(IllegalArgumentException.class, () -> cursoServicio.darDeBajaCurso(curso.getIdCurso()));
    }

    @Test
    @DisplayName("CU-05: da de baja un curso sin dependencias y deja de estar activo")
    void darDeBajaCursoSinDependencias() {
        Curso curso = curso("Introducción a las Finanzas");

        cursoServicio.darDeBajaCurso(curso.getIdCurso());

        assertTrue(curso(curso.getNombre()).esInactivo());
        assertThrows(IllegalArgumentException.class, () -> cursoServicio.darDeBajaCurso(curso.getIdCurso()));
    }

    @Test
    @DisplayName("CU-06: el catálogo solo incluye cursos activos con cohortes con inscripción abierta")
    void catalogoSoloConCohortesAbiertas() {
        List<String> nombres = cursoServicio.buscarEnCatalogo(null, null, null, null, null).stream()
                .map(Curso::getNombre).toList();

        assertTrue(nombres.contains("Mercado de Capitales Argentino"));
        assertTrue(nombres.contains("Macroeconomía de Coyuntura"));
        assertFalse(nombres.contains("Análisis Técnico Bursátil"), "su cohorte todavía no abrió la inscripción");
        assertFalse(nombres.contains("Introducción a las Finanzas"), "no tiene cohortes");
        assertFalse(nombres.contains("Economía Argentina 2024"), "está dado de baja");
    }

    @Test
    @DisplayName("CU-01: el docente solo ve los cursos en los que participa")
    void buscarCursosRestringidosAlDocente() {
        var mariano = docenteServicio.buscarHabilitados().stream()
                .filter(d -> d.getUsuario().getCorreo().equals(MARIANO)).findFirst().orElseThrow();

        List<String> nombres = cursoServicio.buscarConFiltros(null, null, null, null, null, "nombre", mariano).stream()
                .map(Curso::getNombre).toList();

        assertTrue(nombres.contains("Finanzas Personales e Inversión"));
        assertTrue(nombres.contains("Análisis Técnico Bursátil"));
        assertFalse(nombres.contains("Macroeconomía de Coyuntura"));
    }

    @Test
    @DisplayName("CU-01: ordena alfabéticamente por defecto y los dados de baja siempre al final")
    void buscarCursosOrdenadosPorNombre() {
        List<String> nombres = cursoServicio.buscarConFiltros(null, null, null, null, null, "nombre", null).stream()
                .map(Curso::getNombre).toList();

        assertEquals("Economía Argentina 2024", nombres.get(nombres.size() - 1), "el curso dado de baja va al final");
        List<String> vigentes = nombres.subList(0, nombres.size() - 1);
        assertEquals(vigentes.stream().sorted(String.CASE_INSENSITIVE_ORDER).toList(), vigentes);
    }

    @Test
    @DisplayName("CU-01: puede ordenar por los más recientes y los dados de baja siguen al final")
    void buscarCursosOrdenadosPorMasRecientes() {
        List<Curso> cursos = cursoServicio.buscarConFiltros(null, null, null, null, null, "recientes", null);
        List<Curso> vigentes = cursos.stream().filter(curso -> !curso.esInactivo()).toList();

        assertTrue(cursos.get(cursos.size() - 1).esInactivo());
        for (int i = 1; i < vigentes.size(); i++) {
            assertTrue(vigentes.get(i - 1).getIdCurso() > vigentes.get(i).getIdCurso(),
                    "el curso más reciente va primero");
        }
    }

    private Docente docente(String correo) {
        return docenteServicio.buscarHabilitados().stream()
                .filter(d -> d.getUsuario().getCorreo().equals(correo)).findFirst().orElseThrow();
    }

    @Test
    @DisplayName("CU-27: el docente accede a un curso en el que participa, con su programa y su cohorte de trabajo")
    void docenteAccedeAsuCurso() {
        Curso curso = curso("Mercado de Capitales Argentino");

        DocenteCurso participacion = cursoServicio.validarAccesoDocente(curso.getIdCurso(), docente(FAUSTO));

        assertEquals(curso.getIdCurso(), participacion.getCurso().getIdCurso());
        assertEquals("Programa 2026", participacion.getProgramaDeTrabajo().getNombre());
        assertEquals("En dictado", participacion.getCohorteDeTrabajo().getEstado());
    }

    @Test
    @DisplayName("CU-27: el docente no accede a un curso en el que no participa")
    void docenteAjenoAlCurso() {
        Curso curso = curso("Macroeconomía de Coyuntura");

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> cursoServicio.validarAccesoDocente(curso.getIdCurso(), docente(SEBASTIAN)));

        assertTrue(error.getMessage().contains("No participás"));
    }

    @Test
    @DisplayName("CU-27: el docente accede a un curso sin programas (ahí registra el primero), pero no a uno dado de baja")
    void cursoNoDisponible() {
        Curso sinPrograma = curso("Introducción a las Finanzas");
        DocenteCurso participacion = cursoServicio.validarAccesoDocente(sinPrograma.getIdCurso(), docente(SEBASTIAN));
        assertNull(participacion.getProgramaDeTrabajo());
        assertNull(participacion.getCohorteDeTrabajo());
        IllegalArgumentException error;

        int idDeBaja = curso("Economía Argentina 2024").getIdCurso();
        error = assertThrows(IllegalArgumentException.class,
                () -> cursoServicio.validarAccesoDocente(idDeBaja, docente(FAUSTO)));
        assertTrue(error.getMessage().contains("no se encuentra activo"));
    }

    @Test
    @DisplayName("Los destacados del inicio van del curso con más inscripciones históricas al que tiene menos, hasta el límite")
    void destacadosPorInscripciones() {
        List<Curso> destacados = cursoServicio.buscarDestacados(8);

        assertEquals(8, destacados.size());
        for (int i = 1; i < destacados.size(); i++) {
            assertTrue(cursoServicio.contarInscripcionesHistoricas(destacados.get(i - 1))
                    >= cursoServicio.contarInscripcionesHistoricas(destacados.get(i)));
        }
        // "Mercado de Capitales Argentino" es el que más alumnos inscriptos acumula en la semilla
        assertEquals("Mercado de Capitales Argentino", destacados.get(0).getNombre());
        assertTrue(cursoServicio.contarInscripcionesHistoricas(destacados.get(0)) > 0);
        assertEquals(3, cursoServicio.buscarDestacados(3).size());
    }
}
