package com.dunamis.sistema.controller.web;

import com.dunamis.sistema.dto.request.TurmaUpdateRequest;
import com.dunamis.sistema.entity.Turma;
import com.dunamis.sistema.entity.enums.TurmaStatus;
import com.dunamis.sistema.service.TurmaService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/turma")
public class AdminTurmaController {

    private final TurmaService turmaService;

    public AdminTurmaController(TurmaService turmaService) {
        this.turmaService = turmaService;
    }

    @GetMapping
    public String turmaPage(Model model) {
        Turma turma = turmaService.getCurrentTurma();
        if (!model.containsAttribute("turmaUpdate")) {
            model.addAttribute("turmaUpdate", turmaService.toUpdateRequest(turma));
        }
        model.addAttribute("turma", turma);
        model.addAttribute("turmaStatuses", TurmaStatus.values());
        model.addAttribute("activeMenu", "turma");
        return "admin/turma/index";
    }

    @PostMapping
    public String updateTurma(
            @Valid @ModelAttribute("turmaUpdate") TurmaUpdateRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        Turma turma = turmaService.getCurrentTurma();

        if (bindingResult.hasErrors()) {
            model.addAttribute("turma", turma);
            model.addAttribute("turmaStatuses", TurmaStatus.values());
            model.addAttribute("activeMenu", "turma");
            return "admin/turma/index";
        }

        turmaService.updateTurma(turma.getId(), request);
        redirectAttributes.addFlashAttribute("successMessage", "Turma actualizada com sucesso.");
        return "redirect:/admin/turma";
    }
}
