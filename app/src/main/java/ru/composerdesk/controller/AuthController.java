package ru.composerdesk.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.composerdesk.dto.RegisterRequest;
import ru.composerdesk.exception.UserAlreadyExistsException;
import ru.composerdesk.model.Role;
import ru.composerdesk.service.AccountService;

@Controller
@RequestMapping("/auth")
public class AuthController {
    private final AccountService accountService;

    public AuthController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           Model model) {
        try {
            accountService.register(username, password, Role.USER);
            return "redirect:/login";
        } catch (UserAlreadyExistsException e) {
            model.addAttribute("error", e.getMessage());
            return "auth/register";
        }
    }
}