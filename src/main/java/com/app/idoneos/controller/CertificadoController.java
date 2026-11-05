package com.app.idoneos.controller;

import com.app.idoneos.util.FiltroUtil;

import com.app.idoneos.util.PaginacionUtil;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.server.ResponseStatusException;

import com.app.idoneos.model.Alumno;
import com.app.idoneos.model.Certificado;
import com.app.idoneos.model.Curso;
import com.app.idoneos.model.Usuario;
import com.app.idoneos.service.certificado.CertificadoService;
import com.app.idoneos.util.RespuestaFormularioUtil;

/**
 * Controlador de los certificados (MOD-F-03).
 *
 * Mapea CU-48 Corregir datos personales del certificado (Administrador): la ventana se abre desde la búsqueda de
 * inscripciones (CU-44) y este controlador recibe la corrección. El alumno consulta sus certificados en "Mis
 * certificados" (GET /certificado/buscar) y los descarga en PDF (GET /certificado/descargar/{id}); el
 * administrador puede descargar cualquiera.
 */
@Controller
@RequestMapping("/certificado")
public class CertificadoController {

    @Autowired
    private CertificadoService certificadoService;

    // ---------------------------------------------------------------- Lecturas

    /**
     * Lista los certificados del alumno (los que se emitieron al completar un curso), con la descarga en PDF.
     *
     * @param model El modelo de la vista.
     * @param authentication   La autenticación actual.
     * @return La vista de "Mis certificados".
     */
    @GetMapping("/buscar")
    public String buscarPorAlumno(Model model, Authentication authentication, jakarta.servlet.http.HttpServletRequest request) {
        Alumno alumno = ((Usuario) authentication.getPrincipal()).getAlumno();
        List<Certificado> certificados = certificadoService.buscarVigentesDelAlumno(alumno);
        // El filtro, el orden por columna y la página los resuelve el servidor
        PaginacionUtil.Pagina<Certificado> pagina = Filtros.tabla.paginar(certificados, request.getParameterMap());
        PaginacionUtil.cargarEnModelo(model, pagina);
        model.addAttribute("certificados", pagina.filas());
        if (PaginacionUtil.pideSoloFilas(request)) {
            return "pages/certificado/buscarPorAlumno :: filas"; // solo las filas de la página pedida
        }
        model.addAttribute("cursosFiltro", certificados.stream().map(c -> c.getInscripcion().getCurso()).distinct()
                .sorted(Comparator.comparing(Curso::getNombre, String.CASE_INSENSITIVE_ORDER)).toList());
        model.addAttribute("titulo", "Mis certificados | Idóneos Online");
        return "pages/certificado/buscarPorAlumno";
    }

    /**
     * Descarga el certificado (PDF): el alumno el suyo y el administrador cualquiera.
     *
     * @param id   Identificador del certificado.
     * @param authentication La autenticación actual.
     * @return El archivo PDF.
     */
    @GetMapping("/descargar/{id}")
    public ResponseEntity<byte[]> descargar(@PathVariable("id") Integer id, Authentication authentication) {
        Usuario usuario = (Usuario) authentication.getPrincipal();
        Alumno solicitante = usuario.esAdministradorActivo() ? null : usuario.getAlumno();
        if (solicitante == null && !usuario.esAdministradorActivo()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        Certificado certificado;
        try {
            certificado = certificadoService.buscarVisible(id, solicitante);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + certificado.getNumero() + ".pdf\"")
                .body(certificadoService.generarPdf(certificado));
    }

    // ---------------------------------------------------------------- Modificaciones

    /**
     * CU-48: Corrige el nombre y/o el DNI del alumno registrados en un certificado emitido. Responde en JSON.
     *
     * @param id     Identificador del certificado.
     * @param nombre El nombre completo del alumno.
     * @param dni    El DNI del alumno.
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/corregir/{id}")
    public ResponseEntity<Map<String, String>> corregirDatos(@PathVariable("id") Integer id,
            @RequestParam(value = "nombre", required = false) String nombre,
            @RequestParam(value = "dni", required = false) String dni) {
        try {
            certificadoService.corregirDatos(id, nombre, dni);
            return RespuestaFormularioUtil.armarRespuestaExitosa("Datos del certificado actualizados con éxito.");
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }


    /** Columnas de filtro, orden y página de los listados (las aplica el servidor). */
    public static final class Filtros {

        private Filtros() {
        }

        /** Las columnas de "Mis certificados": lo que se filtra y se ordena en el servidor. */
        public static final FiltroUtil<Certificado> tabla = new FiltroUtil<>(List.of(
                new FiltroUtil.Columna<Certificado>("curso", FiltroUtil.Tipo.LISTA, c -> c.getInscripcion().getCurso().getNombre()),
                new FiltroUtil.Columna<Certificado>("programa", FiltroUtil.Tipo.LISTA,
                        c -> c.getInscripcion().getCohorte().getPrograma().getNombre()),
                new FiltroUtil.Columna<Certificado>("numero", FiltroUtil.Tipo.TEXTO, Certificado::getNumero),
                new FiltroUtil.Columna<Certificado>("fechaEmision", FiltroUtil.Tipo.FECHA,
                        c -> FiltroUtil.formatearFechaHora(c.getFechaEmision()))),
                c -> false);
    }
}
