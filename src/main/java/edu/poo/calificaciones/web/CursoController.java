package edu.poo.calificaciones.web;

import edu.poo.calificaciones.model.*;
import edu.poo.calificaciones.repo.*;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

@Controller
public class CursoController {
    private static final int MAX_ESTUDIANTES = 200;
    private static final String NOMBRE = "[\\p{L} .'-]+";

    private final CarreraRepo carreras;
    private final AsignaturaRepo asignaturas;
    private final ProfesorRepo profesores;
    private final EstudianteRepo estudiantes;
    private final CursoRepo cursos;
    private final CalificacionRepo calificaciones;

    public CursoController(CarreraRepo carreras, AsignaturaRepo asignaturas, ProfesorRepo profesores,
                           EstudianteRepo estudiantes, CursoRepo cursos, CalificacionRepo calificaciones) {
        this.carreras = carreras;
        this.asignaturas = asignaturas;
        this.profesores = profesores;
        this.estudiantes = estudiantes;
        this.cursos = cursos;
        this.calificaciones = calificaciones;
    }

    @ModelAttribute("carrerasSug")
    public List<Carrera> carrerasSug() { return carreras.findAll(); }

    @ModelAttribute("asignaturasSug")
    public List<Asignatura> asignaturasSug() { return asignaturas.findAll(); }

    @GetMapping("/")
    public String inicio(Model m) {
        m.addAttribute("cursos", cursos.findAll());
        return "inicio";
    }

    // ---------- Asistente de creación ----------

    @GetMapping("/cursos/nuevo")
    public String nuevo(Model m) {
        m.addAttribute("f", Map.of());
        return "curso-paso1";
    }

    @PostMapping("/cursos/paso2")
    public String paso2(@RequestParam Map<String, String> f, Model m) {
        try {
            int n = validarCabecera(f);
            m.addAttribute("filas", IntStream.rangeClosed(1, n).boxed().toList());
            m.addAttribute("ap", List.of()).addAttribute("no", List.of());
        } catch (IllegalArgumentException e) {
            m.addAttribute("error", e.getMessage());
            return "curso-paso1";
        } finally {
            m.addAttribute("f", f);
        }
        return "curso-paso2";
    }

    @PostMapping("/cursos/crear")
    @Transactional
    public String crear(@RequestParam Map<String, String> f,
                        @RequestParam(name = "ap", required = false, defaultValue = "") List<String> ap,
                        @RequestParam(name = "no", required = false, defaultValue = "") List<String> no,
                        Model m, RedirectAttributes ra) {
        int n;
        m.addAttribute("f", f);
        try {
            n = validarCabecera(f);
        } catch (IllegalArgumentException e) {
            m.addAttribute("error", e.getMessage());
            return "curso-paso1";
        }
        try {
            validarEstudiantes(n, ap, no);
        } catch (IllegalArgumentException e) {
            m.addAttribute("error", e.getMessage());
            m.addAttribute("filas", IntStream.rangeClosed(1, n).boxed().toList());
            m.addAttribute("ap", ap).addAttribute("no", no);
            return "curso-paso2";
        }
        Long id = guardarCurso(f, ap, no);
        ra.addFlashAttribute("ok", "Curso creado. Ahora ingrese las calificaciones.");
        return "redirect:/cursos/" + id + "/notas";
    }

