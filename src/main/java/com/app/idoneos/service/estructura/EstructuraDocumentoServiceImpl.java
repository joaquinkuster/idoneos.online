package com.app.idoneos.service.estructura;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.idoneos.model.Documento;
import com.app.idoneos.model.IndiceDocumento;
import com.app.idoneos.model.Oracion;
import com.app.idoneos.repository.IndiceDocumentoRepository;
import com.app.idoneos.repository.OracionRepository;
import com.app.idoneos.util.EstructuraPdfUtil;
import com.app.idoneos.util.ManejadorArchivosUtil;
import com.app.idoneos.util.OracionesUtil;

/** Implementación de EstructuraDocumentoService. */
@Service
public class EstructuraDocumentoServiceImpl implements EstructuraDocumentoService {

    private static final Logger REGISTRO = LoggerFactory.getLogger(EstructuraDocumentoServiceImpl.class);

    /** Cuánto texto de un PDF se lee como máximo. */
    private static final int LECTURA_COMPLETA = 2_000_000;

    @Autowired
    private IndiceDocumentoRepository indiceRepository;

    @Autowired
    private OracionRepository oracionRepository;

    @Autowired
    @Qualifier("directorioCursos")
    private Path directorioCursos;

    // ----------------------------------------------------------------------
    // Modificaciones
    // ----------------------------------------------------------------------

    @Override
    @Transactional
    public int reconstruir(Documento documento) {
        oracionRepository.borrarDelDocumento(documento);
        List<IndiceDocumento> entradas = indiceRepository.findByDocumentoOrderByNumeroOrden(documento);
        List<OracionesUtil.OracionDeSeccion> oraciones = leerOraciones(documento);
        if (entradas.isEmpty() || oraciones.isEmpty()) {
            return 0;
        }
        List<Oracion> nuevas = new ArrayList<>();
        int[] siguiente = new int[entradas.size()];
        for (OracionesUtil.OracionDeSeccion oracion : oraciones) {
            int entrada = entradaDeLaPagina(entradas, oracion.pagina());
            nuevas.add(new Oracion(entradas.get(entrada), ++siguiente[entrada], oracion.texto()));
        }
        oracionRepository.saveAll(nuevas);
        return nuevas.size();
    }

    @Override
    @Transactional
    public void guardarSecciones(Documento documento, List<Seccion> secciones) {
        oracionRepository.borrarDelDocumento(documento);
        List<IndiceDocumento> entradas = indiceRepository.findByDocumentoOrderByNumeroOrden(documento);
        if (entradas.isEmpty()) {
            return;
        }
        List<Oracion> nuevas = new ArrayList<>();
        int[] siguiente = new int[entradas.size()];
        int desde = 0;
        int ultima = 0;
        for (Seccion seccion : secciones) {
            String titulo = normalizarTitulo(seccion.titulo());
            for (int i = desde; i < entradas.size(); i++) {
                if (titulo.equals(entradas.get(i).getTitulo())) {
                    ultima = i;
                    desde = i + 1;
                    break;
                }
            }
            for (String texto : seccion.textos()) {
                nuevas.add(new Oracion(entradas.get(ultima), ++siguiente[ultima], texto));
            }
        }
        oracionRepository.saveAll(nuevas);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Seccion> secciones(Documento documento) {
        java.util.Map<Integer, List<String>> textos = new java.util.LinkedHashMap<>();
        java.util.Map<Integer, String> titulos = new java.util.HashMap<>();
        java.util.Map<Integer, Integer> niveles = new java.util.HashMap<>();
        for (Oracion oracion : oracionRepository.buscarDelDocumento(documento)) {
            IndiceDocumento entrada = oracion.getIndiceDocumento();
            titulos.putIfAbsent(entrada.getIdIndiceDocumento(), entrada.getTitulo());
            niveles.putIfAbsent(entrada.getIdIndiceDocumento(), (int) entrada.getNivel());
            textos.computeIfAbsent(entrada.getIdIndiceDocumento(), k -> new ArrayList<>()).add(oracion.getTexto());
        }
        // Todas las entradas del índice, en su orden, aunque no tengan oraciones: las que agrupan a otras (por ejemplo, «2. Desarrollo») también se editan
        List<Seccion> secciones = new ArrayList<>();
        for (IndiceDocumento entrada : indiceRepository.findByDocumentoOrderByNumeroOrden(documento)) {
            secciones.add(new Seccion(entrada.getTitulo(), (int) entrada.getNivel(),
                    new ArrayList<>(textos.getOrDefault(entrada.getIdIndiceDocumento(), List.of()))));
        }
        return secciones;
    }

    /** El título como lo guarda el índice: sin espacios de más y, si es muy largo, cortado con puntos suspensivos. */
    private static String normalizarTitulo(String titulo) {
        String limpio = titulo == null ? "" : titulo.trim().replaceAll("\\s+", " ");
        return limpio.length() > IndiceDocumento.LONGITUD_TITULO ? limpio.substring(0, IndiceDocumento.LONGITUD_TITULO - 1).trim() + "…" : limpio;
    }

    /** Lee las oraciones candidatas del PDF del documento, o ninguna si no se puede. */
    private List<OracionesUtil.OracionDeSeccion> leerOraciones(Documento documento) {
        String ruta = documento.getRutaArchivo();
        if (ruta == null || !"pdf".equalsIgnoreCase(documento.getExtension())) {
            return List.of();
        }
        Path pdf = ManejadorArchivosUtil.resolver(directorioCursos, ruta);
        if (!Files.isRegularFile(pdf)) {
            return List.of();
        }
        try {
            List<EstructuraPdfUtil.Bloque> bloques = EstructuraPdfUtil.leer(pdf, LECTURA_COMPLETA);
            return OracionesUtil.candidatasPorBloques(bloques, Integer.MAX_VALUE);
        } catch (IOException | RuntimeException e) {
            REGISTRO.warn("No se pudo leer la estructura del documento {}: {}", documento.getIdDocumento(), e.getMessage());
            return List.of();
        }
    }

    /** La posición de la última entrada que empieza en la página o antes, o la primera si la página va antes de todas. */
    private static int entradaDeLaPagina(List<IndiceDocumento> entradas, int pagina) {
        int elegida = 0;
        for (int i = 0; i < entradas.size(); i++) {
            if (entradas.get(i).getPagina() <= pagina) {
                elegida = i;
            }
        }
        return elegida;
    }
}
