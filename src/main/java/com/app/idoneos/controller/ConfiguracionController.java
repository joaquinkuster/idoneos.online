package com.app.idoneos.controller;

import java.util.HashMap;
import java.util.List;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.app.idoneos.model.Administrador;
import com.app.idoneos.model.Parametro;
import com.app.idoneos.model.Usuario;
import com.app.idoneos.service.parametro.ParametroService;
import com.app.idoneos.util.ConfiguracionUtil;
import com.app.idoneos.util.ConfiguracionUtil.Definicion;
import com.app.idoneos.util.ConfiguracionUtil.Seccion;
import com.app.idoneos.util.ManejadorArchivosUtil;
import com.app.idoneos.util.RespuestaFormularioUtil;

/** Controlador de la configuración: el administrador consulta y modifica el valor de los parámetros del sistema. */
@Controller
@RequestMapping("/configuracion")
public class ConfiguracionController {

    /** Tamaño máximo del logo, en bytes. */
    private static final long LOGO_TAMANIO_MAXIMO = 2L * 1024 * 1024;

    /** Prefijo de la dirección del logo subido desde esta pantalla. */
    private static final String RUTA_LOGO = "/uploads/institucion/";

    @Autowired
    private ParametroService parametroService;

    @Autowired
    private com.app.idoneos.service.institucion.InstitucionService institucionService;

    @Autowired
    @org.springframework.beans.factory.annotation.Qualifier("directorioCursos")
    private java.nio.file.Path directorioCursos;

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /** GET /configuracion/buscar: Muestra el formulario con todos los parámetros y su valor actual. */
    @GetMapping("/buscar")
    public String buscar(Model model) {
        Map<String, String> valores = new HashMap<>();
        Set<String> cargados = new HashSet<>();
        for (Seccion seccion : ConfiguracionUtil.SECCIONES) {
            for (Definicion definicion : seccion.parametros()) {
                Parametro parametro = parametroService.buscarPorClave(definicion.clave()).orElse(null);
                if (parametro == null) {
                    continue;
                }
                if (parametro.esSensible()) {
                    // Un secreto nunca se muestra: solo se indica si ya hay uno cargado
                    if (!parametro.getValor().isEmpty()) {
                        cargados.add(definicion.clave());
                    }
                } else {
                    valores.put(definicion.clave(), parametro.getValor());
                }
            }
        }
        model.addAttribute("secciones", ConfiguracionUtil.SECCIONES);
        model.addAttribute("valores", valores);
        model.addAttribute("cargados", cargados);
        model.addAttribute("titulo", "Configuración | Idóneos Online");
        model.addAttribute("menuActivo", "configuracion");
        return "pages/configuracion/buscar";
    }

    // ----------------------------------------------------------------------
    // Modificaciones
    // ----------------------------------------------------------------------

    /** POST /configuracion/modificar: Valida y registra los parámetros que cambiaron. Todos o ninguno. */
    @PostMapping("/modificar")
    public ResponseEntity<Map<String, String>> modificar(@RequestParam Map<String, String> campos,
            @RequestParam(value = "logoArchivo", required = false) org.springframework.web.multipart.MultipartFile logo, Authentication authentication) {
        try {
            Administrador administrador = ((Usuario) authentication.getPrincipal()).getAdministrador();
            if (administrador == null) {
                throw new IllegalArgumentException("Error! El usuario no tiene el rol de administrador.");
            }
            Map<String, String> cambios = new LinkedHashMap<>();
            for (Seccion seccion : ConfiguracionUtil.SECCIONES) {
                for (Definicion definicion : seccion.parametros()) {
                    String valor = validar(definicion, campos.get(definicion.clave()));
                    if (valor != null && cambio(definicion, valor)) {
                        cambios.put(definicion.clave(), valor);
                    }
                }
            }
            boolean hayLogo = logo != null && !logo.isEmpty();
            if (hayLogo && logo.getSize() > LOGO_TAMANIO_MAXIMO) {
                throw new IllegalArgumentException("Error! El logo no puede superar los 2 MB.");
            }
            if (cambios.isEmpty() && !hayLogo) {
                return RespuestaFormularioUtil.armarRespuestaExitosa("No hay cambios para guardar.");
            }
            if (hayLogo) {
                String anterior = parametroService.buscarPorClave(Parametro.INSTITUCION_LOGO).map(Parametro::getValor).orElse("");
                String nombre = ManejadorArchivosUtil.guardarImagen(logo, directorioCursos.resolve("institucion"), "logo-" + System.currentTimeMillis());
                cambios.put(Parametro.INSTITUCION_LOGO, RUTA_LOGO + nombre);
                if (anterior.startsWith(RUTA_LOGO)) {
                    ManejadorArchivosUtil.eliminar(anterior.substring(RUTA_LOGO.length()), directorioCursos.resolve("institucion"));
                }
            }
            cambios.forEach((clave, valor) -> parametroService.registrarTexto(administrador, clave, valor));
            institucionService.invalidar();
            return RespuestaFormularioUtil.armarRespuestaExitosa("Configuración actualizada con éxito.");
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    /** Valida el valor de un campo y lo devuelve limpio, o null si es un secreto que se deja como está. */
    private String validar(Definicion definicion, String recibido) {
        String valor = recibido == null ? "" : recibido.trim();
        if (definicion.tipo() == ConfiguracionUtil.Tipo.SECRETO) {
            return valor.isEmpty() ? null : valor;
        }
        if (definicion.tipo() == ConfiguracionUtil.Tipo.IMAGEN) {
            return null; // la imagen llega como archivo y se guarda aparte
        }
        if (valor.isEmpty()) {
            if (definicion.obligatorio()) {
                throw new IllegalArgumentException("Error! Completá el valor de «" + definicion.etiqueta() + "».");
            }
            return valor;
        }
        if (definicion.esNumerico()) {
            int numero;
            try {
                numero = Integer.parseInt(valor);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Error! «" + definicion.etiqueta() + "» debe ser un número entero.");
            }
            if (numero < definicion.minimo() || numero > definicion.maximo()) {
                throw new IllegalArgumentException("Error! «" + definicion.etiqueta() + "» debe estar entre " + definicion.minimo() + " y " + definicion.maximo() + ".");
            }
            return String.valueOf(numero);
        }
        if (definicion.largo() > 0 && valor.length() > definicion.largo()) {
            throw new IllegalArgumentException("Error! «" + definicion.etiqueta() + "» no puede superar los " + definicion.largo() + " caracteres.");
        }
        if (definicion.tipo() == ConfiguracionUtil.Tipo.CORREO && !valor.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new IllegalArgumentException("Error! «" + definicion.etiqueta() + "» no es un correo válido.");
        }
        if (Parametro.CBU_INSTITUCION.equals(definicion.clave()) && !valor.matches("\\d{22}")) {
            throw new IllegalArgumentException("Error! El CBU debe tener 22 dígitos.");
        }
        return valor;
    }

    /** Indica si el valor es distinto del que ya está guardado (los secretos siempre se consideran un cambio: se cargaron a propósito). */
    private boolean cambio(Definicion definicion, String valor) {
        if (definicion.tipo() == ConfiguracionUtil.Tipo.SECRETO) {
            return true;
        }
        return !parametroService.buscarPorClave(definicion.clave()).map(Parametro::getValor).orElse("\u0000").equals(valor);
    }
}
