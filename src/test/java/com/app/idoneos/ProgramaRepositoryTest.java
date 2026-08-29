package com.app.idoneos;

import com.app.idoneos.model.Curso;
import com.app.idoneos.model.Programa;
import com.app.idoneos.repository.modulo_cursos.CursoRepository;
import com.app.idoneos.repository.modulo_gestion_academica.ProgramaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
    "spring.security.oauth2.client.registration.google.client-id=demo-id",
    "spring.security.oauth2.client.registration.google.client-secret=demo-secret"
})
@Transactional
public class ProgramaRepositoryTest {

    @Autowired private ProgramaRepository programaRepository;
    @Autowired private CursoRepository cursoRepository;

    @Test
    @DisplayName("Repositorio: Consultas personalizadas findByCurso, findByCursoAndBajaFalse y findByBajaFalse")
    void testConsultasPersonalizadas() {
        Curso curso = cursoRepository.findAll().stream().filter(c -> !c.isBaja()).findFirst().orElseThrow();

        Programa p1 = new Programa("Prog Rep 1", "Desc 1", "Obj 1", "Bib 1", curso);
        p1.setBaja(false);
        programaRepository.save(p1);

        Programa p2 = new Programa("Prog Rep 2 Baja", "Desc 2", "Obj 2", "Bib 2", curso);
        p2.setBaja(true);
        programaRepository.save(p2);

        List<Programa> todosCurso = programaRepository.findByCurso(curso);
        assertTrue(todosCurso.stream().anyMatch(p -> "Prog Rep 1".equals(p.getNombre())));
        assertTrue(todosCurso.stream().anyMatch(p -> "Prog Rep 2 Baja".equals(p.getNombre())));

        List<Programa> activosCurso = programaRepository.findByCursoAndBajaFalse(curso);
        assertTrue(activosCurso.stream().anyMatch(p -> "Prog Rep 1".equals(p.getNombre())));
        assertFalse(activosCurso.stream().anyMatch(p -> "Prog Rep 2 Baja".equals(p.getNombre())));

        List<Programa> activosGenerales = programaRepository.findByBajaFalse();
        assertTrue(activosGenerales.stream().allMatch(p -> !p.isBaja()));
    }
}
