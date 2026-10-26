package edu.poo.calificaciones.model;

import jakarta.persistence.*;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"curso_id", "estudiante_id"}))
public class Calificacion {
    public static final double MINIMO_APROBACION = 70;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(optional = false)
    public Curso curso;

    @ManyToOne(optional = false)
    public Estudiante estudiante;

    public Double nota1;
    public Double nota2;
    public Double nota3;

    public Long getId() { return id; }
    public Curso getCurso() { return curso; }
    public Estudiante getEstudiante() { return estudiante; }
    public Double getNota1() { return nota1; }
    public Double getNota2() { return nota2; }
    public Double getNota3() { return nota3; }

    public boolean completa() { return nota1 != null && nota2 != null && nota3 != null; }

    public Double getPromedio() {
        return completa() ? Math.round((nota1 + nota2 + nota3) / 3 * 100) / 100.0 : null;
    }

    public String getEstado() {
        if (!completa()) return "PENDIENTE";
        return getPromedio() >= MINIMO_APROBACION ? "APROBADO" : "REPROBADO";
    }
}
