package ru.composerdesk.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class ReleaseFilterDto {
    private String search;
    private List<String> types;
    private String sortBy;
    private String sortDirection;
}