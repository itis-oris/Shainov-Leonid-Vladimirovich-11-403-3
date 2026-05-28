package ru.composerdesk.controller.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.composerdesk.model.Account;
import ru.composerdesk.model.Role;
import ru.composerdesk.service.AccountService;
import ru.composerdesk.repository.AccountRepository;

import java.util.Map;

@RestController
@RequestMapping("/api/http")
@Tag(name = "Тестовые ручки", description = "Для тестирования через HTTP-файлы")
public class HttpTestController {
    private final AccountService accountService;
    private final AccountRepository accountRepository;

    public HttpTestController(AccountService accountService, AccountRepository accountRepository) {
        this.accountService = accountService;
        this.accountRepository = accountRepository;
    }

    @PostMapping("/register")
    @Operation(summary = "Регистрация (для тестов)")
    public ResponseEntity<Map<String, String>> register(@RequestBody Map<String, String> body) {
        accountService.register(
                body.get("username"),
                body.get("password"),
                Role.USER);
        return ResponseEntity.ok(Map.of("message", "Registered"));
    }

    @PostMapping("/login")
    @Operation(summary = "Логин (для тестов)")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> body) {
        Account account = accountRepository.findByUsername(body.get("username"))
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(Map.of(
                "accountId", account.getId().toString(),
                "username", account.getUsername()
        ));
    }
}