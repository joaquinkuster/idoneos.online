package com.app.idoneos.service.Progreso;

import com.app.idoneos.model.Inscripcion;
import com.app.idoneos.model.Progreso;
import com.app.idoneos.model.Unidad;
import com.app.idoneos.repository.ProgresoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ProgresoServiceImpl implements ProgresoService {

    @Autowired
    private ProgresoRepository progresoRepository;

    @Override
    public Progreso guardar(Progreso progreso) {
        return progresoRepository.save(progreso);
    }

    @Override
    public Optional<Progreso> buscarPorId(Integer id) {
        return progresoRepository.findById(id);
    }

    @Override
    public List<Progreso> obtenerTodo() {
        return progresoRepository.findAll();
    }

    @Override
    public List<Progreso> obtenerPorInscripcion(Inscripcion inscripcion) {
        return progresoRepository.findByInscripcion(inscripcion);
    }

    @Override
    public Optional<Progreso> obtenerPorInscripcionYUnidad(Inscripcion inscripcion, Unidad unidad) {
        return progresoRepository.findByInscripcionAndUnidad(inscripcion, unidad);
    }

    @Override
    public Progreso marcarCompletada(Inscripcion inscripcion, Unidad unidad) {
        Optional<Progreso> existente = progresoRepository.findByInscripcionAndUnidad(inscripcion, unidad);
        if (existente.isPresent()) {
            Progreso p = existente.get();
            if (!p.getCompletada()) {
                p.setCompletada(true);
                p.setFechaCompletado(LocalDate.now());
                return progresoRepository.save(p);
            }
            return p;
        }
        Progreso nuevo = new Progreso(inscripcion, unidad, true);
        return progresoRepository.save(nuevo);
    }

    @Override
    public int contarCompletadas(Inscripcion inscripcion) {
        return (int) progresoRepository.findByInscripcion(inscripcion)
                .stream()
                .filter(Progreso::getCompletada)
                .count();
    }

    @Override
    public boolean unidadCompletada(Inscripcion inscripcion, Unidad unidad) {
        return progresoRepository.findByInscripcionAndUnidad(inscripcion, unidad)
                .map(Progreso::getCompletada)
                .orElse(false);
    }

    @Override
    public Progreso modificar(Progreso progreso) {
        return progresoRepository.save(progreso);
    }

    @Override
    public void borrar(Progreso progreso) {
        progresoRepository.delete(progreso);
    }

    @Override
    public boolean existePorId(Integer id) {
        return progresoRepository.existsById(id);
    }
}
