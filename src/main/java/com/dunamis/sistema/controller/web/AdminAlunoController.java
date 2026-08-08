package com.dunamis.sistema.controller.web;

import com.dunamis.sistema.entity.Inscricao;
import com.dunamis.sistema.service.AdminAlunoService;
import com.dunamis.sistema.service.AdminBibleSchoolService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/alunos")
public class AdminAlunoController {

    private final AdminAlunoService adminAlunoService;
    private final AdminBibleSchoolService adminBibleSchoolService;

    public AdminAlunoController(AdminAlunoService adminAlunoService, AdminBibleSchoolService adminBibleSchoolService) {
        this.adminAlunoService = adminAlunoService;
        this.adminBibleSchoolService = adminBibleSchoolService;
    }

    @GetMapping
    public String listStudents(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            Model model
    ) {
        Page<Inscricao> alunos = adminAlunoService.listActiveStudents(search, page);
        model.addAttribute("alunos", alunos);
        model.addAttribute("search", search);
        model.addAttribute("turma", adminBibleSchoolService.getActiveTurma());
        model.addAttribute("activeMenu", "alunos");
        return "admin/alunos/list";
    }
}
