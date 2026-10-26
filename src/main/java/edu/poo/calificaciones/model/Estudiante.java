package edu.poo.calificaciones.model;

import jakarta.persistence.*;

@Entity
public class Estudiante {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false)
    public String apellidos;

    @Column(nullable = false)
    public String nombres;

    @ManyToOne(optional = false)
    public Carrera carrera;

    public Long getId() { return id; }
    public String getApellidos() { return apellidos; }
    public String getNombres() { return nombres; }
    public Carrera getCarrera() { return carrera; }

    @Override
    public String toString() { return apellidos + " " + nombres; }
}