    private Long guardarCurso(Map<String, String> f, List<String> ap, List<String> no) {
        Carrera carrera = carreras.findByNombreIgnoreCase(limpiar(f.get("carrera"))).orElseGet(() -> {
            Carrera c = new Carrera();
            c.nombre = limpiar(f.get("carrera"));
            return carreras.save(c);
        });
        Asignatura asig = asignaturas.findByNombreIgnoreCase(limpiar(f.get("asignatura"))).orElseGet(() -> {
            Asignatura a = new Asignatura();
            a.nombre = limpiar(f.get("asignatura"));
            return asignaturas.save(a);
        });
        String pa = limpiar(f.get("profApellidos")), pn = limpiar(f.get("profNombres"));
        Profesor prof = profesores.findFirstByApellidosIgnoreCaseAndNombresIgnoreCase(pa, pn).orElseGet(() -> {
            Profesor p = new Profesor();
            p.apellidos = pa;
            p.nombres = pn;
            return profesores.save(p);
        });
        Curso curso = new Curso();
        curso.carrera = carrera;
        curso.asignatura = asig;
        curso.profesor = prof;
        curso.semestre = Integer.parseInt(f.get("semestre").trim());
        curso.ciclo = limpiar(f.get("ciclo"));
        cursos.save(curso);
        for (int i = 0; i < ap.size(); i++) {
            String a = limpiar(ap.get(i)), n = limpiar(no.get(i));
            Estudiante est = estudiantes
                    .findFirstByApellidosIgnoreCaseAndNombresIgnoreCaseAndCarreraId(a, n, carrera.id)
                    .orElseGet(() -> {
                        Estudiante e = new Estudiante();
                        e.apellidos = a;
                        e.nombres = n;
                        e.carrera = carrera;
                        return estudiantes.save(e);
                    });
            Calificacion cal = new Calificacion();
            cal.curso = curso;
            cal.estudiante = est;
            calificaciones.save(cal);
        }
        return curso.id;
    }

    private static String limpiar(String s) {
        return s == null ? "" : s.trim().replaceAll("\\s+", " ");
    }

    private int validarCabecera(Map<String, String> f) {
        String num = limpiar(f.get("numEstudiantes"));
        if (!num.matches("\\d{1,4}") || Integer.parseInt(num) < 1 || Integer.parseInt(num) > MAX_ESTUDIANTES)
            throw new IllegalArgumentException("Cantidad de estudiantes inválida (1 a " + MAX_ESTUDIANTES + ").");
        String sem = limpiar(f.get("semestre"));
        if (!sem.matches("\\d{1,2}") || Integer.parseInt(sem) < 1 || Integer.parseInt(sem) > 12)
            throw new IllegalArgumentException("El campo semestre debe ser un número entre 1 y 12.");
        if (!limpiar(f.get("ciclo")).matches("[\\p{L}\\p{N} /.-]{3,30}"))
            throw new IllegalArgumentException("El campo ciclo lectivo es obligatorio (ej. 2026-1).");
        for (String[] c : new String[][]{{"carrera", "carrera"}, {"asignatura", "asignatura"}}) {
            String v = limpiar(f.get(c[0]));
            if (v.isEmpty() || v.length() > 100)
                throw new IllegalArgumentException("El campo " + c[1] + " es obligatorio (máx. 100 caracteres).");
        }
        for (String[] c : new String[][]{{"profApellidos", "apellidos del profesor"}, {"profNombres", "nombres del profesor"}}) {
            String v = limpiar(f.get(c[0]));
            if (!v.matches(NOMBRE) || v.length() > 100)
                throw new IllegalArgumentException("El campo " + c[1] + " es obligatorio y solo admite letras.");
        }
        return Integer.parseInt(num);
    }

    private void validarEstudiantes(int n, List<String> ap, List<String> no) {
        if (ap.size() != n || no.size() != n)
            throw new IllegalArgumentException("Debe ingresar los datos de los " + n + " estudiantes (campo incompleto).");
        Set<String> vistos = new HashSet<>();
        for (int i = 0; i < n; i++) {
            String a = limpiar(ap.get(i)), nn = limpiar(no.get(i));
            if (!a.matches(NOMBRE) || !nn.matches(NOMBRE) || a.length() > 100 || nn.length() > 100)
                throw new IllegalArgumentException("Estudiante " + (i + 1) + ": campo de apellidos/nombres vacío o con caracteres inválidos.");
            if (!vistos.add((a + "|" + nn).toLowerCase()))
                throw new IllegalArgumentException("Estudiante " + (i + 1) + ": campo repetido (" + a + " " + nn + ").");
        }
    }

