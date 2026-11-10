package com.app.idoneos.controller.advice;

import com.app.idoneos.util.MensajeErrorUtil;
import java.util.Map;

import com.app.idoneos.util.RespuestaFormularioUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.app.idoneos.model.Cohorte;

import jakarta.servlet.http.HttpServletRequest;

/** Consejo (advice) de controladores para los errores de la solicitud que ocurren antes de llegar a la lógica de cada pantalla, porque los detecta el propio Spring: valores que no se pueden convertir al tipo esperado (por ejemplo, un número demasiado grande), direcciones de operaciones que solo admiten POST abiertas desde el navegador y archivos que superan el tamaño máximo de las subidas. */
@ControllerAdvice
public class ManejadorErroresAdvice {

    /** Mensajes de las excepciones de los casos de uso para los valores numéricos inválidos. */
    private static final Map<String, String> MENSAJES_DE_CU = Map.of(
            "cupoMaximo", MensajeErrorUtil.armarFormatoInvalido("El cupo máximo", "número entero de 1 a " + Cohorte.CUPO_MAXIMO_PERMITIDO),
            "accesoDias", MensajeErrorUtil.armarFormatoInvalido("Los días de acceso", "número entero de 1 a " + Cohorte.DIAS_ACCESO_MAXIMOS));

    private static final Map<String, String> NOMBRES_DE_CAMPOS = Map.ofEntries(
            Map.entry("accesoDias", "Días de acceso"),
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

    /** Informa que un valor enviado no es válido. */
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
    public Object valorInvalido(MethodArgumentTypeMismatchException e, HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        String mensaje = MENSAJES_DE_CU.get(e.getName());
        if (mensaje == null) {
            String campo = NOMBRES_DE_CAMPOS.getOrDefault(e.getName(), e.getName());
            mensaje = MensajeErrorUtil.armarFormatoInvalido("El valor ingresado en «" + campo + "»", formatoDe(e.getRequiredType()));
        }
        if ("POST".equalsIgnoreCase(request.getMethod())) {
            return RespuestaFormularioUtil.armarRespuestaConError(HttpStatus.BAD_REQUEST, mensaje);
        }
        redirectAttributes.addFlashAttribute("error", mensaje);
        return "redirect:/inicio";
    }

    /** Informa que la dirección solicitada no admite ese método. */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Object metodoNoSoportado(HttpRequestMethodNotSupportedException e, RedirectAttributes redirectAttributes) {
        if ("GET".equalsIgnoreCase(e.getMethod())) {
            redirectAttributes.addFlashAttribute("error", "Error! Esa dirección no se puede abrir directamente.");
            return "redirect:/inicio";
        }
        return RespuestaFormularioUtil.armarRespuestaConError(HttpStatus.METHOD_NOT_ALLOWED, "Error! La operación solicitada no está permitida.");
    }

    /** Informa que el archivo supera el tamaño máximo que acepta el servidor. */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, String>> archivoDemasiadoGrande() {
        return RespuestaFormularioUtil.armarRespuestaConError(HttpStatus.BAD_REQUEST, MensajeErrorUtil.armarDemasiadoGrande("El archivo", 500, "MB"));
    }
}
