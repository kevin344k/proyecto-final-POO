package edu.poo.calificaciones.model;

import jakarta.persistence.*;

@Entity
public class Profesor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false)
    public String apellidos;

    @Column(nullable = false)
    public String nombres;

    public Long getId() { return id; }
    public String getApellidos() { return apellidos; }
    public String getNombres() { return nombres; }

    @Override
    public String toString() { return apellidos + " " + nombres; }
}
