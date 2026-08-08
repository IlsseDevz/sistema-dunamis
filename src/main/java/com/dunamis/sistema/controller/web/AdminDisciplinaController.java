package com.dunamis.sistema.controller.web;

import com.dunamis.sistema.dto.request.DisciplinaRequest;
import com.dunamis.sistema.exception.DisciplinaInUseException;
import com.dunamis.sistema.exception.DuplicateDisciplinaException;
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
@RequestMapping("/admin/disciplinas")
public class AdminDisciplinaController {

    private final DisciplinaService disciplinaService;

    public AdminDisciplinaController(DisciplinaService disciplinaService) {
        this.disciplinaService = disciplinaService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("disciplinas", disciplinaService.listCurrentTurmaDisciplinas());
        model.addAttribute("activeMenu", "disciplinas");
        return "admin/disciplinas/list";
    }

    @GetMapping("/nova")
    public String createForm(Model model) {
        model.addAttribute("disciplina", new DisciplinaRequest());
        model.addAttribute("formTitle", "Nova disciplina");
        model.addAttribute("activeMenu", "disciplinas");
        return "admin/disciplinas/form";
    }

    @PostMapping("/nova")
    public String create(
            @Valid @ModelAttribute("disciplina") DisciplinaRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("formTitle", "Nova disciplina");
            model.addAttribute("activeMenu", "disciplinas");
            return "admin/disciplinas/form";
        }

        try {
            disciplinaService.create(request);
            redirectAttributes.addFlashAttribute("successMessage", "Disciplina criada com sucesso.");
            return "redirect:/admin/disciplinas";
        } catch (DuplicateDisciplinaException ex) {
            bindingResult.rejectValue("name", "duplicate.name", ex.getMessage());
        }

        model.addAttribute("formTitle", "Nova disciplina");
        model.addAttribute("activeMenu", "disciplinas");
        return "admin/disciplinas/form";
    }

    @GetMapping("/{id}/editar")
    public String editForm(@PathVariable Long id, Model model) {
        if (!model.containsAttribute("disciplina")) {
            model.addAttribute("disciplina", disciplinaService.toRequest(disciplinaService.getById(id)));
        }
        model.addAttribute("disciplinaId", id);
        model.addAttribute("formTitle", "Editar disciplina");
        model.addAttribute("activeMenu", "disciplinas");
        return "admin/disciplinas/form";
    }

    @PostMapping("/{id}/editar")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("disciplina") DisciplinaRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("disciplinaId", id);
            model.addAttribute("formTitle", "Editar disciplina");
            model.addAttribute("activeMenu", "disciplinas");
            return "admin/disciplinas/form";
        }

        try {
            disciplinaService.update(id, request);
            redirectAttributes.addFlashAttribute("successMessage", "Disciplina actualizada com sucesso.");
            return "redirect:/admin/disciplinas";
        } catch (DuplicateDisciplinaException ex) {
            bindingResult.rejectValue("name", "duplicate.name", ex.getMessage());
        }

        model.addAttribute("disciplinaId", id);
        model.addAttribute("formTitle", "Editar disciplina");
        model.addAttribute("activeMenu", "disciplinas");
        return "admin/disciplinas/form";
    }

    @PostMapping("/{id}/remover")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            disciplinaService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Disciplina removida com sucesso.");
        } catch (DisciplinaInUseException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/disciplinas";
    }
}
