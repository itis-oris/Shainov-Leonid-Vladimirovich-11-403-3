package ru.composerdesk.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class TrackDto {
    private UUID id;
    private String title;
    private String description;
    private String lyrics;
    private String coverUrl;
    private String audioUrl;
    private String updatedAt;
    private List<ArtistRef> artists;
    private List<TagRef> tags;
    private List<AttachmentRef> attachments;

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

    @Getter
    @AllArgsConstructor
    public static class AttachmentRef {
        private UUID id;
        private String originalFilename;
        private String contentType;
        private Long fileSize;
        private String s3Key;
    }
}