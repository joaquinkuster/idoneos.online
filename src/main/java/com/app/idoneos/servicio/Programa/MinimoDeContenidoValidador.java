package com.app.idoneos.servicio.Programa;

import java.util.Collection;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.app.idoneos.modelo.Parametro;
import com.app.idoneos.modelo.Programa;
import com.app.idoneos.modelo.Unidad;
import com.app.idoneos.servicio.Parametro.ParametroServicio;
import com.app.idoneos.utilidades.MensajeErrorUtilidad;

/**
 * Regla del mínimo de contenido de un programa: mientras tenga cohortes en curso (aunque no tengan inscripciones), debe
 * conservar al menos el mínimo de unidades con material publicado y autoevaluación que se exige para registrar una cohorte
 * (parámetro {@code programa.minimo_unidades_con_material}). Las operaciones que le quitan contenido (ocultar o dar de baja
 * materiales, quitar unidades del cronograma) consultan acá si el programa se mantiene por encima del mínimo.
 */
@Component
public class MinimoDeContenidoValidador {

    private static final int MINIMO_POR_DEFECTO = 1;

    /**
     * Un programa que quedaría por debajo del mínimo.
     *
     * @param programa el programa afectado
     * @param minimo   el mínimo de unidades con material publicado y autoevaluación que exige el sistema
     * @param quedaria la cantidad de unidades que conservaría
     */
    public record Bloqueo(Programa programa, int minimo, int quedaria) {

        /** El mínimo expresado en unidades, por ejemplo "2 unidades". */
        public String minimoEnUnidades() {
            return MensajeErrorUtilidad.cantidad(minimo, "unidad", "unidades");
        }
    }

    @Autowired
    private ParametroServicio parametroServicio;

    /** El mínimo de unidades con material publicado y autoevaluación que exige el sistema. */
    public int minimo() {
        return parametroServicio.obtenerEntero(Parametro.MINIMO_UNIDADES_CON_MATERIAL, MINIMO_POR_DEFECTO);
    }

    /**
     * Indica si una operación dejaría al programa por debajo del mínimo. Solo cuenta si el programa tiene cohortes en
     * curso y si la operación es la que le quita contenido: un programa que ya estaba por debajo y no empeora no se bloquea.
     *
     * @param programa           El programa que se verifica.
     * @param materialesQuitados Los materiales que dejarían de estar publicados.
     * @param unidadesQuitadas   Las unidades que dejarían de formar parte del cronograma.
     * @return El bloqueo, o vacío si el programa se mantiene por encima del mínimo.
     */
    public Optional<Bloqueo> verificar(Programa programa, Collection<Integer> materialesQuitados,
            Collection<Integer> unidadesQuitadas) {
        if (!programa.tieneCohortesEnCurso()) {
            return Optional.empty();
        }
        int minimo = minimo();
        int antes = programa.getCantidadUnidadesConMaterialPublicado();
        int despues = programa.cantidadUnidadesConMaterialPublicadoSin(materialesQuitados, unidadesQuitadas);
        if (despues >= antes || despues >= minimo) {
            return Optional.empty();
        }
        return Optional.of(new Bloqueo(programa, minimo, despues));
    }

    /**
     * Indica si quitar unidades del cronograma dejaría al programa con cohortes en curso con menos unidades que el mínimo.
     * Cuenta solo unidades del cronograma, sin mirar su contenido. Si el programa ya estaba por debajo y la operación no
     * empeora, no se bloquea.
     *
     * @param programa         El programa que se verifica.
     * @param unidadesQuitadas Las unidades que dejarían de formar parte del cronograma.
     * @return El bloqueo (con la cantidad de unidades que quedarían), o vacío si el programa conserva el mínimo.
     */
    public Optional<Bloqueo> verificarUnidades(Programa programa, Collection<Integer> unidadesQuitadas) {
        if (!programa.tieneCohortesEnCurso()) {
            return Optional.empty();
        }
        int minimo = minimo();
        int antes = programa.getCantidadUnidades();
        int despues = (int) programa.getCronogramaOrdenado().stream()
                .filter(cronograma -> !unidadesQuitadas.contains(cronograma.getUnidad().getIdUnidad())).count();
        return despues >= antes || despues >= minimo ? Optional.empty() : Optional.of(new Bloqueo(programa, minimo, despues));
    }

    /**
     * Motivo (sin punto final) por el que no se puede quitar el material de una unidad, con la misma oración que usan las
     * autoevaluaciones: es el único visible de la unidad y su programa tiene cohortes en curso.
     *
     * @param unidad             La unidad del material.
     * @param materialesQuitados Los materiales que dejarían de estar publicados en la operación.
     * @return El motivo, o vacío si el programa se mantiene por encima del mínimo.
     */
    public Optional<String> motivoParaElMaterial(Unidad unidad, Collection<Integer> materialesQuitados) {
        for (Programa programa : unidad.getProgramasQueLaIncluyen()) {
            Optional<Bloqueo> bloqueo = verificar(programa, materialesQuitados, java.util.Set.of());
            if (bloqueo.isPresent()) {
                Unidad afectada = programa.unidadesQueDejanDeContarSin(materialesQuitados).stream().findFirst().orElse(unidad);
                return Optional.of("es el único visible de la unidad «" + afectada.getTitulo()
                        + "» y su programa tiene cohortes en curso");
            }
        }
        return Optional.empty();
    }
}
