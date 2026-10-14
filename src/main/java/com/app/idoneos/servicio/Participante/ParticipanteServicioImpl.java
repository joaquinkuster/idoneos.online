package com.app.idoneos.servicio.Participante;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.app.idoneos.modelo.Cohorte;
import com.app.idoneos.modelo.Curso;
import com.app.idoneos.modelo.Inscripcion;
import com.app.idoneos.modelo.Participante;
import com.app.idoneos.modelo.Programa;
import com.app.idoneos.utilidades.TextoUtilidad;

/**
 * Implementación del servicio de consulta de participantes.
 */
@Service
public class ParticipanteServicioImpl implements ParticipanteServicio {

    // ---------------------------------------------------------------- Lecturas

    @Override
    public List<Participante> buscarParticipantes(Curso curso, String nombreCompleto, String rol) {
        List<Participante> alumnos = new ArrayList<>();
        for (Programa programa : curso.getProgramasActivos()) {
            for (Cohorte cohorte : programa.getCohortesActivas()) {
                alumnos.addAll(alumnosDe(cohorte));
            }
        }
        return filtrar(unir(equipoDocente(curso), alumnos), nombreCompleto, rol);
    }

    @Override
    public List<Participante> buscarParticipantesPorCohorte(Cohorte cohorte, String nombreCompleto, String rol) {
        return filtrar(unir(equipoDocente(cohorte.getPrograma().getCurso()), alumnosDe(cohorte)), nombreCompleto, rol);
    }

    /** El equipo docente vigente del curso, el titular primero. */
    private List<Participante> equipoDocente(Curso curso) {
        return curso.getEquipoDocente().stream()
                .sorted(Comparator.comparing((com.app.idoneos.modelo.ParticipacionDocente p) -> !p.getEsTitular()))
                .map(p -> new Participante(p.getDocente().getUsuario(),
                        p.getEsTitular() ? Participante.DOCENTE_TITULAR : Participante.DOCENTE_AYUDANTE, null))
                .toList();
    }

    /** Los alumnos con inscripción vigente en la cohorte. */
    private List<Participante> alumnosDe(Cohorte cohorte) {
        return cohorte.getInscripciones().stream().filter(inscripcion -> !inscripcion.esInactivo())
                .map((Inscripcion inscripcion) -> new Participante(inscripcion.getAlumno().getUsuario(),
                        Participante.ALUMNO, cohorte))
                .toList();
    }

    /** Une el equipo docente y los alumnos (estos ordenados por apellido y nombre). */
    private List<Participante> unir(List<Participante> docentes, List<Participante> alumnos) {
        List<Participante> todos = new ArrayList<>(docentes);
        alumnos.stream().sorted(Comparator
                .comparing((Participante p) -> TextoUtilidad.normalizar(p.getApellido()))
                .thenComparing(p -> TextoUtilidad.normalizar(p.getNombre()))).forEach(todos::add);
        return todos;
    }

    private List<Participante> filtrar(List<Participante> participantes, String nombreCompleto, String rol) {
        String buscado = TextoUtilidad.normalizar(nombreCompleto);
        String rolBuscado = TextoUtilidad.normalizar(rol);
        return participantes.stream()
                .filter(p -> buscado.isEmpty()
                        || TextoUtilidad.normalizar(p.getNombre() + " " + p.getApellido()).contains(buscado))
                .filter(p -> rolBuscado.isEmpty() || TextoUtilidad.normalizar(p.getRol()).startsWith(rolBuscado))
                .toList();
    }
}
