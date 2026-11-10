package com.app.idoneos.service.indice;

import com.app.idoneos.model.IndiceRecurso;
import com.app.idoneos.model.Recurso;
import com.app.idoneos.repository.IndiceRecursoRepository;
import com.app.idoneos.util.MensajeErrorUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Implementación de IndiceRecursoService. */
@Service
public class IndiceRecursoServiceImpl implements IndiceRecursoService {

    @Autowired
    private IndiceRecursoRepository indiceRepository;

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    @Transactional(readOnly = true)
    public List<Entrada> buscar(Recurso recurso) {
        return indiceRepository.findByRecursoOrderByNumeroOrden(recurso).stream()
                .map(e -> new Entrada(e.getTitulo(), e.getPagina(), e.getNivel())).toList();
    }

    @Override
    @Transactional
    public void reemplazar(Recurso recurso, String json) {
        if (json == null) {
            return;
        }
        List<Entrada> entradas = leer(json);
        indiceRepository.borrarDelRecurso(recurso);
        List<IndiceRecurso> nuevas = new ArrayList<>();
        int orden = 1;
        for (Entrada entrada : entradas) {
            nuevas.add(new IndiceRecurso(recurso, entrada.titulo(), entrada.pagina(), entrada.nivel(), orden++));
        }
        indiceRepository.saveAll(nuevas);
    }

    /** Lee y valida el índice: cada entrada con título, una página de 1 en adelante y un nivel de 1 a 3. */
    private List<Entrada> leer(String json) {
        List<Entrada> leidas;
        try {
            leidas = json.isBlank() ? List.of() : mapper.readValue(json, new TypeReference<List<Entrada>>() { });
        } catch (Exception e) {
            throw new IllegalArgumentException("Error! El índice del documento no tiene un formato válido.");
        }
        if (leidas.size() > MAXIMO_DE_ENTRADAS) {
            throw new IllegalArgumentException(MensajeErrorUtil.armarDemasiadoLargo("El índice", MAXIMO_DE_ENTRADAS, "entradas"));
        }
        List<Entrada> validas = new ArrayList<>();
        for (Entrada entrada : leidas) {
            String titulo = entrada.titulo() == null ? "" : entrada.titulo().trim().replaceAll("\\s+", " ");
            if (titulo.isEmpty()) {
                throw new IllegalArgumentException("Error! Una entrada del índice no tiene título.");
            }
            if (titulo.length() > IndiceRecurso.LONGITUD_TITULO) {
                titulo = titulo.substring(0, IndiceRecurso.LONGITUD_TITULO - 1).trim() + "…";
            }
            if (entrada.pagina() < 1) {
                throw new IllegalArgumentException("Error! La entrada «" + titulo + "» del índice tiene una página que no es válida.");
            }
            validas.add(new Entrada(titulo, entrada.pagina(), Math.min(Math.max(entrada.nivel(), 1), IndiceRecurso.NIVEL_MAXIMO)));
        }
        return validas;
    }
}
