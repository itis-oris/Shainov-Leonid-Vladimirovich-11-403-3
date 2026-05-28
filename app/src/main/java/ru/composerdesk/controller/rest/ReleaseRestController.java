package ru.composerdesk.controller.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.composerdesk.dto.ReleaseDto;
import ru.composerdesk.dto.ReleaseFilterDto;
import ru.composerdesk.dto.ReleaseListDto;
import ru.composerdesk.repository.AccountRepository;
import ru.composerdesk.service.ReleaseService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/releases")
@Tag(name = "Релизы", description = "Управление релизами и треклистами")
public class ReleaseRestController {
    private final ReleaseService releaseService;
    private final AccountRepository accountRepository;

    public ReleaseRestController(ReleaseService releaseService, AccountRepository accountRepository) {
        this.releaseService = releaseService;
        this.accountRepository = accountRepository;
    }

    private UUID getAccountId(UserDetails principal) {
        return accountRepository.findByUsername(principal.getUsername())
                .orElseThrow().getId();
    }

    @GetMapping
    @Operation(summary = "Получить все релизы")
    public ResponseEntity<List<ReleaseListDto>> getAll(@AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(releaseService.getUserReleases(getAccountId(principal)));
    }

    @PostMapping
    @Operation(summary = "Создать релиз")
    public ResponseEntity<ReleaseDto> create(@AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(releaseService.createRelease(getAccountId(principal)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить релиз по ID")
    public ResponseEntity<ReleaseDto> getOne(@PathVariable UUID id) {
        return ResponseEntity.ok(releaseService.getRelease(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить релиз")
    public ResponseEntity<ReleaseDto> update(@PathVariable UUID id,
                                             @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(releaseService.updateRelease(id,
                body.get("title"), body.get("description"), body.get("type")));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить релиз")
    public ResponseEntity<Map<String, String>> delete(@PathVariable UUID id) {
        releaseService.deleteRelease(id);
        return ResponseEntity.ok(Map.of("message", "Release deleted"));
    }

    @PutMapping("/{id}/artists")
    @Operation(summary = "Установить исполнителей релиза")
    public ResponseEntity<ReleaseDto> setArtists(@PathVariable UUID id,
                                                 @RequestBody Map<String, List<UUID>> body) {
        return ResponseEntity.ok(releaseService.setArtists(id, body.get("artistIds")));
    }

    @PostMapping("/{id}/cover")
    @Operation(summary = "Загрузить обложку")
    public ResponseEntity<ReleaseDto> uploadCover(@PathVariable UUID id,
                                                  @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(releaseService.uploadCover(id, file));
    }

    @PostMapping("/{id}/tracks/{trackId}")
    @Operation(summary = "Добавить трек в релиз")
    public ResponseEntity<ReleaseDto> addTrack(@PathVariable UUID id,
                                               @PathVariable UUID trackId) {
        return ResponseEntity.ok(releaseService.addTrack(id, trackId));
    }

    @DeleteMapping("/{id}/tracks/{trackId}")
    @Operation(summary = "Удалить трек из релиза")
    public ResponseEntity<ReleaseDto> removeTrack(@PathVariable UUID id,
                                                  @PathVariable UUID trackId) {
        return ResponseEntity.ok(releaseService.removeTrack(id, trackId));
    }

    @PutMapping("/{id}/tracks/{trackId}/position")
    @Operation(summary = "Изменить позицию трека")
    public ResponseEntity<ReleaseDto> reorderTrack(@PathVariable UUID id,
                                                   @PathVariable UUID trackId,
                                                   @RequestBody Map<String, Integer> body) {
        return ResponseEntity.ok(releaseService.reorderTracks(id, trackId, body.get("position")));
    }

    @PostMapping("/{id}/attachments")
    @Operation(summary = "Загрузить аттачмент")
    public ResponseEntity<ReleaseDto> uploadAttachment(@PathVariable UUID id,
                                                       @AuthenticationPrincipal UserDetails principal,
                                                       @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(releaseService.uploadAttachment(id, file, getAccountId(principal)));
    }

    @DeleteMapping("/{id}/attachments/{attachmentId}")
    @Operation(summary = "Удалить аттачмент")
    public ResponseEntity<ReleaseDto> removeAttachment(@PathVariable UUID id,
                                                       @PathVariable UUID attachmentId) {
        return ResponseEntity.ok(releaseService.removeAttachment(id, attachmentId));
    }

    @PostMapping("/filter")
    @Operation(summary = "Поиск и фильтрация релизов")
    public ResponseEntity<List<ReleaseListDto>> filter(@AuthenticationPrincipal UserDetails principal,
                                                       @RequestBody ReleaseFilterDto filter) {
        return ResponseEntity.ok(releaseService.getFilteredReleases(getAccountId(principal), filter));
    }
}