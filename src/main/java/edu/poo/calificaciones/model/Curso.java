package edu.poo.calificaciones.model;

import jakarta.persistence.*;

/** Una asignatura dictada en una carrera, semestre y ciclo lectivo por un profesor. */
@Entity
public class Curso {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(optional = false)
    public Carrera carrera;

    @ManyToOne(optional = false)
    public Asignatura asignatura;

    @ManyToOne(optional = false)
    public Profesor profesor;

    @Column(nullable = false)
    public int semestre;

    @Column(nullable = false)
    public String ciclo;

    public Long getId() { return id; }
    public Carrera getCarrera() { return carrera; }
    public Asignatura getAsignatura() { return asignatura; }
    public Profesor getProfesor() { return profesor; }
    public int getSemestre() { return semestre; }
    public String getCiclo() { return ciclo; }
}
