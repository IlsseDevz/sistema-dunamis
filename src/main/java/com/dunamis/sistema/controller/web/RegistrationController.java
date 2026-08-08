package com.dunamis.sistema.controller.web;

import com.dunamis.sistema.dto.request.MemberRegistrationRequest;
import com.dunamis.sistema.entity.enums.ChurchSituation;
import com.dunamis.sistema.exception.DuplicateContactException;
import com.dunamis.sistema.exception.DuplicateEmailException;
import com.dunamis.sistema.repository.ChurchFunctionRepository;
import com.dunamis.sistema.service.MemberRegistrationService;
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
@RequestMapping("/cadastro")
public class RegistrationController {

    private final MemberRegistrationService memberRegistrationService;
    private final ChurchFunctionRepository churchFunctionRepository;

    public RegistrationController(
            MemberRegistrationService memberRegistrationService,
            ChurchFunctionRepository churchFunctionRepository
    ) {
        this.memberRegistrationService = memberRegistrationService;
        this.churchFunctionRepository = churchFunctionRepository;
    }

    @GetMapping
    public String showRegistrationForm(Model model) {
        prepareFormModel(model, new MemberRegistrationRequest());
        return "auth/cadastro";
    }

    @PostMapping
    public String register(
            @Valid @ModelAttribute("registration") MemberRegistrationRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            prepareFormModel(model, request);
            return "auth/cadastro";
        }

        try {
            memberRegistrationService.register(request);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Cadastro realizado com sucesso! Faça login com o seu contacto."
            );
            return "redirect:/login?registered";
        } catch (DuplicateContactException ex) {
            bindingResult.rejectValue("contacto", "duplicate.contacto", ex.getMessage());
        } catch (DuplicateEmailException ex) {
            bindingResult.rejectValue("email", "duplicate.email", ex.getMessage());
        }

        prepareFormModel(model, request);
        request.setPassword(null);
        request.setConfirmPassword(null);
        return "auth/cadastro";
    }

    private void prepareFormModel(Model model, MemberRegistrationRequest request) {
        model.addAttribute("registration", request);
        model.addAttribute("churchSituations", ChurchSituation.values());
        model.addAttribute(
                "churchFunctions",
                churchFunctionRepository.findAllByActiveTrueOrderByDisplayOrderAsc()
        );
    }
}
