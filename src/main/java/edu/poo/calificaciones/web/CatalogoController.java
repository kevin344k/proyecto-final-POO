package edu.poo.calificaciones.web;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

/** CRUD genérico: las subclases definen campos, creación y asignación de valores. */
public abstract class CatalogoController<T> {
    public record Campo(String name, String label) {}

    private final JpaRepository<T, Long> repo;
    private final String base;
    private final String titulo;
    private final List<Campo> campos;

    protected CatalogoController(JpaRepository<T, Long> repo, String base, String titulo, List<Campo> campos) {
        this.repo = repo;
        this.base = base;
        this.titulo = titulo;
        this.campos = campos;
    }

    protected abstract T crear();

    /** Copia y valida los parámetros; lanza IllegalArgumentException con el mensaje de error. */
    protected abstract void aplicar(T item, Map<String, String> p);

    protected void extras(Model model) {}

    protected static String requerido(Map<String, String> p, String clave, String etiqueta) {
        String v = p.get(clave);
        if (v == null || v.isBlank()) throw new IllegalArgumentException("El campo '" + etiqueta + "' es obligatorio.");
        v = v.trim().replaceAll("\\s+", " ");
        if (v.length() > 100) throw new IllegalArgumentException("El campo '" + etiqueta + "' es demasiado largo (máx. 100).");
        return v;
    }

    protected static String nombrePropio(Map<String, String> p, String clave, String etiqueta) {
        String v = requerido(p, clave, etiqueta);
        if (!v.matches("[\\p{L} .'-]+"))
            throw new IllegalArgumentException("El campo '" + etiqueta + "' solo admite letras.");
        return v;
    }

    private Model base(Model m) {
        m.addAttribute("base", base).addAttribute("titulo", titulo).addAttribute("campos", campos);
        extras(m);
        return m;
    }

    @GetMapping
    public String listar(Model m) {
        base(m).addAttribute("items", repo.findAll());
        return "catalogo-lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model m) {
        base(m).addAttribute("item", crear());
        return "catalogo-form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model m, RedirectAttributes ra) {
        var item = repo.findById(id);
        if (item.isEmpty()) {
            ra.addFlashAttribute("error", "El registro no existe.");
            return "redirect:/" + base;
        }
        base(m).addAttribute("item", item.get());
        return "catalogo-form";
    }

    @PostMapping("/guardar")
    public String guardar(@RequestParam Map<String, String> p, Model m, RedirectAttributes ra) {
        T item = crear();
        String id = p.get("id");
        try {
            if (id != null && !id.isBlank()) {
                item = repo.findById(Long.valueOf(id)).orElseThrow(() -> new IllegalArgumentException("El registro no existe."));
            }
            aplicar(item, p);
            repo.save(item);
        } catch (IllegalArgumentException | DataIntegrityViolationException e) {
            String msg = e instanceof DataIntegrityViolationException ? "Ya existe un registro igual." : e.getMessage();
            base(m).addAttribute("item", item).addAttribute("error", msg);
            return "catalogo-form";
        }
        ra.addFlashAttribute("ok", "Registro guardado.");
        return "redirect:/" + base;
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes ra) {
        try {
            repo.deleteById(id);
            repo.flush();
            ra.addFlashAttribute("ok", "Registro eliminado.");
        } catch (DataIntegrityViolationException e) {
            ra.addFlashAttribute("error", "No se puede eliminar: está siendo usado por otros registros.");
        }
        return "redirect:/" + base;
    }
}
