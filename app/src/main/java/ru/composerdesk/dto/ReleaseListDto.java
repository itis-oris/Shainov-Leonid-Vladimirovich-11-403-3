package ru.composerdesk.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class ReleaseListDto {
    private UUID id;
    private String title;
    private String coverUrl;
    private String type;
    private String artistNames;
    private int trackCount;
}
