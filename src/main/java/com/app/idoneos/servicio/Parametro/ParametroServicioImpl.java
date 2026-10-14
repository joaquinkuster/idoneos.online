package com.app.idoneos.servicio.Parametro;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Administrador;
import com.app.idoneos.modelo.Parametro;
import com.app.idoneos.repositorio.ParametroRepositorio;
import com.app.idoneos.servicio.CrudServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link Parametro}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link ParametroServicio}.
 */
@Service
public class ParametroServicioImpl implements ParametroServicio, CrudServicio<Parametro> {

    /** Prefijo con el que se guarda el valor cifrado de un parámetro sensible. */
    private static final String PREFIJO_CIFRADO = "cifrado:";

    /** Sal de la clave de cifrado (en hexadecimal): fija, porque la clave de cifrado ya es el secreto. */
    private static final String SAL = new String(Hex.encode("idoneos.online.parametros".getBytes(StandardCharsets.UTF_8)));

    @Autowired
    private ParametroRepositorio parametroRepositorio;

    /**
     * Clave con la que se cifran los parámetros sensibles (AES-256). En producción se define con la variable de entorno
     * {@code IDONEOS_SEGURIDAD_CLAVE_CIFRADO}; el valor por defecto es solo para desarrollo. Si cambia, los parámetros
     * cifrados con la anterior dejan de poder leerse y hay que volver a cargarlos.
     */
    @Value("${idoneos.seguridad.clave-cifrado:idoneos-online-clave-de-cifrado-de-desarrollo}")
    private String claveDeCifrado;

    private volatile TextEncryptor cifrador;

    // ---------------------------------------------------------------- Lecturas

    /**
     * Busca el parámetro por su identificador. Devuelve el registro si existe.
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<Parametro> buscarPorId(Integer id) {
        return parametroRepositorio.findById(id);
    }

    /**
     * Obtiene todos los registros. Devuelve todos los registros.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<Parametro> obtenerTodo() {
        return parametroRepositorio.findAll();
    }

    /**
     * Verifica si existe un registro con el identificador dado.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return parametroRepositorio.existsById(id);
    }

    /**
     * Busca los registros de parametro asociados a administrador.
     *
     * @param administrador el registro de Administrador asociado
     * @return una lista de Parametro asociados al Administrador indicado
     */
    @Override
    public List<Parametro> buscarPorAdministrador(Administrador administrador) {
        return parametroRepositorio.findByAdministrador(administrador);
    }

    /**
     * Busca el parámetro por su atributo único 'clave'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param clave el valor de 'clave' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    @Override
    public Optional<Parametro> buscarPorClave(String clave) {
        return parametroRepositorio.findByClave(clave);
    }

    /**
     * Obtiene el valor entero de un parámetro del sistema.
     *
     * @param clave la clave del parámetro
     * @param valorPorDefecto el valor a devolver si el parámetro no existe o no es un entero
     * @return el valor entero del parámetro
     */
    @Override
    public int obtenerEntero(String clave, int valorPorDefecto) {
        return parametroRepositorio.findByClave(clave).map(parametro -> {
            try {
                return Integer.parseInt(parametro.getValor().trim());
            } catch (NumberFormatException e) {
                return valorPorDefecto;
            }
        }).orElse(valorPorDefecto);
    }

    @Override
    public Optional<String> obtenerTexto(String clave) {
        return parametroRepositorio.findByClave(clave).map(this::valorLegible);
    }

    @Override
    public float obtenerDecimal(String clave, float valorPorDefecto) {
        return parametroRepositorio.findByClave(clave).map(parametro -> {
            try {
                return Float.parseFloat(parametro.getValor().trim().replace(',', '.'));
            } catch (NumberFormatException e) {
                return valorPorDefecto;
            }
        }).orElse(valorPorDefecto);
    }

    // ---------------------------------------------------------------- Creaciones

    /**
     * Guarda el parámetro en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public Parametro guardar(Parametro entidad) {
        return parametroRepositorio.save(entidad);
    }

    // ---------------------------------------------------------------- Modificaciones

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public Parametro modificar(Parametro entidad) {
        return parametroRepositorio.save(entidad);
    }

    @Override
    @Transactional
    public Parametro registrarTexto(Administrador administrador, String clave, String valor) {
        if (clave == null || clave.isBlank()) {
            throw new IllegalArgumentException("Error! La clave del parámetro es obligatoria.");
        }
        if (valor == null) {
            throw new IllegalArgumentException("Error! El valor del parámetro «" + clave + "» es obligatorio.");
        }
        String almacenado = Parametro.esClaveSensible(clave) && !valor.isEmpty()
                ? PREFIJO_CIFRADO + cifrador().encrypt(valor) : valor;
        Parametro parametro = parametroRepositorio.findByClave(clave).orElse(null);
        if (parametro == null) {
            parametro = new Parametro(administrador, clave, almacenado);
        } else {
            parametro.setValor(almacenado);
            parametro.setAdministrador(administrador);
            parametro.setUltimaModificacion(LocalDateTime.now());
        }
        return parametroRepositorio.save(parametro);
    }

    // ---------------------------------------------------------------- Bajas

    /**
     * Elimina el registro de la base de datos. Esta entidad no tiene baja lógica.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(Parametro entidad) {
        parametroRepositorio.delete(entidad);
    }

    private TextEncryptor cifrador() {
        if (cifrador == null) {
            cifrador = Encryptors.delux(claveDeCifrado, SAL);
        }
        return cifrador;
    }

    /**
     * El valor de un parámetro como lo usa el sistema: descifrado si es sensible y está cifrado. Un valor sensible sin
     * el prefijo de cifrado (cargado a mano en la base) se toma tal cual.
     */
    private String valorLegible(Parametro parametro) {
        String valor = parametro.getValor();
        if (!parametro.esSensible() || !valor.startsWith(PREFIJO_CIFRADO)) {
            return valor;
        }
        try {
            return cifrador().decrypt(valor.substring(PREFIJO_CIFRADO.length()));
        } catch (RuntimeException e) {
            throw new IllegalStateException("No se pudo descifrar el parámetro «" + parametro.getClave()
                    + "»: la clave de cifrado no es la que se usó al guardarlo", e);
        }
    }
}
