package com.app.idoneos.configuracion;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.app.idoneos.modelo.Cohorte;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Consejo de controladores para los errores de la solicitud que ocurren antes de llegar a la lógica de cada
 * pantalla: valores que no se pueden convertir al tipo esperado (por ejemplo, un número demasiado grande) y
 * direcciones de operaciones que solo admiten POST abiertas desde el navegador.
 */
@ControllerAdvice
public class ErroresDeSolicitudConsejo {

    /** Mensajes de las excepciones de los casos de uso (CU-12 y CU-13) para los valores numéricos inválidos. */
    private static final Map<String, String> MENSAJES_DE_CU = Map.of(
            "cupoMaximo", "Error! El cupo máximo debe ser un número entero mayor a cero y no puede superar los "
                    + Cohorte.CUPO_MAXIMO_PERMITIDO + " inscriptos.",
            "semanasAcceso", "Error! Las semanas de acceso deben ser un número entero mayor a cero y no pueden superar las "
                    + Cohorte.SEMANAS_ACCESO_MAXIMAS + " semanas.");

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
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Object valorInvalido(MethodArgumentTypeMismatchException e, HttpServletRequest solicitud,
            RedirectAttributes redirectAttributes) {
        String mensaje = MENSAJES_DE_CU.get(e.getName());
        if (mensaje == null) {
            String campo = NOMBRES_DE_CAMPOS.getOrDefault(e.getName(), e.getName());
            mensaje = "Error! El valor ingresado en «" + campo + "» no es válido.";
        }
        if ("POST".equalsIgnoreCase(solicitud.getMethod())) {
            return ResponseEntity.badRequest().body(Map.of("error", mensaje));
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
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(Map.of("error", "Error! La operación solicitada no está permitida."));
    }
}
