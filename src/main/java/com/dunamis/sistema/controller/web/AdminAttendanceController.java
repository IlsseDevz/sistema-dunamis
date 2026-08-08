package com.dunamis.sistema.controller.web;

import com.dunamis.sistema.dto.request.AttendanceBatchRequest;
import com.dunamis.sistema.entity.Inscricao;
import com.dunamis.sistema.service.AdminAttendanceService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/admin/presencas")
public class AdminAttendanceController {

    private final AdminAttendanceService adminAttendanceService;

    public AdminAttendanceController(AdminAttendanceService adminAttendanceService) {
        this.adminAttendanceService = adminAttendanceService;
    }

    @GetMapping
    public String list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model
    ) {
        model.addAttribute("presencas", adminAttendanceService.listTurmaAttendances(date));
        model.addAttribute("filterDate", date);
        model.addAttribute("activeMenu", "presencas");
        return "admin/presencas/list";
    }

    @GetMapping("/registar")
    public String registerForm(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model
    ) {
        if (!model.containsAttribute("batch")) {
            model.addAttribute("batch", adminAttendanceService.buildBatchRequest(date));
        }
        List<Inscricao> enrollments = adminAttendanceService.listActiveEnrollments();
        model.addAttribute("enrollments", enrollments);
        model.addAttribute("activeMenu", "presencas");
        return "admin/presencas/registar";
    }

    @PostMapping("/registar")
    public String register(
            @Valid @ModelAttribute("batch") AttendanceBatchRequest batch,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("enrollments", adminAttendanceService.listActiveEnrollments());
            model.addAttribute("activeMenu", "presencas");
            return "admin/presencas/registar";
        }

        adminAttendanceService.saveBatch(batch);
        redirectAttributes.addFlashAttribute("successMessage", "Presenças registadas com sucesso.");
        return "redirect:/admin/presencas?date=" + batch.getAttendanceDate();
    }

    @PostMapping("/{id}/remover")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        adminAttendanceService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Presença removida com sucesso.");
        return "redirect:/admin/presencas";
    }
}
