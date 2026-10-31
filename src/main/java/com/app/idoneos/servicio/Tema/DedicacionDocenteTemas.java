package com.app.idoneos.servicio.Tema;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.app.idoneos.modelo.Curso;
import com.app.idoneos.modelo.Tema;
import com.app.idoneos.repositorio.TemaRepositorio;
import com.app.idoneos.utilidades.DuracionUtilidad;
import com.app.idoneos.utilidades.MensajeErrorUtilidad;
import com.app.idoneos.utilidades.RepartoUtilidad;
import com.app.idoneos.utilidades.RespuestaFormularioUtilidad;

/**
 * Reparto de la dedicación docente de un curso entre sus temas. La suma de los minutos de los temas vigentes es siempre igual
 * a la dedicación docente del curso: el primer tema recibe toda la dedicación; al agregar un tema entra con una parte pareja y el
 * resto se reajusta en proporción; al quitarlo o al cambiar la dedicación del curso, el total se vuelve a repartir; y al
 * cambiar los minutos de un tema, la diferencia la absorben los demás.
 * <p>
 * La dedicación docente es el trabajo que reconoce la empresa y no algo que vea o pague el alumno: por eso se reajusta aunque
 * algún tema pertenezca a un programa con inscripciones activas.
 */
@Component
public class DedicacionDocenteTemas {

    @Autowired
    private TemaRepositorio temaRepositorio;

    /**
     * Asigna la dedicación docente de un tema nuevo (todavía no guardado) y reajusta la de los demás temas del curso.
     *
     * @param curso     El curso del tema.
     * @param nuevo     El tema nuevo; recibe su dedicación docente.
     * @param solicitada Los minutos que se piden para el tema, o {@code null} para una parte pareja.
     */
    public void asignarATemaNuevo(Curso curso, Tema nuevo, Integer solicitada) {
        List<Tema> existentes = temaRepositorio.findByCursoAndBajaFalse(curso);
        int total = curso.getDedicacionDocenteMinutos();
        if (existentes.isEmpty()) {
            if (solicitada != null && solicitada != total) {
                throw RespuestaFormularioUtilidad.campoInvalido("dedicacionDocente", "Error! La dedicación docente debe ser la del curso completo ("
                        + DuracionUtilidad.formatearMinutos(total) + ") porque es el primer tema.");
            }
            nuevo.setDedicacionDocenteMinutos(total);
            return;
        }
        int disponible = total;
        int parteMaxima = disponible - existentes.size();
        if (parteMaxima < 1) {
            throw new IllegalArgumentException("Error! La dedicación docente (" + DuracionUtilidad.formatearMinutos(total)
                    + ") no alcanza para sumar otro tema.");
        }
        int parte;
        if (solicitada != null) {
            if (solicitada < 1 || solicitada > parteMaxima) {
                throw RespuestaFormularioUtilidad.campoInvalido("dedicacionDocente", MensajeErrorUtilidad.formatoInvalido("La dedicación docente",
                        "mayor a cero y hasta " + DuracionUtilidad.formatearMinutos(parteMaxima)));
            }
            parte = solicitada;
        } else {
            parte = Curso.dedicacionPorDefectoDeTemaNuevo(total, existentes.size());
        }
        nuevo.setDedicacionDocenteMinutos(parte);
        repartir(existentes, disponible - parte);
    }

    /**
     * Cambia los minutos de un tema y reajusta, en proporción, los de los demás temas que se pueden ajustar.
     *
     * @param tema  El tema que se modifica (sin inscripciones activas).
     * @param nueva Los minutos nuevos del tema.
     */
    public void cambiarDedicacionDeTema(Tema tema, int nueva) {
        List<Tema> otros = new ArrayList<>(temaRepositorio.findByCursoAndBajaFalse(tema.getCurso()));
        otros.removeIf(t -> t.getIdTema() == tema.getIdTema());
        int disponible = tema.getCurso().getDedicacionDocenteMinutos();
        if (otros.isEmpty()) {
            if (nueva != disponible) {
                throw RespuestaFormularioUtilidad.campoInvalido("dedicacionDocente", "Error! La dedicación docente debe ser de "
                        + DuracionUtilidad.formatearMinutos(disponible) + " en este tema: no hay otros temas que se puedan ajustar.");
            }
            tema.setDedicacionDocenteMinutos(nueva);
            return;
        }
        int maxima = disponible - otros.size();
        if (nueva < 1 || nueva > maxima) {
            throw RespuestaFormularioUtilidad.campoInvalido("dedicacionDocente", MensajeErrorUtilidad.formatoInvalido("La dedicación docente",
                    "mayor a cero y hasta " + DuracionUtilidad.formatearMinutos(maxima)));
        }
        tema.setDedicacionDocenteMinutos(nueva);
        repartir(otros, disponible - nueva);
    }

    /**
     * Vuelve a repartir la dedicación docente del curso entre sus temas, en proporción a lo que tiene cada uno. Se usa al quitar
     * un tema o al cambiar la dedicación docente del curso.
     *
     * @param curso El curso cuya dedicacion se reparte.
     */
    public void redistribuir(Curso curso) {
        List<Tema> vigentes = temaRepositorio.findByCursoAndBajaFalse(curso);
        if (vigentes.isEmpty()) {
            return;
        }
        if (curso.getDedicacionDocenteMinutos() < vigentes.size()) {
            throw new IllegalArgumentException("Error! La dedicación docente (" + DuracionUtilidad.formatearMinutos(
                    curso.getDedicacionDocenteMinutos()) + ") no alcanza para repartir entre sus temas.");
        }
        repartir(vigentes, curso.getDedicacionDocenteMinutos());
    }

    /**
     * Verifica que una dedicación docente alcance para darle al menos un minuto a cada tema vigente del curso.
     *
     * @param curso El curso.
     * @param dedicacion La dedicación docente que se quiere para el curso, en minutos.
     */
    public void validarDedicacionDelCurso(Curso curso, int dedicacion) {
        int temas = temaRepositorio.findByCursoAndBajaFalse(curso).size();
        if (temas > 0 && dedicacion < temas) {
            throw RespuestaFormularioUtilidad.campoInvalido("dedicacionDocente", "Error! La dedicación docente no puede ser menor a "
                    + DuracionUtilidad.formatearMinutos(temas) + ": es lo que necesitan los " + temas + " temas del curso.");
        }
    }

    private void repartir(List<Tema> temas, int total) {
        int[] partes = RepartoUtilidad.repartirProporcional(total, temas.stream().map(Tema::getDedicacionDocenteMinutos).toList());
        for (int i = 0; i < temas.size(); i++) {
            if (temas.get(i).getDedicacionDocenteMinutos() != partes[i]) {
                temas.get(i).setDedicacionDocenteMinutos(partes[i]);
                temaRepositorio.save(temas.get(i));
            }
        }
    }
}
