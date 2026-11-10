package com.app.idoneos.service.resaltado;

import com.app.idoneos.model.Alumno;
import com.app.idoneos.model.Recurso;
import com.app.idoneos.model.ResaltadoAlumno;
import com.app.idoneos.repository.ResaltadoAlumnoRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Implementación de ResaltadoAlumnoService. */
@Service
public class ResaltadoAlumnoServiceImpl implements ResaltadoAlumnoService {

    /** Cantidad máxima de páginas de un documento que se pueden anotar. */
    private static final int PAGINA_MAXIMA = 5000;

    @Autowired
    private ResaltadoAlumnoRepository resaltadoRepository;

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    @Transactional(readOnly = true)
    public List<Pagina> buscar(Alumno alumno, Recurso recurso) {
        return resaltadoRepository.findByAlumnoAndRecursoOrderByPagina(alumno, recurso).stream()
                .map(r -> new Pagina(r.getPagina(), r.getTrazos())).toList();
    }

    @Override
    @Transactional
    public void guardar(Alumno alumno, Recurso recurso, int pagina, String trazos) {
        if (pagina < 1 || pagina > PAGINA_MAXIMA) {
            throw new IllegalArgumentException("Error! La página no es válida.");
        }
        String json = trazos == null || trazos.isBlank() ? "[]" : trazos.trim();
        if (json.length() > ResaltadoAlumno.LONGITUD_TRAZOS) {
            throw new IllegalArgumentException("Error! Las anotaciones de esta página son demasiado extensas. Borrá algunas para seguir.");
        }
        JsonNode arbol;
        try {
            arbol = mapper.readTree(json);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error! Las anotaciones no tienen un formato válido.");
        }
        if (arbol == null || !arbol.isArray()) {
            throw new IllegalArgumentException("Error! Las anotaciones no tienen un formato válido.");
        }
        var existente = resaltadoRepository.findByAlumnoAndRecursoAndPagina(alumno, recurso, pagina);
        if (arbol.isEmpty()) {
            existente.ifPresent(resaltadoRepository::delete);
            return;
        }
        ResaltadoAlumno fila = existente.orElseGet(() -> new ResaltadoAlumno(alumno, recurso, pagina, json));
        fila.setTrazos(json);
        fila.setUltimaModificacion(LocalDateTime.now());
        resaltadoRepository.save(fila);
    }
}
