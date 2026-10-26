package edu.poo.calificaciones.repo;

import edu.poo.calificaciones.model.Estudiante;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EstudianteRepo extends JpaRepository<Estudiante, Long> {
    Optional<Estudiante> findFirstByApellidosIgnoreCaseAndNombresIgnoreCaseAndCarreraId(String apellidos, String nombres, Long carreraId);
}
