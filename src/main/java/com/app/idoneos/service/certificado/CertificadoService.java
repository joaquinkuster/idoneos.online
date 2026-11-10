package com.app.idoneos.service.certificado;

import com.app.idoneos.service.CrudService;

import com.app.idoneos.model.Alumno;
import com.app.idoneos.model.Certificado;
import com.app.idoneos.model.Inscripcion;
import java.util.List;
import java.util.Optional;

/** Servicio del certificado. */
public interface CertificadoService extends CrudService<Certificado> {

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /**
     * Busca el certificado por su atributo único 'numero'.
     *
     * @param numero el valor de 'numero' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<Certificado> buscarPorNumero(String numero);

    // ----------------------------------------------------------------------
    // Creaciones
    // ----------------------------------------------------------------------

    /**
     * Emite el certificado de finalización de una inscripción que completó todos los temas del programa.
     *
     * @param inscripcion la inscripción que completó el curso
     * @return el certificado vigente de la inscripción
     */
    Certificado emitir(Inscripcion inscripcion);

    // ----------------------------------------------------------------------
    // Modificaciones
    // ----------------------------------------------------------------------

    /**
     * Corrige el nombre y/o el DNI del alumno registrados en un certificado emitido de una inscripción activa, ante un error de tipeo.
     *
     * @param idCertificado el certificado
     * @param nombre el nombre completo del alumno
     * @param dni el DNI del alumno (7 u 8 dígitos)
     * @return el certificado corregido
     * @throws IllegalArgumentException si falta algún dato, el DNI no es válido, el certificado está anulado o la inscripción está dada de baja
     */
    Certificado corregirDatos(Integer idCertificado, String nombre, String dni);

    // ----------------------------------------------------------------------
    // Bajas
    // ----------------------------------------------------------------------

    /**
     * Anula los certificados vigentes de una inscripción.
     *
     * @param inscripcion la inscripción
     * @return {@code true} si había algún certificado vigente
     */
    boolean anular(Inscripcion inscripcion);

    /**
     * Busca los certificados vigentes de un alumno (los de todos sus cursos), del más reciente al más antiguo.
     *
     * @param alumno el alumno
     * @return sus certificados vigentes
     */
    List<Certificado> buscarVigentesDelAlumno(Alumno alumno);

    /**
     * Busca un certificado vigente para mostrarlo.
     *
     * @param idCertificado el identificador del certificado
     * @param solicitante el alumno que lo pide, o {@code null} si lo pide el administrador
     * @return el certificado
     * @throws IllegalArgumentException si no existe, está anulado o no es del alumno
     */
    Certificado buscarVisible(Integer idCertificado, Alumno solicitante);

    /**
     * Genera el archivo PDF de un certificado, para que el alumno o el administrador lo descarguen.
     *
     * @param certificado el certificado emitido
     * @return el contenido del archivo PDF
     */
    byte[] generarPdf(Certificado certificado);
}
