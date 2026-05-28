package ru.composerdesk.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.util.*;

@Entity
@Table(name = "releases")
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@SQLRestriction("deleted = false")
public class Release {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String coverPath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReleaseType type;

    private LocalDate releaseDate;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToMany
    @JoinTable(
            name = "release_artists",
            joinColumns = @JoinColumn(name = "release_id"),
            inverseJoinColumns = @JoinColumn(name = "artist_id")
    )
    private Set<Artist> artists = new HashSet<>();

    @OneToMany(mappedBy = "release", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<ReleaseTrack> releaseTracks = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "release_tags",
            joinColumns = @JoinColumn(name = "release_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "release_attachments",
            joinColumns = @JoinColumn(name = "release_id"),
            inverseJoinColumns = @JoinColumn(name = "attachment_id")
    )
    @BatchSize(size = 20)
    private Set<Attachment> attachments = new HashSet<>();

    @Column(nullable = false)
    private boolean deleted = false;
}

