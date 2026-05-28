package ru.composerdesk.controller.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.composerdesk.service.DatamuseService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rhymes")
@Tag(name = "Рифмы", description = "Поиск рифм через Datamuse API")
public class DatamuseRestController {
    private final DatamuseService service;

    public DatamuseRestController(DatamuseService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Найти рифмы к слову")
    public ResponseEntity<Map<String, Object>> getRhymes(@RequestParam String word) {
        List<String> rhymes = service.getRhymes(word);
        return ResponseEntity.ok(Map.of(
                "word", word,
                "rhymes", rhymes,
                "count", rhymes.size()
        ));
    }
}