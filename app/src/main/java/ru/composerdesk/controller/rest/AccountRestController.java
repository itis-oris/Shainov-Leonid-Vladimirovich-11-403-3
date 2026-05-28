package ru.composerdesk.controller.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ru.composerdesk.dto.ChangePasswordRequest;
import ru.composerdesk.dto.ChangeUsernameRequest;
import ru.composerdesk.model.Account;
import ru.composerdesk.repository.AccountRepository;
import ru.composerdesk.service.AccountService;

import java.util.Map;

@RestController
@RequestMapping("/api/account")
@Tag(name = "Аккаунт", description = "Управление профилем пользователя")
public class AccountRestController {

    private final AccountService accountService;
    private final AccountRepository accountRepository;

    public AccountRestController(AccountService accountService, AccountRepository accountRepository) {
        this.accountService = accountService;
        this.accountRepository = accountRepository;
    }

    @GetMapping
    @Operation(summary = "Получить данные аккаунта")
    public ResponseEntity<Map<String, String>> getAccount(
            @AuthenticationPrincipal UserDetails principal) {

        Account account = accountRepository.findByUsername(principal.getUsername())
                .orElseThrow();

        return ResponseEntity.ok(Map.of(
                "username", account.getUsername(),
                "role", account.getRole().name()
        ));
    }

    @PutMapping("/username")
    @Operation(summary = "Сменить логин")
    public ResponseEntity<Map<String, String>> changeUsername(
            @AuthenticationPrincipal UserDetails principal,
            @RequestBody ChangeUsernameRequest request) {

        Account account = accountRepository.findByUsername(principal.getUsername())
                .orElseThrow();

        accountService.changeUsername(account.getId(), request);

        return ResponseEntity.ok(Map.of("message", "Username changed successfully"));
    }

    @PutMapping("/password")
    @Operation(summary = "Сменить пароль")
    public ResponseEntity<Map<String, String>> changePassword(
            @AuthenticationPrincipal UserDetails principal,
            @RequestBody ChangePasswordRequest request) {

        Account account = accountRepository.findByUsername(principal.getUsername())
                .orElseThrow();

        accountService.changePassword(account.getId(), request);

        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }

    @DeleteMapping
    @Operation(summary = "Удалить аккаунт")
    public ResponseEntity<Map<String, String>> deleteAccount(
            @AuthenticationPrincipal UserDetails principal) {

        Account account = accountRepository.findByUsername(principal.getUsername())
                .orElseThrow();

        accountService.deleteAccount(account.getId());

        return ResponseEntity.ok(Map.of("message", "Account deleted"));
    }
}