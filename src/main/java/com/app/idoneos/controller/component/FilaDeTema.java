package com.app.idoneos.controller.component;

import com.app.idoneos.model.Tema;

/**
 * Un tema en el acordeón del curso que ve el docente: los del programa de la cohorte van primero, con su número de orden y sus días, y los
 * demás temas del curso (todavía sin programa o fuera de él) van después, sin número.
 */
public class FilaDeTema {

    private final Tema tema;
    private final int numeroOrden;
    private final int duracionDias;
    private final boolean enPrograma;

    public FilaDeTema(Tema tema, int numeroOrden, int duracionDias, boolean enPrograma) {
        this.tema = tema;
        this.numeroOrden = numeroOrden;
        this.duracionDias = duracionDias;
        this.enPrograma = enPrograma;
    }

    public Tema getTema() {
        return tema;
    }

    /** El número de orden en el cronograma del programa, o 0 si el tema no está en él. */
    public int getNumeroOrden() {
        return numeroOrden;
    }

    /** Los días que dura en el cronograma del programa, o 0 si el tema no está en él. */
    public int getDuracionDias() {
        return duracionDias;
    }

    public boolean isEnPrograma() {
        return enPrograma;
    }
}
