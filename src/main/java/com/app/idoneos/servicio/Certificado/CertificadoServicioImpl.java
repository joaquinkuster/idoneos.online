package com.app.idoneos.servicio.Certificado;

import java.util.List;
import java.util.Optional;
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

    @Autowired
    private CertificadoRepositorio certificadoRepositorio;

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
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public Certificado modificar(Certificado entidad) {
        return certificadoRepositorio.save(entidad);
    }

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
}
