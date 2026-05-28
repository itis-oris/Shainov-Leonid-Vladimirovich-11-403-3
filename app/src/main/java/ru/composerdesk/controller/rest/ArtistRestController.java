package ru.composerdesk.controller.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.composerdesk.dto.ArtistFilterDto;
import ru.composerdesk.model.Artist;
import ru.composerdesk.repository.AccountRepository;
import ru.composerdesk.service.ArtistService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/artists")
@Tag(name = "Исполнители", description = "Управление исполнителями")
public class ArtistRestController {

    private final ArtistService artistService;
    private final AccountRepository accountRepository;

    public ArtistRestController(ArtistService artistService, AccountRepository accountRepository) {
        this.artistService = artistService;
        this.accountRepository = accountRepository;
    }

    private UUID getAccountId(UserDetails principal) {
        return accountRepository.findByUsername(principal.getUsername())
                .orElseThrow().getId();
    }

    @GetMapping
    @Operation(summary = "Получить всех исполнителей")
    public ResponseEntity<List<Artist>> getAll(@AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(artistService.getUserArtists(getAccountId(principal)));
    }

    @PostMapping
    @Operation(summary = "Создать исполнителя")
    public ResponseEntity<Artist> create(@AuthenticationPrincipal UserDetails principal,
                                         @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(artistService.createArtist(getAccountId(principal),
                body.get("name"), body.get("description")));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить исполнителя по ID")
    public ResponseEntity<Artist> getOne(@PathVariable UUID id) {
        return ResponseEntity.ok(artistService.getArtist(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить исполнителя")
    public ResponseEntity<Artist> update(@PathVariable UUID id,
                                         @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(artistService.updateArtist(id,
                body.get("name"), body.get("description")));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить исполнителя")
    public ResponseEntity<Map<String, String>> delete(@PathVariable UUID id) {
        artistService.deleteArtist(id);
        return ResponseEntity.ok(Map.of("message", "Artist deleted"));
    }

    @PostMapping("/{id}/avatar")
    @Operation(summary = "Загрузить аватар")
    public ResponseEntity<Artist> uploadAvatar(@PathVariable UUID id,
                                               @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(artistService.uploadAvatar(id, file));
    }

    @PostMapping("/filter")
    @Operation(summary = "Поиск и фильтрация исполнителей")
    public ResponseEntity<List<Artist>> filter(@AuthenticationPrincipal UserDetails principal,
                                               @RequestBody ArtistFilterDto filter) {
        return ResponseEntity.ok(artistService.getFilteredArtists(getAccountId(principal), filter));
    }
}