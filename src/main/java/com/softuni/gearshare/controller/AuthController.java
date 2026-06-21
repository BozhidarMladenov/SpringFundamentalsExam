package com.softuni.gearshare.controller;

import com.softuni.gearshare.model.dto.RegisterRequest;
import com.softuni.gearshare.model.enums.UserRole;
import com.softuni.gearshare.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/auth/register")
    public String registerForm(Model model) {
        if (!model.containsAttribute("registerRequest")) {
            model.addAttribute("registerRequest", new RegisterRequest());
        }
        model.addAttribute("roles", UserRole.values());
        return "auth/register";
    }

    @PostMapping("/auth/register")
    public String register(@Valid @ModelAttribute("registerRequest") RegisterRequest request,
                            BindingResult bindingResult,
                            Model model) {

        if (request.getPassword() != null && !request.getPassword().equals(request.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "mismatch", "Passwords do not match.");
        }
        if (request.getUsername() != null && userService.usernameTaken(request.getUsername())) {
            bindingResult.rejectValue("username", "taken", "This username is already taken.");
        }
        if (request.getEmail() != null && userService.emailTaken(request.getEmail())) {
            bindingResult.rejectValue("email", "taken", "This email is already registered.");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("roles", UserRole.values());
            return "auth/register";
        }

        userService.register(request);
        return "redirect:/auth/login?registered";
    }

    @GetMapping("/auth/login")
    public String loginForm() {
        return "auth/login";
    }
}
