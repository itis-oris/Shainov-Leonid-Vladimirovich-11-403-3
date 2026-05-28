package ru.composerdesk.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ArtistFilterDto {
    private String search;
    private String sortBy;
    private String sortDirection;
}