    // ---------- Calificaciones ----------

    @GetMapping("/cursos/{id}/notas")
    public String notas(@PathVariable Long id, Model m, RedirectAttributes ra) {
        var curso = cursos.findById(id);
        if (curso.isEmpty()) {
            ra.addFlashAttribute("error", "El curso no existe.");
            return "redirect:/";
        }
        m.addAttribute("curso", curso.get());
        m.addAttribute("filas", calificaciones.findByCursoIdOrderByEstudianteApellidosAscEstudianteNombresAsc(id));
        return "notas";
    }

    @PostMapping("/cursos/{id}/notas")
    @Transactional
    public String guardarNotas(@PathVariable Long id, @RequestParam Map<String, String> p, Model m,
                               RedirectAttributes ra) {
        var curso = cursos.findById(id);
        if (curso.isEmpty()) {
            ra.addFlashAttribute("error", "El curso no existe.");
            return "redirect:/";
        }
        List<Calificacion> filas = calificaciones.findByCursoIdOrderByEstudianteApellidosAscEstudianteNombresAsc(id);
        List<String> errores = new ArrayList<>();
        for (Calificacion c : filas) {
            c.nota1 = leer(p, "n1_" + c.id, c, 1, errores);
            c.nota2 = leer(p, "n2_" + c.id, c, 2, errores);
            c.nota3 = leer(p, "n3_" + c.id, c, 3, errores);
        }
        if (!errores.isEmpty()) {
            org.springframework.transaction.interceptor.TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            m.addAttribute("curso", curso.get()).addAttribute("filas", filas).addAttribute("errores", errores);
            m.addAttribute("raw", p);
            return "notas";
        }
        calificaciones.saveAll(filas);
        ra.addFlashAttribute("ok", "Calificaciones guardadas.");
        return "redirect:/cursos/" + id + "/notas";
    }

    private Double leer(Map<String, String> p, String clave, Calificacion c, int n, List<String> errores) {
        String v = p.get(clave);
        if (v == null || v.isBlank()) return null;
        v = v.trim().replace(',', '.');
        if (!v.matches("\\d{1,3}(\\.\\d{1,2})?") || Double.parseDouble(v) > 100) {
            errores.add(c.estudiante + ": la nota " + n + " debe ser un número entre 0 y 100.");
            return null;
        }
        return Double.valueOf(v);
    }

    // ---------- Reporte y eliminación ----------

    @GetMapping("/cursos/{id}/reporte")
    public String reporte(@PathVariable Long id, Model m, RedirectAttributes ra) {
        var curso = cursos.findById(id);
        if (curso.isEmpty()) {
            ra.addFlashAttribute("error", "El curso no existe.");
            return "redirect:/";
        }
        var filas = calificaciones.findByCursoIdOrderByEstudianteApellidosAscEstudianteNombresAsc(id);
        m.addAttribute("curso", curso.get()).addAttribute("filas", filas);
        m.addAttribute("aprobados", filas.stream().filter(c -> "APROBADO".equals(c.getEstado())).count());
        m.addAttribute("reprobados", filas.stream().filter(c -> "REPROBADO".equals(c.getEstado())).count());
        m.addAttribute("pendientes", filas.stream().filter(c -> "PENDIENTE".equals(c.getEstado())).count());
        m.addAttribute("minimo", (int) Calificacion.MINIMO_APROBACION);
        return "reporte";
    }

    @PostMapping("/cursos/{id}/eliminar")
    @Transactional
    public String eliminar(@PathVariable Long id, RedirectAttributes ra) {
        calificaciones.deleteByCursoId(id);
        cursos.deleteById(id);
        ra.addFlashAttribute("ok", "Curso eliminado.");
        return "redirect:/";
    }
}
