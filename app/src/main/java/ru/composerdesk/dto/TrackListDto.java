package ru.composerdesk.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class TrackListDto {
    private UUID id;
    private String title;
    private String coverUrl;
    private String audioUrl;
    private List<ArtistRef> artists;
    private List<TagRef> tags;
    private String updatedAt;

    @Getter
    @AllArgsConstructor
    public static class ArtistRef {
        private UUID id;
        private String name;
    }

    @Getter
    @AllArgsConstructor
    public static class TagRef {
        private UUID id;
        private String name;
        private String color;
    }
}