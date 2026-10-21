package com.app.idoneos.servicio.Certificado;

import com.app.idoneos.utilidades.MensajeValidacionUtilidad;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;
import com.app.idoneos.modelo.Certificado;
import com.app.idoneos.modelo.Inscripcion;
import com.app.idoneos.repositorio.CertificadoRepositorio;
import com.app.idoneos.servicio.CrudServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link Certificado}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link CertificadoServicio}.
 */
@Service
public class CertificadoServicioImpl implements CertificadoServicio, CrudServicio<Certificado> {

    /** Longitud máxima del nombre (la de la columna). */
    private static final int LONGITUD_NOMBRE = 100;

    @Autowired
    private CertificadoRepositorio certificadoRepositorio;

    // ---------------------------------------------------------------- Lecturas

    /**
     * Busca el certificado por su identificador. Incluye los registros dados de baja o anulados, para poder consultarlos.
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<Certificado> buscarPorId(Integer id) {
        return certificadoRepositorio.findById(id);
    }

    /**
     * Obtiene todos los registros. Solo devuelve certificados vigentes (no anulados).
     *
     * @return Una lista de registros.
     */
    @Override
    public List<Certificado> obtenerTodo() {
        return certificadoRepositorio.findByAnuladoFalse();
    }

    /**
     * Verifica si existe un registro con el identificador dado.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return certificadoRepositorio.existsById(id);
    }

    /**
     * Busca los registros vigentes de certificado asociados a inscripcion.
     *
     * @param inscripcion el registro de Inscripcion asociado
     * @return una lista de Certificado asociados al Inscripcion indicado
     */
    @Override
    public List<Certificado> buscarPorInscripcion(Inscripcion inscripcion) {
        return certificadoRepositorio.findByInscripcionAndAnuladoFalse(inscripcion);
    }

    /**
     * Busca el certificado por su atributo único 'numero'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param numero el valor de 'numero' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    @Override
    public Optional<Certificado> buscarPorNumero(String numero) {
        return certificadoRepositorio.findByNumero(numero);
    }

    // ---------------------------------------------------------------- Creaciones

    /**
     * Guarda el certificado en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public Certificado guardar(Certificado entidad) {
        return certificadoRepositorio.save(entidad);
    }

    // ---------------------------------------------------------------- Modificaciones

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public Certificado modificar(Certificado entidad) {
        return certificadoRepositorio.save(entidad);
    }

    @Override
    @Transactional
    public Certificado corregirDatos(Integer idCertificado, String nombre, String dni) {
        Certificado certificado = certificadoRepositorio.findById(idCertificado)
                .filter(c -> !c.estaAnulado() && !c.getInscripcion().esInactivo())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Error! El certificado no se encuentra vigente o su inscripción está dada de baja."));
        if (nombre == null || nombre.isBlank() || dni == null || dni.isBlank()) {
            throw new IllegalArgumentException("Error! El nombre y el DNI del alumno son obligatorios.");
        }
        String nombreLimpio = nombre.trim();
        String dniLimpio = dni.trim();
        if (nombreLimpio.length() > LONGITUD_NOMBRE) {
            throw new IllegalArgumentException(MensajeValidacionUtilidad.demasiadoLargo("El nombre", LONGITUD_NOMBRE, "caracteres"));
        }
        if (!dniLimpio.matches("\\d{7,8}")) {
            throw new IllegalArgumentException(MensajeValidacionUtilidad.formatoInvalido("El DNI", "solo números, 7 u 8 dígitos"));
        }
        certificado.setNombreAlumno(nombreLimpio);
        certificado.setDniAlumno(dniLimpio);
        certificado.setUltimaModificacion(LocalDateTime.now());
        return certificadoRepositorio.save(certificado);
    }

    // ---------------------------------------------------------------- Bajas

    /**
     * Anula el certificado, en lugar de eliminarlo de la base de datos.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(Certificado entidad) {
        entidad.anular(); // Anulación en lugar de borrado
        certificadoRepositorio.save(entidad);
    }
}
