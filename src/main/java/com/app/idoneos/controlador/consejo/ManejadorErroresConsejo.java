package com.app.idoneos.controlador.consejo;

import com.app.idoneos.utilidades.MensajeValidacionUtilidad;
import java.util.Map;

import com.app.idoneos.utilidades.RespuestaJsonUtilidad;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.app.idoneos.modelo.Cohorte;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Consejo (advice) de controladores para los errores de la solicitud que ocurren antes de llegar a la lógica de cada
 * pantalla, porque los detecta el propio Spring: valores que no se pueden convertir al tipo esperado (por ejemplo,
 * un número demasiado grande), direcciones de operaciones que solo admiten POST abiertas desde el navegador y archivos
 * que superan el tamaño máximo de las subidas. Responde con un mensaje claro, en el propio formulario cuando es un
 * POST.
 */
@ControllerAdvice
public class ManejadorErroresConsejo {

    /** Mensajes de las excepciones de los casos de uso (CU-12 y CU-13) para los valores numéricos inválidos. */
    private static final Map<String, String> MENSAJES_DE_CU = Map.of(
            "cupoMaximo", MensajeValidacionUtilidad.formatoInvalido("El cupo máximo", "número entero de 1 a " + Cohorte.CUPO_MAXIMO_PERMITIDO),
            "semanasAcceso", MensajeValidacionUtilidad.formatoInvalido("Las semanas de acceso", "número entero de 1 a " + Cohorte.SEMANAS_ACCESO_MAXIMAS));

    private static final Map<String, String> NOMBRES_DE_CAMPOS = Map.ofEntries(
            Map.entry("semanasAcceso", "Semanas de acceso"),
            Map.entry("cupoMaximo", "Cupo máximo"),
            Map.entry("precio", "Precio"),
            Map.entry("categoriaId", "Categoría"),
            Map.entry("nivelId", "Nivel"),
            Map.entry("programaId", "Programa"),
            Map.entry("docenteTitularId", "Docente titular"),
            Map.entry("fechaInicioInscripcion", "Inicio de inscripción"),
            Map.entry("fechaFinInscripcion", "Fin de inscripción"),
            Map.entry("fechaInicioDictado", "Inicio de dictado"),
            Map.entry("fechaFinDictado", "Fin de dictado"));

    /**
     * Informa que un valor enviado no es válido. En los formularios (POST) responde en JSON para que el mensaje
     * se muestre en el propio formulario; en una consulta (GET) vuelve al inicio con un aviso.
     *
     * @param e                  La excepción de conversión de tipos.
     * @param solicitud          La solicitud HTTP.
     * @param redirectAttributes Atributos para mensajes de redirección.
     * @return Una respuesta HTTP 400 con el mensaje de error, o una redirección al inicio.
     */
    /** El formato que se espera según el tipo al que no se pudo convertir el valor. */
    private static String formatoDe(Class<?> tipo) {
        if (tipo == null) {
            return "valor de la lista";
        }
        if (java.time.temporal.Temporal.class.isAssignableFrom(tipo)) {
            return "fecha con el formato aaaa-mm-dd";
        }
        if (Number.class.isAssignableFrom(tipo) || tipo.isPrimitive()) {
            return Float.class.equals(tipo) || Double.class.equals(tipo) || float.class.equals(tipo) || double.class.equals(tipo)
                    ? "número, con punto para los decimales" : "número entero";
        }
        return "valor de la lista";
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Object valorInvalido(MethodArgumentTypeMismatchException e, HttpServletRequest solicitud,
            RedirectAttributes redirectAttributes) {
        String mensaje = MENSAJES_DE_CU.get(e.getName());
        if (mensaje == null) {
            String campo = NOMBRES_DE_CAMPOS.getOrDefault(e.getName(), e.getName());
            mensaje = MensajeValidacionUtilidad.formatoInvalido("El valor ingresado en «" + campo + "»", formatoDe(e.getRequiredType()));
        }
        if ("POST".equalsIgnoreCase(solicitud.getMethod())) {
            return RespuestaJsonUtilidad.respuestaConError(HttpStatus.BAD_REQUEST, mensaje);
        }
        redirectAttributes.addFlashAttribute("error", mensaje);
        return "redirect:/inicio";
    }

    /**
     * Informa que la dirección solicitada no admite ese método. Si se abrió desde el navegador (GET) una
     * operación que solo admite POST, vuelve al inicio con un aviso.
     *
     * @param e                  La excepción de método no soportado.
     * @param redirectAttributes Atributos para mensajes de redirección.
     * @return Una redirección al inicio, o una respuesta HTTP 405 con el mensaje de error.
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Object metodoNoSoportado(HttpRequestMethodNotSupportedException e, RedirectAttributes redirectAttributes) {
        if ("GET".equalsIgnoreCase(e.getMethod())) {
            redirectAttributes.addFlashAttribute("error", "Error! Esa dirección no se puede abrir directamente.");
            return "redirect:/inicio";
        }
        return RespuestaJsonUtilidad.respuestaConError(HttpStatus.METHOD_NOT_ALLOWED, "Error! La operación solicitada no está permitida.");
    }

    /**
     * Informa que el archivo supera el tamaño máximo que acepta el servidor. Ese límite lo controla Spring antes de
     * llegar a los controladores, por eso el error se atiende acá y se muestra dentro del formulario.
     *
     * @return Una respuesta HTTP 400 con el mensaje de error.
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, String>> archivoDemasiadoGrande() {
        return RespuestaJsonUtilidad.respuestaConError(HttpStatus.BAD_REQUEST, MensajeValidacionUtilidad.demasiadoGrande("El archivo", 500, "MB"));
    }
}
