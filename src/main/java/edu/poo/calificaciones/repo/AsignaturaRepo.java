package edu.poo.calificaciones.repo;

import edu.poo.calificaciones.model.Asignatura;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AsignaturaRepo extends JpaRepository<Asignatura, Long> {
    Optional<Asignatura> findByNombreIgnoreCase(String nombre);
}
