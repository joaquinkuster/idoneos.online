package com.app.idoneos.service.indice;

import com.app.idoneos.model.IndiceDocumento;
import com.app.idoneos.model.Documento;
import com.app.idoneos.repository.IndiceDocumentoRepository;
import com.app.idoneos.repository.OracionRepository;
import com.app.idoneos.util.MensajeErrorUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Implementación de IndiceDocumentoService. */
@Service
public class IndiceDocumentoServiceImpl implements IndiceDocumentoService {

    @Autowired
    private IndiceDocumentoRepository indiceRepository;

    @Autowired
    private OracionRepository oracionRepository;

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    @Transactional(readOnly = true)
    public List<Entrada> buscar(Documento documento) {
        if (documento == null) {
            return List.of();
        }
        return indiceRepository.findByDocumentoOrderByNumeroOrden(documento).stream()
                .map(e -> new Entrada(e.getTitulo(), e.getPagina(), e.getNivel())).toList();
    }

    @Override
    @Transactional
    public void crear(Documento documento, String json) {
        if (documento == null) {
            return;
        }
        guardar(documento, json);
    }

    @Override
    @Transactional
    public void reemplazar(Documento documento, String json) {
        if (documento == null || json == null) {
            return;
        }
        oracionRepository.borrarDelDocumento(documento);
        indiceRepository.borrarDelDocumento(documento);
        guardar(documento, json);
    }

    /** Valida las entradas y las guarda en su orden. Si no hay ninguna, guarda la entrada por defecto. */
    private void guardar(Documento documento, String json) {
        List<Entrada> entradas = leer(json == null ? "" : json);
        if (entradas.isEmpty()) {
            entradas = List.of(entradaPorDefecto(documento));
        }
        List<IndiceDocumento> nuevas = new ArrayList<>();
        int orden = 1;
        for (Entrada entrada : entradas) {
            nuevas.add(new IndiceDocumento(documento, entrada.titulo(), entrada.pagina(), entrada.nivel(), orden++));
        }
        indiceRepository.saveAll(nuevas);
    }

    /** La entrada de un documento sin índice: su título, en el nivel 1 y la página 1. */
    private Entrada entradaPorDefecto(Documento documento) {
        String titulo = documento.getRecurso().getTitulo().trim().replaceAll("\\s+", " ");
        if (titulo.length() > IndiceDocumento.LONGITUD_TITULO) {
            titulo = titulo.substring(0, IndiceDocumento.LONGITUD_TITULO - 1).trim() + "…";
        }
        return new Entrada(titulo, 1, 1);
    }

    /** Lee y valida el índice: cada entrada con título, una página de 1 en adelante y un nivel de 1 a 4. */
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
            if (titulo.length() > IndiceDocumento.LONGITUD_TITULO) {
                titulo = titulo.substring(0, IndiceDocumento.LONGITUD_TITULO - 1).trim() + "…";
            }
            if (entrada.pagina() < 1) {
                throw new IllegalArgumentException("Error! La entrada «" + titulo + "» del índice tiene una página que no es válida.");
            }
            validas.add(new Entrada(titulo, entrada.pagina(), Math.min(Math.max(entrada.nivel(), 1), IndiceDocumento.NIVEL_MAXIMO)));
        }
        return validas;
    }
}
