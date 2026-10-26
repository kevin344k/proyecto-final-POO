# UML de clases

```mermaid
classDiagram
    class Carrera {
        -Long id
        -String nombre
    }
    class Asignatura {
        -Long id
        -String nombre
    }
    class Profesor {
        -Long id
        -String apellidos
        -String nombres
    }
    class Estudiante {
        -Long id
        -String apellidos
        -String nombres
    }
    class Curso {
        -Long id
        -int semestre
        -String ciclo
    }
    class Calificacion {
        -Long id
        -Double nota1
        -Double nota2
        -Double nota3
        +getPromedio() Double
        +getEstado() String
    }

    Carrera "1" --> "0..*" Estudiante : pertenece
    Carrera "1" --> "0..*" Curso
    Asignatura "1" --> "0..*" Curso
    Profesor "1" --> "0..*" Curso : dicta
    Curso "1" --> "0..*" Calificacion
    Estudiante "1" --> "0..*" Calificacion
```

Cada clase es una tabla (`carrera`, `asignatura`, `profesor`, `estudiante`, `curso`, `calificacion`) y las flechas son llaves foráneas.
Si tu UML tiene otras clases o atributos (cédula, correo, código de asignatura, etc.), pásamelo y lo ajusto.
