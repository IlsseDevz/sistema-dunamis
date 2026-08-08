package com.dunamis.sistema.controller.web;

import com.dunamis.sistema.dto.request.GradeRequest;
import com.dunamis.sistema.entity.enums.GradeType;
import com.dunamis.sistema.exception.DuplicateGradeException;
import com.dunamis.sistema.service.AdminGradeService;
import com.dunamis.sistema.service.DisciplinaService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/notas")
public class AdminGradeController {

    private final AdminGradeService adminGradeService;
    private final DisciplinaService disciplinaService;

    public AdminGradeController(AdminGradeService adminGradeService, DisciplinaService disciplinaService) {
        this.adminGradeService = adminGradeService;
        this.disciplinaService = disciplinaService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("notas", adminGradeService.listTurmaGrades());
        model.addAttribute("activeMenu", "notas");
        return "admin/notas/list";
    }

    @GetMapping("/nova")
    public String createForm(Model model) {
        model.addAttribute("grade", new GradeRequest());
        addFormAttributes(model, "Nova nota");
        return "admin/notas/form";
    }

    @PostMapping("/nova")
    public String create(
            @Valid @ModelAttribute("grade") GradeRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            addFormAttributes(model, "Nova nota");
            return "admin/notas/form";
        }

        try {
            adminGradeService.create(request);
            redirectAttributes.addFlashAttribute("successMessage", "Nota registada com sucesso.");
            return "redirect:/admin/notas";
        } catch (DuplicateGradeException ex) {
            bindingResult.reject("duplicate.grade", ex.getMessage());
        }

        addFormAttributes(model, "Nova nota");
        return "admin/notas/form";
    }

    @GetMapping("/{id}/editar")
    public String editForm(@PathVariable Long id, Model model) {
        if (!model.containsAttribute("grade")) {
            model.addAttribute("grade", adminGradeService.toRequest(adminGradeService.getById(id)));
        }
        model.addAttribute("gradeId", id);
        addFormAttributes(model, "Editar nota");
        return "admin/notas/form";
    }

    @PostMapping("/{id}/editar")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("grade") GradeRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("gradeId", id);
            addFormAttributes(model, "Editar nota");
            return "admin/notas/form";
        }

        try {
            adminGradeService.update(id, request);
            redirectAttributes.addFlashAttribute("successMessage", "Nota actualizada com sucesso.");
            return "redirect:/admin/notas";
        } catch (DuplicateGradeException ex) {
            bindingResult.reject("duplicate.grade", ex.getMessage());
        }

        model.addAttribute("gradeId", id);
        addFormAttributes(model, "Editar nota");
        return "admin/notas/form";
    }

    @PostMapping("/{id}/remover")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        adminGradeService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Nota removida com sucesso.");
        return "redirect:/admin/notas";
    }

    private void addFormAttributes(Model model, String title) {
        model.addAttribute("formTitle", title);
        model.addAttribute("enrollments", adminGradeService.listActiveEnrollments());
        model.addAttribute("disciplinas", disciplinaService.listCurrentTurmaDisciplinas());
        model.addAttribute("gradeTypes", GradeType.values());
        model.addAttribute("activeMenu", "notas");
    }
}
