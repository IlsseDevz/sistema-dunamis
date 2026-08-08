package com.dunamis.sistema.controller.web;

import com.dunamis.sistema.service.AdminDashboardService;
import com.dunamis.sistema.service.CurrentUserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AdminDashboardService adminDashboardService;
    private final CurrentUserService currentUserService;

    public AdminController(AdminDashboardService adminDashboardService, CurrentUserService currentUserService) {
        this.adminDashboardService = adminDashboardService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("stats", adminDashboardService.getDashboardStats());
        model.addAttribute("user", currentUserService.getCurrentUserEntity());
        model.addAttribute("activeMenu", "dashboard");
        return "admin/index";
    }

    @GetMapping("/configuracoes")
    public String configuracoes(Model model) {
        model.addAttribute("activeMenu", "configuracoes");
        model.addAttribute("sectionTitle", "Configurações");
        model.addAttribute("sectionMessage", "Configurações do sistema serão expandidas em fases futuras.");
        return "admin/placeholder";
    }
}
