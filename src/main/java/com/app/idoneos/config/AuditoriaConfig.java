package com.app.idoneos.config;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DelegatingDataSource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.app.idoneos.model.Usuario;
import com.app.idoneos.util.DispositivoUtil;

/**
 * Configuración de la auditoría. Los disparadores de la base registran las acciones de los usuarios; para saber quién las hizo, cada conexión que se pide
 * al pool lleva, como variables de la sesión de la base, el usuario con sesión iniciada y su IP. Sin sesión (tareas programadas, el aviso de pago de MODO)
 * las variables quedan vacías y no se registra nada.
 */
@Configuration
public class AuditoriaConfig {

    private static final Logger REGISTRO = LoggerFactory.getLogger(AuditoriaConfig.class);

    /** Envuelve el origen de datos para pasar el usuario y la IP a la base al pedir cada conexión. */
    @Bean
    public static BeanPostProcessor origenDeDatosAuditado() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String nombre) {
                if (bean instanceof DataSource origen && !(bean instanceof OrigenAuditado)) {
                    return new OrigenAuditado(origen);
                }
                return bean;
            }
        };
    }

    /** Origen de datos que informa a la base quién es el usuario de la conexión. */
    static final class OrigenAuditado extends DelegatingDataSource {

        /** Si la base no admite las variables de sesión (por ejemplo H2 en las pruebas), se deja de intentar. */
        private volatile boolean admite = true;

        OrigenAuditado(DataSource destino) {
            super(destino);
        }

        @Override
        public Connection getConnection() throws SQLException {
            return informar(super.getConnection());
        }

        @Override
        public Connection getConnection(String usuario, String clave) throws SQLException {
            return informar(super.getConnection(usuario, clave));
        }

        private Connection informar(Connection conexion) {
            if (!admite) {
                return conexion;
            }
            String idUsuario = "";
            String ip = "";
            Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
            if (autenticacion != null && autenticacion.getPrincipal() instanceof Usuario usuario) {
                idUsuario = String.valueOf(usuario.getIdUsuario());
                if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes atributos) {
                    ip = DispositivoUtil.obtenerIp(atributos.getRequest());
                }
            }
            try (PreparedStatement sentencia = conexion.prepareStatement("SELECT set_config('idoneos.usuario', ?, false), set_config('idoneos.ip', ?, false)")) {
                sentencia.setString(1, idUsuario);
                sentencia.setString(2, ip == null ? "" : ip);
                sentencia.execute();
            } catch (SQLException e) {
                admite = false;
                REGISTRO.warn("La base de datos no admite variables de sesión: no se registra la auditoría ({})", e.getMessage());
            }
            return conexion;
        }
    }
}
