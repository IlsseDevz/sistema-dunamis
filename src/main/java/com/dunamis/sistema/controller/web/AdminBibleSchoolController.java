package com.dunamis.sistema.controller.web;

import com.dunamis.sistema.service.AdminBibleSchoolService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/escola-biblica")
public class AdminBibleSchoolController {

    private final AdminBibleSchoolService adminBibleSchoolService;

    public AdminBibleSchoolController(AdminBibleSchoolService adminBibleSchoolService) {
        this.adminBibleSchoolService = adminBibleSchoolService;
    }

    @GetMapping
    public String overview(Model model) {
        model.addAttribute("overview", adminBibleSchoolService.getOverview());
        model.addAttribute("activeMenu", "escola");
        return "admin/escola-biblica/index";
    }
}
