package ru.composerdesk.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class TrackFilterDto {
    private String search;
    private List<UUID> tagIds;
    private String sortBy;
    private String sortDirection;
}