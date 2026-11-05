package com.app.idoneos.controller;

import java.util.Map;

import com.app.idoneos.model.Usuario;
import com.app.idoneos.service.recordatoriocohorte.RecordatorioCohorteService;
import com.app.idoneos.util.RespuestaFormularioUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * «Recordarme» de la ficha del curso: el alumno marca una cohorte próxima (POST /recordatorio/recordar/{cohorteId}) o quita
 * el recordatorio (POST /recordatorio/quitar/{cohorteId}). Responde en JSON.
 */
@Controller
@RequestMapping("/recordatorio")
public class RecordatorioCohorteController {

    @Autowired
    private RecordatorioCohorteService recordatorioService;

    /** Marca la cohorte próxima para que el sistema avise al alumno unos días antes de que abra la inscripción. */
    @PostMapping("/recordar/{cohorteId}")
    public ResponseEntity<Map<String, String>> recordar(@PathVariable("cohorteId") Integer cohorteId, Authentication authentication) {
        try {
            recordatorioService.recordar(((Usuario) authentication.getPrincipal()).getAlumno(), cohorteId);
            return RespuestaFormularioUtil.armarRespuestaExitosa("Listo, te vamos a avisar antes de que abra la inscripción.");
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    /** Quita el recordatorio de la cohorte. */
    @PostMapping("/quitar/{cohorteId}")
    public ResponseEntity<Map<String, String>> quitar(@PathVariable("cohorteId") Integer cohorteId, Authentication authentication) {
        try {
            recordatorioService.quitar(((Usuario) authentication.getPrincipal()).getAlumno(), cohorteId);
            return RespuestaFormularioUtil.armarRespuestaExitosa("Recordatorio quitado.");
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }
}
