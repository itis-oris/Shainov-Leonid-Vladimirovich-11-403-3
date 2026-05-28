package ru.composerdesk.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.composerdesk.dto.TrackDto;
import ru.composerdesk.dto.TrackFilterDto;
import ru.composerdesk.dto.TrackListDto;
import ru.composerdesk.exception.TrackNotFoundException;
import ru.composerdesk.model.*;
import ru.composerdesk.repository.*;

import jakarta.persistence.criteria.Predicate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TrackService {

    private static final Logger log = LoggerFactory.getLogger(TrackService.class);

    private final TrackRepository trackRepository;
    private final ArtistRepository artistRepository;
    private final TagService tagService;
    private final AttachmentRepository attachmentRepository;
    private final FileService fileService;
    private final ReleaseTrackRepository releaseTrackRepository;
    private final AttachmentService attachmentService;
    private final AccountRepository accountRepository;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("title", "createdDate", "updatedDate");

    public TrackService(TrackRepository trackRepository,
                        ArtistRepository artistRepository,
                        TagService tagService,
                        AttachmentRepository attachmentRepository,
                        FileService fileService,
                        ReleaseTrackRepository releaseTrackRepository,
                        AttachmentService attachmentService,
                        AccountRepository accountRepository) {
        this.trackRepository = trackRepository;
        this.artistRepository = artistRepository;
        this.tagService = tagService;
        this.attachmentRepository = attachmentRepository;
        this.fileService = fileService;
        this.releaseTrackRepository = releaseTrackRepository;
        this.attachmentService = attachmentService;
        this.accountRepository = accountRepository;
    }

    public List<TrackListDto> getUserTracks(UUID accountId) {
        return trackRepository.findByAccountIdWithArtists(accountId).stream()
                .map(this::toListDto)
                .collect(Collectors.toList());
    }

    public List<TrackListDto> getUserTracksExcluding(UUID accountId, UUID excludeId) {
        return getUserTracks(accountId).stream()
                .filter(t -> !t.getId().equals(excludeId))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TrackDto getTrack(UUID id) {
        Track track = trackRepository.findByIdWithAll(id)
                .orElseThrow(() -> new TrackNotFoundException(id.toString()));
        return toDto(track);
    }

    public List<Track> getTracksByAccount(UUID accountId) {
        return trackRepository.findByAccountId(accountId);
    }

    public TrackDto createTrack(UUID accountId) {
        log.info("Creating track for account: {}", accountId);
        Track track = new Track();
        track.setTitle("Новый трек");
        track.setDescription("");
        track.setLyrics("");
        track.setAccount(accountRepository.getReferenceById(accountId));
        track = trackRepository.save(track);
        return toDto(track);
    }

    public TrackDto updateTrack(UUID id, String title, String description, String lyrics) {
        Track track = trackRepository.findById(id)
                .orElseThrow(() -> new TrackNotFoundException(id.toString()));
        if (title != null) track.setTitle(title);
        if (description != null) track.setDescription(description);
        if (lyrics != null) track.setLyrics(lyrics);
        trackRepository.save(track);
        return toDto(track);
    }

    @Transactional
    public void deleteTrack(UUID id) {
        Track track = trackRepository.findById(id)
                .orElseThrow(() -> new TrackNotFoundException(id.toString()));
        deleteTrack(track);
    }

    @Transactional
    public void deleteTrack(Track track) {
        log.warn("Deleting track: {}", track.getId());
        track.setDeleted(true);
        trackRepository.save(track);
        releaseTrackRepository.deleteByTrackId(track.getId());

        Set<Tag> tags = new HashSet<>(track.getTags());
        track.getTags().clear();
        trackRepository.save(track);
        for (Tag tag : tags) {
            if (tag.getTracks().stream().allMatch(Track::isDeleted)) {
                tagService.deleteTag(tag);
            }
        }

        Set<Attachment> attachments = new HashSet<>(track.getAttachments());
        track.getAttachments().clear();
        trackRepository.save(track);
        for (Attachment attachment : attachments) {
            attachmentService.deleteAttachment(attachment);
        }
    }

    @Transactional
    public TrackDto setArtists(UUID trackId, List<UUID> artistIds) {
        Track track = trackRepository.findByIdWithAll(trackId)
                .orElseThrow(() -> new TrackNotFoundException(trackId.toString()));
        Set<Artist> artists = new HashSet<>(artistRepository.findAllById(artistIds));
        track.setArtists(artists);
        trackRepository.save(track);
        return toDto(track);
    }

    public TrackDto uploadCover(UUID trackId, MultipartFile file) {
        Track track = trackRepository.findById(trackId)
                .orElseThrow(() -> new TrackNotFoundException(trackId.toString()));
        track.setImagePath(fileService.uploadFile(file, "tracks/" + trackId + "/cover"));
        trackRepository.save(track);
        return toDto(track);
    }

    public TrackDto uploadAudio(UUID trackId, MultipartFile file) {
        Track track = trackRepository.findById(trackId)
                .orElseThrow(() -> new TrackNotFoundException(trackId.toString()));
        track.setAudioPath(fileService.uploadFile(file, "tracks/" + trackId + "/audio"));
        trackRepository.save(track);
        return toDto(track);
    }

    @Transactional
    public TrackDto addTag(UUID trackId, String tagName, String color, UUID accountId) {
        Track track = trackRepository.findByIdWithAll(trackId)
                .orElseThrow(() -> new TrackNotFoundException(trackId.toString()));
        Tag tag = tagService.getOrCreateTag(tagName, color, accountId);
        track.getTags().add(tag);
        trackRepository.save(track);
        return toDto(track);
    }

    @Transactional
    public TrackDto removeTag(UUID trackId, UUID tagId) {
        Track track = trackRepository.findByIdWithAll(trackId)
                .orElseThrow(() -> new TrackNotFoundException(trackId.toString()));
        track.getTags().removeIf(tag -> tag.getId().equals(tagId));
        trackRepository.save(track);
        return toDto(track);
    }

    public TrackDto uploadAttachment(UUID trackId, MultipartFile file, UUID accountId) {
        Track track = trackRepository.findByIdWithAll(trackId)
                .orElseThrow(() -> new TrackNotFoundException(trackId.toString()));

        Attachment attachment = new Attachment();
        attachment.setOriginalFilename(file.getOriginalFilename());
        attachment.setS3Key(fileService.uploadFile(file, "tracks/" + trackId + "/attachments"));
        attachment.setContentType(file.getContentType());
        attachment.setFileSize(file.getSize());
        attachment.setAccount(accountRepository.getReferenceById(accountId));
        attachment = attachmentRepository.save(attachment);

        track.getAttachments().add(attachment);
        trackRepository.save(track);
        return toDto(track);
    }

    @Transactional
    public TrackDto removeAttachment(UUID trackId, UUID attachmentId) {
        Track track = trackRepository.findByIdWithAll(trackId)
                .orElseThrow(() -> new TrackNotFoundException(trackId.toString()));
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow();
        attachmentService.deleteAttachment(attachment);
        track.getAttachments().removeIf(att -> att.getId().equals(attachmentId));
        trackRepository.save(track);
        return toDto(track);
    }

    @Transactional(readOnly = true)
    public List<TrackListDto> getFilteredTracks(UUID accountId, TrackFilterDto filter) {
        Specification<Track> spec = (root, query, cb) -> {
            root.fetch("artists", jakarta.persistence.criteria.JoinType.LEFT);
            root.fetch("tags", jakarta.persistence.criteria.JoinType.LEFT);
            query.distinct(true);

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("account").get("id"), accountId));
            predicates.add(cb.equal(root.get("deleted"), false));

            if (filter.getSearch() != null && !filter.getSearch().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("title")), "%" + filter.getSearch().toLowerCase() + "%"));
            }
            if (filter.getTagIds() != null && !filter.getTagIds().isEmpty()) {
                predicates.add(root.join("tags").get("id").in(filter.getTagIds()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        String sortBy = ALLOWED_SORT_FIELDS.contains(filter.getSortBy()) ? filter.getSortBy() : "updatedDate";
        Sort sort = Sort.by("desc".equalsIgnoreCase(filter.getSortDirection()) ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);

        return trackRepository.findAll(spec, sort).stream()
                .map(this::toListDto)
                .collect(Collectors.toList());
    }

    public List<TrackListDto> getTracksNotInRelease(UUID accountId, UUID releaseId) {
        return trackRepository.findTracksNotInRelease(accountId, releaseId).stream()
                .map(this::toListDto)
                .collect(Collectors.toList());
    }

    private TrackDto toDto(Track track) {
        return new TrackDto(
                track.getId(), track.getTitle(), track.getDescription(), track.getLyrics(),
                track.getImagePath(), track.getAudioPath(), track.getUpdatedDate().toString(),
                track.getArtists().stream().map(a -> new TrackDto.ArtistRef(a.getId(), a.getName())).collect(Collectors.toList()),
                track.getTags().stream().map(t -> new TrackDto.TagRef(t.getId(), t.getName(), t.getColor())).collect(Collectors.toList()),
                track.getAttachments().stream().map(a -> new TrackDto.AttachmentRef(
                        a.getId(), a.getOriginalFilename(), a.getContentType(), a.getFileSize(), a.getS3Key())).collect(Collectors.toList()));
    }

    private TrackListDto toListDto(Track t) {
        return new TrackListDto(
                t.getId(), t.getTitle(), t.getImagePath(), t.getAudioPath(),
                t.getArtists().stream().map(a -> new TrackListDto.ArtistRef(a.getId(), a.getName())).collect(Collectors.toList()),
                t.getTags().stream().map(tg -> new TrackListDto.TagRef(tg.getId(), tg.getName(), tg.getColor())).collect(Collectors.toList()),
                t.getUpdatedDate().toString());
    }
}