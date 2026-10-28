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
 * Reparto de la carga horaria de un curso entre sus temas. La suma de los minutos de los temas vigentes es siempre igual
 * a la carga horaria del curso: el primer tema recibe toda la carga; al agregar un tema entra con una parte pareja y el
 * resto se reajusta en proporción; al quitarlo o al cambiar la carga del curso, el total se vuelve a repartir; y al
 * cambiar los minutos de un tema, la diferencia la absorben los demás.
 * <p>
 * La carga horaria es el trabajo que reconoce la empresa y no algo que vea o pague el alumno: por eso se reajusta aunque
 * algún tema pertenezca a un programa con inscripciones activas.
 */
@Component
public class CargaHorariaTemas {

    @Autowired
    private TemaRepositorio temaRepositorio;

    /**
     * Asigna la carga horaria de un tema nuevo (todavía no guardado) y reajusta la de los demás temas del curso.
     *
     * @param curso     El curso del tema.
     * @param nuevo     El tema nuevo; recibe su carga horaria.
     * @param solicitada Los minutos que se piden para el tema, o {@code null} para una parte pareja.
     */
    public void asignarATemaNuevo(Curso curso, Tema nuevo, Integer solicitada) {
        List<Tema> existentes = temaRepositorio.findByCursoAndBajaFalse(curso);
        int total = curso.getCargaHorariaMinutos();
        if (existentes.isEmpty()) {
            if (solicitada != null && solicitada != total) {
                throw RespuestaFormularioUtilidad.campoInvalido("cargaHoraria", "Error! El primer tema recibe toda la carga horaria del curso ("
                        + DuracionUtilidad.formatearMinutos(total) + ").");
            }
            nuevo.setCargaHorariaMinutos(total);
            return;
        }
        int disponible = total;
        int parteMaxima = disponible - existentes.size();
        if (parteMaxima < 1) {
            throw new IllegalArgumentException("Error! La carga horaria del curso (" + DuracionUtilidad.formatearMinutos(total)
                    + ") no alcanza para sumar otro tema.");
        }
        int parte;
        if (solicitada != null) {
            if (solicitada < 1 || solicitada > parteMaxima) {
                throw RespuestaFormularioUtilidad.campoInvalido("cargaHoraria", MensajeErrorUtilidad.formatoInvalido("La carga horaria",
                        "mayor a cero y hasta " + DuracionUtilidad.formatearMinutos(parteMaxima)));
            }
            parte = solicitada;
        } else {
            parte = Math.min(Math.max(1, total / (existentes.size() + 1)), parteMaxima);
        }
        nuevo.setCargaHorariaMinutos(parte);
        repartir(existentes, disponible - parte);
    }

    /**
     * Cambia los minutos de un tema y reajusta, en proporción, los de los demás temas que se pueden ajustar.
     *
     * @param tema  El tema que se modifica (sin inscripciones activas).
     * @param nueva Los minutos nuevos del tema.
     */
    public void cambiarCargaDeTema(Tema tema, int nueva) {
        List<Tema> otros = new ArrayList<>(temaRepositorio.findByCursoAndBajaFalse(tema.getCurso()));
        otros.removeIf(t -> t.getIdTema() == tema.getIdTema());
        int disponible = tema.getCurso().getCargaHorariaMinutos();
        if (otros.isEmpty()) {
            if (nueva != disponible) {
                throw RespuestaFormularioUtilidad.campoInvalido("cargaHoraria", "Error! No hay otros temas que se puedan ajustar: este debe llevar los "
                        + DuracionUtilidad.formatearMinutos(disponible) + " que le corresponden.");
            }
            tema.setCargaHorariaMinutos(nueva);
            return;
        }
        int maxima = disponible - otros.size();
        if (nueva < 1 || nueva > maxima) {
            throw RespuestaFormularioUtilidad.campoInvalido("cargaHoraria", MensajeErrorUtilidad.formatoInvalido("La carga horaria",
                    "mayor a cero y hasta " + DuracionUtilidad.formatearMinutos(maxima)));
        }
        tema.setCargaHorariaMinutos(nueva);
        repartir(otros, disponible - nueva);
    }

    /**
     * Vuelve a repartir la carga horaria del curso entre sus temas, en proporción a lo que tiene cada uno. Se usa al quitar
     * un tema o al cambiar la carga horaria del curso.
     *
     * @param curso El curso cuya carga se reparte.
     */
    public void redistribuir(Curso curso) {
        List<Tema> vigentes = temaRepositorio.findByCursoAndBajaFalse(curso);
        if (vigentes.isEmpty()) {
            return;
        }
        if (curso.getCargaHorariaMinutos() < vigentes.size()) {
            throw new IllegalArgumentException("Error! La carga horaria del curso (" + DuracionUtilidad.formatearMinutos(
                    curso.getCargaHorariaMinutos()) + ") no alcanza para repartir entre sus temas.");
        }
        repartir(vigentes, curso.getCargaHorariaMinutos());
    }

    /**
     * Verifica que una carga horaria alcance para darle al menos un minuto a cada tema vigente del curso.
     *
     * @param curso El curso.
     * @param carga La carga horaria que se quiere para el curso, en minutos.
     */
    public void validarCargaDelCurso(Curso curso, int carga) {
        int temas = temaRepositorio.findByCursoAndBajaFalse(curso).size();
        if (temas > 0 && carga < temas) {
            throw RespuestaFormularioUtilidad.campoInvalido("cargaHoraria", "Error! La carga horaria no puede ser menor a "
                    + DuracionUtilidad.formatearMinutos(temas) + ": es lo que necesitan los " + temas + " temas del curso.");
        }
    }

    private void repartir(List<Tema> temas, int total) {
        int[] partes = RepartoUtilidad.repartirProporcional(total, temas.stream().map(Tema::getCargaHorariaMinutos).toList());
        for (int i = 0; i < temas.size(); i++) {
            if (temas.get(i).getCargaHorariaMinutos() != partes[i]) {
                temas.get(i).setCargaHorariaMinutos(partes[i]);
                temaRepositorio.save(temas.get(i));
            }
        }
    }
}
