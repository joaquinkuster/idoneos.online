package com.app.idoneos.servicio.Certificado;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Alumno;
import com.app.idoneos.modelo.Certificado;
import com.app.idoneos.modelo.Inscripcion;

/**
 * Servicio para gestionar las operaciones relacionadas con el certificado.
 */
public interface CertificadoServicio {

    // ---------------------------------------------------------------- Lecturas

    /**
     * Busca los registros vigentes de certificado asociados a inscripcion.
     *
     * @param inscripcion el registro de Inscripcion asociado
     * @return una lista de Certificado asociados al Inscripcion indicado
     */
    List<Certificado> buscarPorInscripcion(Inscripcion inscripcion);

    /**
     * Busca el certificado por su atributo único 'numero'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param numero el valor de 'numero' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<Certificado> buscarPorNumero(String numero);

    // ---------------------------------------------------------------- Creaciones

    /**
     * Emite el certificado de finalización de una inscripción que completó todas las unidades del programa: genera su
     * número (CERT-año-secuencia) y su fecha de emisión y lo registra. El envío por correo se simula (queda registrado
     * en el log) hasta que exista el servicio de correo. Si la inscripción ya tiene un certificado vigente, lo devuelve.
     *
     * @param inscripcion la inscripción que completó el curso
     * @return el certificado vigente de la inscripción
     */
    Certificado emitirCertificado(Inscripcion inscripcion);

    // ---------------------------------------------------------------- Modificaciones

    /**
     * Corrige el nombre y/o el DNI del alumno registrados en un certificado emitido de una inscripción activa
     * (CU-48), ante un error de tipeo.
     *
     * @param idCertificado el certificado
     * @param nombre el nombre completo del alumno
     * @param dni el DNI del alumno (7 u 8 dígitos)
     * @return el certificado corregido
     * @throws IllegalArgumentException si falta algún dato, el DNI no es válido, el certificado está anulado o la
     *         inscripción está dada de baja
     */
    Certificado corregirDatos(Integer idCertificado, String nombre, String dni);

    // ---------------------------------------------------------------- Bajas

    /**
     * Anula los certificados vigentes de una inscripción (por ejemplo, cuando se revierte una unidad del curso por
     * fraude y el curso deja de estar completo).
     *
     * @param inscripcion la inscripción
     * @return {@code true} si había algún certificado vigente
     */
    boolean anularCertificados(Inscripcion inscripcion);

    /**
     * Busca los certificados vigentes de un alumno (los de todos sus cursos), del más reciente al más antiguo.
     *
     * @param alumno el alumno
     * @return sus certificados vigentes
     */
    List<Certificado> buscarVigentesDelAlumno(Alumno alumno);

    /**
     * Busca un certificado vigente para mostrarlo: el alumno solo puede ver los suyos y el administrador (se indica
     * {@code null}) cualquiera.
     *
     * @param idCertificado el identificador del certificado
     * @param solicitante   el alumno que lo pide, o {@code null} si lo pide el administrador
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
