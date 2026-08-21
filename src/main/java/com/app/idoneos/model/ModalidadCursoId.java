package com.app.idoneos.model;
import com.app.idoneos.service.Reportes.*;


import java.io.Serializable;
import java.util.Objects;

/**
 * Clase de clave compuesta para la entidad ModalidadCurso.
 * Corresponde a la PK (id_modalidad, id_curso) de la tabla "Modalidad Curso".
 */
public class ModalidadCursoId implements Serializable {

    private int idModalidad;
    private int idCurso;

    public ModalidadCursoId() {
    }

    public ModalidadCursoId(int idModalidad, int idCurso) {
        this.idModalidad = idModalidad;
        this.idCurso = idCurso;
    }

    public int getIdModalidad() {
        return idModalidad;
    }

    public void setIdModalidad(int idModalidad) {
        this.idModalidad = idModalidad;
    }

    public int getIdCurso() {
        return idCurso;
    }

    public void setIdCurso(int idCurso) {
        this.idCurso = idCurso;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ModalidadCursoId)) return false;
        ModalidadCursoId that = (ModalidadCursoId) o;
        return idModalidad == that.idModalidad && idCurso == that.idCurso;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idModalidad, idCurso);
    }
}

