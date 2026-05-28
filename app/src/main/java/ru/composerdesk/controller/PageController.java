package ru.composerdesk.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@Controller
public class PageController {
    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("error", "Неверный логин или пароль");
        }
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "auth/register";
    }

    @GetMapping("/account")
    public String accountPage() {
        return "account";
    }

    @GetMapping("/home")
    public String homePage() {
        return "home";
    }

    @GetMapping("/tracks/{id}")
    public String trackPage(@PathVariable UUID id, Model model) {
        model.addAttribute("trackId", id.toString());
        return "tracks/edit";
    }

    @GetMapping("/tracks")
    public String tracksListPage() {
        return "tracks/list";
    }

    @GetMapping("/artists")
    public String artistsPage() {
        return "artists";
    }

    @GetMapping("/releases")
    public String releasesPage() {
        return "releases";
    }

    @GetMapping("/releases/{id}")
    public String releasePage(@PathVariable UUID id, Model model) {
        model.addAttribute("releaseId", id.toString());
        return "releases/edit";
    }
}