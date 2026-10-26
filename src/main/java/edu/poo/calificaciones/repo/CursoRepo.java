package edu.poo.calificaciones.repo;

import edu.poo.calificaciones.model.Curso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CursoRepo extends JpaRepository<Curso, Long> {
    
}
