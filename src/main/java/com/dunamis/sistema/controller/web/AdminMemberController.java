package com.dunamis.sistema.controller.web;

import com.dunamis.sistema.dto.request.AdminMemberUpdateRequest;
import com.dunamis.sistema.entity.User;
import com.dunamis.sistema.entity.enums.AccountStatus;
import com.dunamis.sistema.entity.enums.ChurchSituation;
import com.dunamis.sistema.exception.DuplicateContactException;
import com.dunamis.sistema.exception.DuplicateEmailException;
import com.dunamis.sistema.exception.OperationNotAllowedException;
import com.dunamis.sistema.repository.ChurchFunctionRepository;
import com.dunamis.sistema.security.SecurityUtils;
import com.dunamis.sistema.service.AdminMemberService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
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

@Controller
@RequestMapping("/admin/membros")
public class AdminMemberController {

    private final AdminMemberService adminMemberService;
    private final ChurchFunctionRepository churchFunctionRepository;
    private final SecurityUtils securityUtils;

    public AdminMemberController(
            AdminMemberService adminMemberService,
            ChurchFunctionRepository churchFunctionRepository,
            SecurityUtils securityUtils
    ) {
        this.adminMemberService = adminMemberService;
        this.churchFunctionRepository = churchFunctionRepository;
        this.securityUtils = securityUtils;
    }

    @GetMapping
    public String listMembers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) ChurchSituation situation,
            @RequestParam(required = false) String bairro,
            @RequestParam(required = false) Boolean baptized,
            @RequestParam(required = false) Long functionId,
            @RequestParam(required = false) AccountStatus accountStatus,
            @RequestParam(defaultValue = "0") int page,
            Model model
    ) {
        Page<User> members = adminMemberService.searchMembers(
                search, situation, bairro, baptized, functionId, accountStatus, page
        );

        model.addAttribute("members", members);
        model.addAttribute("search", search);
        model.addAttribute("situation", situation);
        model.addAttribute("bairro", bairro);
        model.addAttribute("baptized", baptized);
        model.addAttribute("functionId", functionId);
        model.addAttribute("accountStatus", accountStatus);
        model.addAttribute("churchSituations", ChurchSituation.values());
        model.addAttribute("accountStatuses", AccountStatus.values());
        model.addAttribute("churchFunctions", churchFunctionRepository.findAllByActiveTrueOrderByDisplayOrderAsc());
        model.addAttribute("activeMenu", "membros");
        return "admin/membros/list";
    }

    @GetMapping("/{id}")
    public String memberDetail(@PathVariable Long id, Model model) {
        User member = adminMemberService.getMemberById(id);
        model.addAttribute("member", member);
        model.addAttribute("isStudent", adminMemberService.isStudent(id));
        model.addAttribute("currentUserId", securityUtils.getCurrentUserId());
        model.addAttribute("activeMenu", "membros");
        return "admin/membros/detalhe";
    }

    @GetMapping("/{id}/editar")
    public String editMemberForm(@PathVariable Long id, Model model) {
        User member = adminMemberService.getMemberById(id);
        if (!model.containsAttribute("memberUpdate")) {
            model.addAttribute("memberUpdate", adminMemberService.toUpdateRequest(member));
        }
        model.addAttribute("memberId", id);
        addEditFormAttributes(model);
        model.addAttribute("activeMenu", "membros");
        return "admin/membros/editar";
    }

    @PostMapping("/{id}/editar")
    public String editMember(
            @PathVariable Long id,
            @Valid @ModelAttribute("memberUpdate") AdminMemberUpdateRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("memberId", id);
            addEditFormAttributes(model);
            model.addAttribute("activeMenu", "membros");
            return "admin/membros/editar";
        }

        try {
            adminMemberService.updateMember(id, request);
            redirectAttributes.addFlashAttribute("successMessage", "Membro actualizado com sucesso.");
            return "redirect:/admin/membros/" + id;
        } catch (DuplicateContactException ex) {
            bindingResult.rejectValue("contacto", "duplicate.contacto", ex.getMessage());
        } catch (DuplicateEmailException ex) {
            bindingResult.rejectValue("email", "duplicate.email", ex.getMessage());
        }

        model.addAttribute("memberId", id);
        addEditFormAttributes(model);
        model.addAttribute("activeMenu", "membros");
        return "admin/membros/editar";
    }

    @PostMapping("/{id}/desactivar")
    public String deactivateMember(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            adminMemberService.deactivateMember(id);
            redirectAttributes.addFlashAttribute("successMessage", "Conta desactivada com sucesso.");
        } catch (OperationNotAllowedException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/membros/" + id;
    }

    @PostMapping("/{id}/activar")
    public String activateMember(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        adminMemberService.activateMember(id);
        redirectAttributes.addFlashAttribute("successMessage", "Conta activada com sucesso.");
        return "redirect:/admin/membros/" + id;
    }

    private void addEditFormAttributes(Model model) {
        model.addAttribute("churchSituations", ChurchSituation.values());
        model.addAttribute("accountStatuses", AccountStatus.values());
        model.addAttribute("churchFunctions", churchFunctionRepository.findAllByActiveTrueOrderByDisplayOrderAsc());
    }
}
