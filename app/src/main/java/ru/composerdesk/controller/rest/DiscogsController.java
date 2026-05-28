package ru.composerdesk.controller.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.composerdesk.service.DiscogsService;

import java.util.Map;

@RestController
@RequestMapping("/api/discogs")
@Tag(name = "Discogs", description = "Проверка уникальности названий")
public class DiscogsController {
    private final DiscogsService service;

    public DiscogsController(DiscogsService service) {
        this.service = service;
    }

    @GetMapping("/check-track")
    @Operation(summary = "Проверить название трека")
    public ResponseEntity<Map<String, Object>> checkTrack(@RequestParam String title) {
        int count = service.checkTrackTitle(title);
        return ResponseEntity.ok(Map.of("title", title, "count", count,
                "warning", count > 0 ? "Найдено релизов: " + count : "Не найдено"));
    }

    @GetMapping("/check-release")
    @Operation(summary = "Проверить название релиза")
    public ResponseEntity<Map<String, Object>> checkRelease(@RequestParam String title) {
        int count = service.checkReleaseTitle(title);
        return ResponseEntity.ok(Map.of("title", title, "count", count,
                "warning", count > 0 ? "Найдено релизов: " + count : "Не найдено"));
    }

    @GetMapping("/check-artist")
    @Operation(summary = "Проверить имя исполнителя")
    public ResponseEntity<Map<String, Object>> checkArtist(@RequestParam String name) {
        int count = service.checkArtistName(name);
        return ResponseEntity.ok(Map.of("name", name, "count", count,
                "warning", count > 0 ? "Найдено исполнителей: " + count : "Не найдено"));
    }
}