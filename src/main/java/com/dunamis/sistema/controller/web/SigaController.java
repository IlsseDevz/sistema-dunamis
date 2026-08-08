package com.dunamis.sistema.controller.web;

import com.dunamis.sistema.entity.Disciplina;
import com.dunamis.sistema.entity.Inscricao;
import com.dunamis.sistema.entity.Nota;
import com.dunamis.sistema.entity.Presenca;
import com.dunamis.sistema.entity.User;
import com.dunamis.sistema.exception.StudentNotEnrolledException;
import com.dunamis.sistema.service.CurrentUserService;
import com.dunamis.sistema.service.SigaAcademicService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/siga")
public class SigaController {

    private final SigaAcademicService sigaAcademicService;
    private final CurrentUserService currentUserService;

    public SigaController(SigaAcademicService sigaAcademicService, CurrentUserService currentUserService) {
        this.sigaAcademicService = sigaAcademicService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public String dashboard(Model model) {
        prepareCommonModel(model, "dashboard");
        model.addAttribute("summary", sigaAcademicService.buildSummary());
        return "siga/index";
    }

    @GetMapping("/turma")
    public String turma(Model model) {
        Inscricao inscricao = sigaAcademicService.getCurrentEnrollment();
        prepareCommonModel(model, "turma");
        model.addAttribute("inscricao", inscricao);
        model.addAttribute("turma", inscricao.getTurma());
        return "siga/turma";
    }

    @GetMapping("/disciplinas")
    public String disciplinas(Model model) {
        List<Disciplina> disciplinas = sigaAcademicService.getCurrentDisciplinas();
        prepareCommonModel(model, "disciplinas");
        model.addAttribute("disciplinas", disciplinas);
        return "siga/disciplinas";
    }

    @GetMapping("/notas")
    public String notas(Model model) {
        List<Nota> notas = sigaAcademicService.getCurrentGrades();
        prepareCommonModel(model, "notas");
        model.addAttribute("notas", notas);
        model.addAttribute("summary", sigaAcademicService.buildSummary());
        return "siga/notas";
    }

    @GetMapping("/presencas")
    public String presencas(Model model) {
        List<Presenca> presencas = sigaAcademicService.getCurrentAttendances();
        prepareCommonModel(model, "presencas");
        model.addAttribute("presencas", presencas);
        model.addAttribute("summary", sigaAcademicService.buildSummary());
        return "siga/presencas";
    }

    @GetMapping("/media")
    public String media(Model model) {
        prepareCommonModel(model, "media");
        model.addAttribute("summary", sigaAcademicService.buildSummary());
        return "siga/media";
    }

    @GetMapping("/relatorios")
    public String relatorios(Model model) {
        prepareCommonModel(model, "relatorios");
        model.addAttribute("summary", sigaAcademicService.buildSummary());
        model.addAttribute("user", currentUserService.getCurrentUserEntity());
        return "siga/relatorios";
    }

    @ExceptionHandler(StudentNotEnrolledException.class)
    public String handleNotEnrolled(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute(
                "infoMessage",
                "Precisa de se inscrever na Escola Bíblica para aceder ao SIGA."
        );
        return "redirect:/dashboard";
    }

    private void prepareCommonModel(Model model, String activeMenu) {
        User user = currentUserService.getCurrentUserEntity();
        model.addAttribute("user", user);
        model.addAttribute("activeMenu", activeMenu);
    }
}
