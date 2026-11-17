package com.app.idoneos.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientPropertiesMapper;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Component;

import com.app.idoneos.model.Parametro;
import com.app.idoneos.service.parametro.ParametroService;

/**
 * Cliente de Google OAuth del sistema. Las credenciales las configura el administrador (parámetros {@code google.client_id} y {@code google.client_secret}) y se
 * leen en cada ingreso, así que un cambio rige sin reiniciar. Si en la configuración no están cargadas, se usan las de {@code application.properties}.
 */
@Component
public class GoogleClienteConfig implements ClientRegistrationRepository {

    private static final Logger REGISTRO = LoggerFactory.getLogger(GoogleClienteConfig.class);

    /** Identificador del cliente de Google en las direcciones de ingreso ({@code /oauth2/authorization/google}). */
    private static final String ID_GOOGLE = "google";

    @Autowired
    private ParametroService parametroService;

    @Autowired
    private Environment entorno;

    /** Busca el cliente de Google: el de la configuración del sistema o, si no está cargado, el de las propiedades de la aplicación. */
    @Override
    public ClientRegistration findByRegistrationId(String idRegistro) {
        if (!ID_GOOGLE.equals(idRegistro)) {
            return null;
        }
        try {
            String id = parametroService.obtenerTexto(Parametro.GOOGLE_CLIENT_ID).map(String::trim).orElse("");
            String secreto = parametroService.obtenerTexto(Parametro.GOOGLE_CLIENT_SECRET).map(String::trim).orElse("");
            if (!id.isEmpty() && !secreto.isEmpty()) {
                return CommonOAuth2Provider.GOOGLE.getBuilder(ID_GOOGLE).clientId(id).clientSecret(secreto).scope("email", "profile").build();
            }
        } catch (RuntimeException e) {
            REGISTRO.warn("No se pudieron leer las credenciales de Google de la configuración: se usan las de la aplicación ({})", e.getMessage());
        }
        OAuth2ClientProperties propiedades = Binder.get(entorno).bind("spring.security.oauth2.client", OAuth2ClientProperties.class)
                .orElseGet(OAuth2ClientProperties::new);
        return new OAuth2ClientPropertiesMapper(propiedades).asClientRegistrations().get(ID_GOOGLE);
    }
}
