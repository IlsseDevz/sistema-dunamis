package com.dunamis.sistema.controller.web;

import com.dunamis.sistema.dto.request.ChangePasswordRequest;
import com.dunamis.sistema.dto.request.ProfileUpdateRequest;
import com.dunamis.sistema.entity.User;
import com.dunamis.sistema.entity.enums.ChurchSituation;
import com.dunamis.sistema.exception.AlreadyEnrolledException;
import com.dunamis.sistema.exception.DuplicateContactException;
import com.dunamis.sistema.exception.DuplicateEmailException;
import com.dunamis.sistema.exception.InvalidPasswordException;
import com.dunamis.sistema.repository.ChurchFunctionRepository;
import com.dunamis.sistema.service.BibleSchoolEnrollmentService;
import com.dunamis.sistema.service.CurrentUserService;
import com.dunamis.sistema.service.MemberProfileService;
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
@RequestMapping("/dashboard")
public class DashboardController {

    private final CurrentUserService currentUserService;
    private final MemberProfileService memberProfileService;
    private final BibleSchoolEnrollmentService bibleSchoolEnrollmentService;
    private final ChurchFunctionRepository churchFunctionRepository;

    public DashboardController(
            CurrentUserService currentUserService,
            MemberProfileService memberProfileService,
            BibleSchoolEnrollmentService bibleSchoolEnrollmentService,
            ChurchFunctionRepository churchFunctionRepository
    ) {
        this.currentUserService = currentUserService;
        this.memberProfileService = memberProfileService;
        this.bibleSchoolEnrollmentService = bibleSchoolEnrollmentService;
        this.churchFunctionRepository = churchFunctionRepository;
    }

    @GetMapping
    public String dashboard(Model model) {
        User user = currentUserService.getCurrentUserEntity();
        model.addAttribute("user", user);
        model.addAttribute("enrolled", bibleSchoolEnrollmentService.isCurrentUserEnrolled());
        model.addAttribute("activeMenu", "dashboard");
        return "dashboard/index";
    }

    @GetMapping("/perfil")
    public String profile(Model model) {
        User user = currentUserService.getCurrentUserEntity();
        model.addAttribute("user", user);
        model.addAttribute("enrolled", bibleSchoolEnrollmentService.isCurrentUserEnrolled());
        model.addAttribute("activeMenu", "perfil");
        return "dashboard/perfil";
    }

    @GetMapping("/perfil/editar")
    public String editProfileForm(Model model) {
        User user = currentUserService.getCurrentUserEntity();
        if (!model.containsAttribute("profileUpdate")) {
            model.addAttribute("profileUpdate", memberProfileService.toProfileUpdateRequest(user));
        }
        addProfileFormAttributes(model);
        model.addAttribute("activeMenu", "editar");
        return "dashboard/editar-perfil";
    }

    @PostMapping("/perfil/editar")
    public String editProfile(
            @Valid @ModelAttribute("profileUpdate") ProfileUpdateRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            addProfileFormAttributes(model);
            model.addAttribute("activeMenu", "editar");
            return "dashboard/editar-perfil";
        }

        try {
            memberProfileService.updateProfile(request);
            redirectAttributes.addFlashAttribute("successMessage", "Perfil actualizado com sucesso.");
            return "redirect:/dashboard/perfil";
        } catch (DuplicateContactException ex) {
            bindingResult.rejectValue("contacto", "duplicate.contacto", ex.getMessage());
        } catch (DuplicateEmailException ex) {
            bindingResult.rejectValue("email", "duplicate.email", ex.getMessage());
        }

        addProfileFormAttributes(model);
        model.addAttribute("activeMenu", "editar");
        return "dashboard/editar-perfil";
    }

    @GetMapping("/alterar-password")
    public String changePasswordForm(Model model) {
        if (!model.containsAttribute("changePassword")) {
            model.addAttribute("changePassword", new ChangePasswordRequest());
        }
        model.addAttribute("enrolled", bibleSchoolEnrollmentService.isCurrentUserEnrolled());
        model.addAttribute("activeMenu", "password");
        return "dashboard/alterar-password";
    }

    @PostMapping("/alterar-password")
    public String changePassword(
            @Valid @ModelAttribute("changePassword") ChangePasswordRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("enrolled", bibleSchoolEnrollmentService.isCurrentUserEnrolled());
            model.addAttribute("activeMenu", "password");
            return "dashboard/alterar-password";
        }

        try {
            memberProfileService.changePassword(request);
            redirectAttributes.addFlashAttribute("successMessage", "Password alterada com sucesso.");
            return "redirect:/dashboard/perfil";
        } catch (InvalidPasswordException ex) {
            bindingResult.rejectValue("currentPassword", "invalid.password", ex.getMessage());
        }

        model.addAttribute("enrolled", bibleSchoolEnrollmentService.isCurrentUserEnrolled());
        model.addAttribute("activeMenu", "password");
        request.setCurrentPassword(null);
        request.setNewPassword(null);
        request.setConfirmNewPassword(null);
        return "dashboard/alterar-password";
    }

    @PostMapping("/inscrever-escola")
    public String enrollInBibleSchool(RedirectAttributes redirectAttributes) {
        try {
            bibleSchoolEnrollmentService.enrollCurrentUser();
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Inscrição na Escola Bíblica realizada com sucesso! Bem-vindo ao SIGA."
            );
            return "redirect:/siga";
        } catch (AlreadyEnrolledException ex) {
            redirectAttributes.addFlashAttribute("infoMessage", ex.getMessage());
            return "redirect:/siga";
        }
    }

    private void addProfileFormAttributes(Model model) {
        model.addAttribute("churchSituations", ChurchSituation.values());
        model.addAttribute(
                "churchFunctions",
                churchFunctionRepository.findAllByActiveTrueOrderByDisplayOrderAsc()
        );
        model.addAttribute("enrolled", bibleSchoolEnrollmentService.isCurrentUserEnrolled());
    }
}
