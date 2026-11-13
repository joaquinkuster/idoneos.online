package com.app.idoneos.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.app.idoneos.model.Bibliografia;
import com.app.idoneos.repository.BibliografiaRepository;
import com.app.idoneos.service.recurso.RecursoService;
import com.app.idoneos.util.ManejadorArchivosUtil;

/** Pruebas del renombrado de los archivos guardados con el nombre anterior. */
@SpringBootTest
@Transactional
class MigracionDeNombresDeArchivosTest {

    @Autowired
    private RecursoService recursoService;

    @Autowired
    private BibliografiaRepository bibliografiaRepository;

    @Autowired
    @Qualifier("directorioCursos")
    private Path directorioCursos;

    @Test
    @DisplayName("Un archivo guardado como recurso{id} pasa a llamarse como su tipo, con su ruta actualizada, y se puede repetir")
    void renombraLosArchivosAnteriores() throws IOException {
        Bibliografia bibliografia = bibliografiaRepository.findAll().stream()
                .filter(b -> Files.exists(ManejadorArchivosUtil.resolver(directorioCursos, b.getRutaArchivo()))).findFirst().orElseThrow();
        int id = bibliografia.getRecurso().getIdRecurso();
        String carpeta = bibliografia.getRutaArchivo().substring(0, bibliografia.getRutaArchivo().lastIndexOf('/') + 1);
        String antigua = carpeta + "recurso" + id + ".pdf";
        Files.deleteIfExists(ManejadorArchivosUtil.resolver(directorioCursos, antigua));
        Files.move(ManejadorArchivosUtil.resolver(directorioCursos, bibliografia.getRutaArchivo()),
                ManejadorArchivosUtil.resolver(directorioCursos, antigua));
        bibliografia.setRutaArchivo(antigua);

        assertTrue(recursoService.migrarNombresDeArchivos() >= 1);

        assertEquals(carpeta + "bibliografia" + id + ".pdf", bibliografia.getRutaArchivo());
        assertTrue(Files.exists(ManejadorArchivosUtil.resolver(directorioCursos, bibliografia.getRutaArchivo())));
        assertFalse(Files.exists(ManejadorArchivosUtil.resolver(directorioCursos, antigua)));
        assertEquals(0, recursoService.migrarNombresDeArchivos());
    }
}
