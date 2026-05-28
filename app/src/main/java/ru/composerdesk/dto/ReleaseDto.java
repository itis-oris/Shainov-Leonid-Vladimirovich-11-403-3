package ru.composerdesk.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class ReleaseDto {
    private UUID id;
    private String title;
    private String description;
    private String coverUrl;
    private String type;
    private List<TrackRef> tracks;
    private List<ArtistRef> artists;
    private List<AttachmentRef> attachments;

    @Getter
    @AllArgsConstructor
    public static class TrackRef {
        private UUID id;
        private String title;
        private int position;
        private String audioUrl;
        private List<ArtistRef> artists;
    }

    @Getter
    @AllArgsConstructor
    public static class ArtistRef {
        private UUID id;
        private String name;
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