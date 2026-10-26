package edu.poo.calificaciones.web;

import edu.poo.calificaciones.model.*;
import edu.poo.calificaciones.repo.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

public class Catalogos {

    @Controller
    @RequestMapping("/carreras")
    public static class Carreras extends CatalogoController<Carrera> {
        public Carreras(CarreraRepo repo) {
            super(repo, "carreras", "Carreras", List.of(new Campo("nombre", "Nombre")));
        }

        @Override protected Carrera crear() { return new Carrera(); }

        @Override protected void aplicar(Carrera c, Map<String, String> p) {
            c.nombre = requerido(p, "nombre", "Nombre");
        }
    }

    @Controller
    @RequestMapping("/asignaturas")
    public static class Asignaturas extends CatalogoController<Asignatura> {
        public Asignaturas(AsignaturaRepo repo) {
            super(repo, "asignaturas", "Asignaturas", List.of(new Campo("nombre", "Nombre")));
        }

        @Override protected Asignatura crear() { return new Asignatura(); }

        @Override protected void aplicar(Asignatura a, Map<String, String> p) {
            a.nombre = requerido(p, "nombre", "Nombre");
        }
    }

    @Controller
    @RequestMapping("/profesores")
    public static class Profesores extends CatalogoController<Profesor> {
        public Profesores(ProfesorRepo repo) {
            super(repo, "profesores", "Profesores",
                    List.of(new Campo("apellidos", "Apellidos"), new Campo("nombres", "Nombres")));
        }

        @Override protected Profesor crear() { return new Profesor(); }

        @Override protected void aplicar(Profesor x, Map<String, String> p) {
            x.apellidos = nombrePropio(p, "apellidos", "Apellidos");
            x.nombres = nombrePropio(p, "nombres", "Nombres");
        }
    }

    @Controller
    @RequestMapping("/estudiantes")
    public static class Estudiantes extends CatalogoController<Estudiante> {
        private final CarreraRepo carreras;

        public Estudiantes(EstudianteRepo repo, CarreraRepo carreras) {
            super(repo, "estudiantes", "Estudiantes", List.of(new Campo("apellidos", "Apellidos"),
                    new Campo("nombres", "Nombres"), new Campo("carrera", "Carrera")));
            this.carreras = carreras;
        }

        @Override protected Estudiante crear() { return new Estudiante(); }

        @Override protected void extras(Model m) { m.addAttribute("carreras", carreras.findAll()); }

        @Override protected void aplicar(Estudiante e, Map<String, String> p) {
            e.apellidos = nombrePropio(p, "apellidos", "Apellidos");
            e.nombres = nombrePropio(p, "nombres", "Nombres");
            try {
                e.carrera = carreras.findById(Long.valueOf(p.getOrDefault("carrera", "")))
                        .orElseThrow(() -> new IllegalArgumentException("La carrera no existe."));
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Seleccione una carrera.");
            }
        }
    }
}
