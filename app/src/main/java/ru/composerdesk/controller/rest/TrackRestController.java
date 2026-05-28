package ru.composerdesk.controller.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.composerdesk.dto.TrackDto;
import ru.composerdesk.dto.TrackFilterDto;
import ru.composerdesk.dto.TrackListDto;
import ru.composerdesk.repository.AccountRepository;
import ru.composerdesk.service.TrackService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/tracks")
@Tag(name = "Треки", description = "Управление треками")
public class TrackRestController {
    private final TrackService trackService;
    private final AccountRepository accountRepository;

    public TrackRestController(TrackService trackService, AccountRepository accountRepository) {
        this.trackService = trackService;
        this.accountRepository = accountRepository;
    }

    private UUID getAccountId(UserDetails principal) {
        return accountRepository.findByUsername(principal.getUsername()).orElseThrow().getId();
    }

    @GetMapping
    @Operation(summary = "Получить все треки")
    public ResponseEntity<List<TrackListDto>> getAll(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam(required = false) UUID excludeId) {
        UUID accountId = getAccountId(principal);
        if (excludeId != null) {
            return ResponseEntity.ok(trackService.getUserTracksExcluding(accountId, excludeId));
        }
        return ResponseEntity.ok(trackService.getUserTracks(accountId));
    }

    @PostMapping
    @Operation(summary = "Создать трек")
    public ResponseEntity<TrackDto> create(@AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(trackService.createTrack(getAccountId(principal)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить трек по ID")
    public ResponseEntity<TrackDto> getOne(@PathVariable UUID id) {
        return ResponseEntity.ok(trackService.getTrack(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить трек")
    public ResponseEntity<TrackDto> update(@PathVariable UUID id,
                                           @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(trackService.updateTrack(id,
                body.get("title"), body.get("description"), body.get("lyrics")));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить трек")
    public ResponseEntity<Map<String, String>> delete(@PathVariable UUID id) {
        trackService.deleteTrack(id);
        return ResponseEntity.ok(Map.of("message", "Track deleted"));
    }

    @PutMapping("/{id}/artists")
    @Operation(summary = "Установить исполнителей трека")
    public ResponseEntity<TrackDto> setArtists(@PathVariable UUID id,
                                               @RequestBody Map<String, List<UUID>> body) {
        return ResponseEntity.ok(trackService.setArtists(id, body.get("artistIds")));
    }

    @PostMapping("/{id}/cover")
    @Operation(summary = "Загрузить обложку")
    public ResponseEntity<TrackDto> uploadCover(@PathVariable UUID id,
                                                @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(trackService.uploadCover(id, file));
    }

    @PostMapping("/{id}/audio")
    @Operation(summary = "Загрузить аудио")
    public ResponseEntity<TrackDto> uploadAudio(@PathVariable UUID id,
                                                @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(trackService.uploadAudio(id, file));
    }

    @PostMapping("/{id}/tags")
    @Operation(summary = "Добавить тег")
    public ResponseEntity<TrackDto> addTag(@PathVariable UUID id,
                                           @AuthenticationPrincipal UserDetails principal,
                                           @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(trackService.addTag(id,
                body.get("name"),
                body.getOrDefault("color", "#808080"),
                getAccountId(principal)));
    }

    @DeleteMapping("/{id}/tags/{tagId}")
    @Operation(summary = "Удалить тег")
    public ResponseEntity<TrackDto> removeTag(@PathVariable UUID id,
                                              @PathVariable UUID tagId) {
        return ResponseEntity.ok(trackService.removeTag(id, tagId));
    }

    @PostMapping("/{id}/attachments")
    @Operation(summary = "Загрузить аттачмент")
    public ResponseEntity<TrackDto> uploadAttachment(@PathVariable UUID id,
                                                     @AuthenticationPrincipal UserDetails principal,
                                                     @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(trackService.uploadAttachment(id, file, getAccountId(principal)));
    }

    @DeleteMapping("/{id}/attachments/{attachmentId}")
    @Operation(summary = "Удалить аттачмент")
    public ResponseEntity<TrackDto> removeAttachment(@PathVariable UUID id,
                                                     @PathVariable UUID attachmentId) {
        return ResponseEntity.ok(trackService.removeAttachment(id, attachmentId));
    }

    @PostMapping("/filter")
    @Operation(summary = "Поиск и фильтрация треков")
    public ResponseEntity<List<TrackListDto>> filter(@AuthenticationPrincipal UserDetails principal,
                                                     @RequestBody TrackFilterDto filter) {
        return ResponseEntity.ok(trackService.getFilteredTracks(getAccountId(principal), filter));
    }

    @GetMapping("/not-in-release/{releaseId}")
    @Operation(summary = "Треки, не входящие в релиз")
    public ResponseEntity<List<TrackListDto>> getTracksNotInRelease(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable UUID releaseId) {
        return ResponseEntity.ok(trackService.getTracksNotInRelease(getAccountId(principal), releaseId));
    }
}