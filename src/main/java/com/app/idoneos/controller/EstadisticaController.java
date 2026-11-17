package com.app.idoneos.controller;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.app.idoneos.service.estadistica.EstadisticaService;

/** Controlador de las estadísticas: el tablero con los indicadores del negocio. */
@Controller
@RequestMapping("/estadisticas")
public class EstadisticaController {

    @Autowired
    private EstadisticaService estadisticaService;

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /** GET /estadisticas: Muestra el tablero de estadísticas, sin necesidad de generar un informe. */
    @GetMapping({ "", "/" })
    public String ver(Model model) {
        EstadisticaService.Tablero tablero = estadisticaService.tablero();
        model.addAttribute("t", tablero);
        model.addAttribute("valoresDias", Arrays.stream(tablero.inscripcionesPorDia()).boxed().toList());
        model.addAttribute("titulo", "Tablero de estadísticas | Idóneos Online");
        model.addAttribute("menuActivo", "estadisticas");
        return "pages/estadistica/tablero";
    }
}
