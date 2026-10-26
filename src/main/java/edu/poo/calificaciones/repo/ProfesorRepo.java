package edu.poo.calificaciones.repo;

import edu.poo.calificaciones.model.Profesor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProfesorRepo extends JpaRepository<Profesor, Long> {
    Optional<Profesor> findFirstByApellidosIgnoreCaseAndNombresIgnoreCase(String apellidos, String nombres);
}
