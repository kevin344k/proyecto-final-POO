package edu.poo.calificaciones.repo;

import edu.poo.calificaciones.model.Calificacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CalificacionRepo extends JpaRepository<Calificacion, Long> {
    List<Calificacion> findByCursoIdOrderByEstudianteApellidosAscEstudianteNombresAsc(Long cursoId);

    void deleteByCursoId(Long cursoId);
}
