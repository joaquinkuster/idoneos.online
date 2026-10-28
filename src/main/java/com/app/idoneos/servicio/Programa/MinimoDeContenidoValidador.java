package com.app.idoneos.servicio.Programa;

import java.util.Collection;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.app.idoneos.modelo.Parametro;
import com.app.idoneos.modelo.Programa;
import com.app.idoneos.modelo.Tema;
import com.app.idoneos.servicio.Parametro.ParametroServicio;
import com.app.idoneos.utilidades.MensajeErrorUtilidad;

/**
 * Regla del mínimo de contenido de un programa: mientras tenga cohortes en curso (aunque no tengan inscripciones), debe
 * conservar al menos el mínimo de temas con material publicado y autoevaluación que se exige para registrar una cohorte
 * (parámetro {@code programa.minimo_temas_con_material}). Las operaciones que le quitan contenido (ocultar o dar de baja
 * materiales, quitar temas del cronograma) consultan acá si el programa se mantiene por encima del mínimo.
 */
@Component
public class MinimoDeContenidoValidador {

    private static final int MINIMO_POR_DEFECTO = 1;

    /**
     * Un programa que quedaría por debajo del mínimo.
     *
     * @param programa el programa afectado
     * @param minimo   el mínimo de temas con material publicado y autoevaluación que exige el sistema
     * @param quedaria la cantidad de temas que conservaría
     */
    public record Bloqueo(Programa programa, int minimo, int quedaria) {

        /** El mínimo expresado en temas, por ejemplo "2 temas". */
        public String minimoEnTemas() {
            return MensajeErrorUtilidad.cantidad(minimo, "tema", "temas");
        }
    }

    @Autowired
    private ParametroServicio parametroServicio;

    /** El mínimo de temas con material publicado y autoevaluación que exige el sistema. */
    public int minimo() {
        return parametroServicio.obtenerEntero(Parametro.MINIMO_TEMAS_CON_MATERIAL, MINIMO_POR_DEFECTO);
    }

    /**
     * Indica si una operación dejaría al programa por debajo del mínimo. Solo cuenta si el programa tiene cohortes en
     * curso y si la operación es la que le quita contenido: un programa que ya estaba por debajo y no empeora no se bloquea.
     *
     * @param programa           El programa que se verifica.
     * @param materialesQuitados Los materiales que dejarían de estar publicados.
     * @param temasQuitadas   Los temas que dejarían de formar parte del cronograma.
     * @return El bloqueo, o vacío si el programa se mantiene por encima del mínimo.
     */
    public Optional<Bloqueo> verificar(Programa programa, Collection<Integer> materialesQuitados,
            Collection<Integer> temasQuitadas) {
        if (!programa.tieneCohortesEnCurso()) {
            return Optional.empty();
        }
        int minimo = minimo();
        int antes = programa.getCantidadTemasConMaterialPublicado();
        int despues = programa.cantidadTemasConMaterialPublicadoSin(materialesQuitados, temasQuitadas);
        if (despues >= antes || despues >= minimo) {
            return Optional.empty();
        }
        return Optional.of(new Bloqueo(programa, minimo, despues));
    }

    /**
     * Indica si quitar temas del cronograma dejaría al programa con cohortes en curso con menos temas que el mínimo.
     * Cuenta solo temas del cronograma, sin mirar su contenido. Si el programa ya estaba por debajo y la operación no
     * empeora, no se bloquea.
     *
     * @param programa         El programa que se verifica.
     * @param temasQuitadas Los temas que dejarían de formar parte del cronograma.
     * @return El bloqueo (con la cantidad de temas que quedarían), o vacío si el programa conserva el mínimo.
     */
    public Optional<Bloqueo> verificarTemas(Programa programa, Collection<Integer> temasQuitadas) {
        if (!programa.tieneCohortesEnCurso()) {
            return Optional.empty();
        }
        int minimo = minimo();
        int antes = programa.getCantidadTemas();
        int despues = (int) programa.getCronogramaOrdenado().stream()
                .filter(cronograma -> !temasQuitadas.contains(cronograma.getTema().getIdTema())).count();
        return despues >= antes || despues >= minimo ? Optional.empty() : Optional.of(new Bloqueo(programa, minimo, despues));
    }

    /**
     * Motivo (sin punto final) por el que no se puede quitar el material de un tema, con la misma oración que usan las
     * autoevaluaciones: es el único visible del tema y su programa tiene cohortes en curso.
     *
     * @param tema             El tema del material.
     * @param materialesQuitados Los materiales que dejarían de estar publicados en la operación.
     * @return El motivo, o vacío si el programa se mantiene por encima del mínimo.
     */
    public Optional<String> motivoParaElMaterial(Tema tema, Collection<Integer> materialesQuitados) {
        for (Programa programa : tema.getProgramasQueLaIncluyen()) {
            Optional<Bloqueo> bloqueo = verificar(programa, materialesQuitados, java.util.Set.of());
            if (bloqueo.isPresent()) {
                Tema afectada = programa.temasQueDejanDeContarSin(materialesQuitados).stream().findFirst().orElse(tema);
                return Optional.of("es el único visible del tema «" + afectada.getTitulo()
                        + "» y su programa tiene cohortes en curso");
            }
        }
        return Optional.empty();
    }
}
