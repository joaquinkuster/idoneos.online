package com.app.idoneos.controlador;

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

import com.app.idoneos.modelo.Alumno;
import com.app.idoneos.modelo.Certificado;
import com.app.idoneos.modelo.Curso;
import com.app.idoneos.modelo.Usuario;
import com.app.idoneos.servicio.Certificado.CertificadoServicioImpl;
import com.app.idoneos.utilidades.RespuestaFormularioUtilidad;

/**
 * Controlador de los certificados (MOD-F-03).
 *
 * Mapea CU-48 Corregir datos personales del certificado (Administrador): la ventana se abre desde la búsqueda de
 * inscripciones (CU-44) y este controlador recibe la corrección. El alumno consulta sus certificados en "Mis
 * certificados" (GET /certificado/misCertificados) y los descarga en PDF (GET /certificado/descargar/{id}); el
 * administrador puede descargar cualquiera.
 */
@Controller
@RequestMapping("/certificado")
public class CertificadoControlador {

    @Autowired
    private CertificadoServicioImpl certificadoServicio;

    // ---------------------------------------------------------------- Lecturas

    /**
     * Lista los certificados del alumno (los que se emitieron al completar un curso), con la descarga en PDF.
     *
     * @param modelo El modelo de la vista.
     * @param auth   La autenticación actual.
     * @return La vista de "Mis certificados".
     */
    @GetMapping("/misCertificados")
    public String verMisCertificados(Model modelo, Authentication auth) {
        Alumno alumno = ((Usuario) auth.getPrincipal()).getAlumno();
        List<Certificado> certificados = certificadoServicio.buscarVigentesDelAlumno(alumno);
        modelo.addAttribute("certificados", certificados);
        modelo.addAttribute("cursosFiltro", certificados.stream().map(c -> c.getInscripcion().getCurso()).distinct()
                .sorted(Comparator.comparing(Curso::getNombre, String.CASE_INSENSITIVE_ORDER)).toList());
        modelo.addAttribute("titulo", "Mis certificados | Idóneos Online");
        return "pages/certificado/misCertificados";
    }

    /**
     * Descarga el certificado (PDF): el alumno el suyo y el administrador cualquiera.
     *
     * @param id   Identificador del certificado.
     * @param auth La autenticación actual.
     * @return El archivo PDF.
     */
    @GetMapping("/descargar/{id}")
    public ResponseEntity<byte[]> descargar(@PathVariable("id") Integer id, Authentication auth) {
        Usuario usuario = (Usuario) auth.getPrincipal();
        Alumno solicitante = usuario.esAdministradorActivo() ? null : usuario.getAlumno();
        if (solicitante == null && !usuario.esAdministradorActivo()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        Certificado certificado;
        try {
            certificado = certificadoServicio.buscarVisible(id, solicitante);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + certificado.getNumero() + ".pdf\"")
                .body(certificadoServicio.generarPdf(certificado));
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
            certificadoServicio.corregirDatos(id, nombre, dni);
            return RespuestaFormularioUtilidad.respuestaExitosa("Datos del certificado actualizados con éxito.");
        } catch (Exception e) {
            return RespuestaFormularioUtilidad.respuestaConError(e);
        }
    }
}
