package ru.composerdesk.service;

import jakarta.persistence.criteria.Predicate;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.composerdesk.dto.ReleaseDto;
import ru.composerdesk.dto.ReleaseFilterDto;
import ru.composerdesk.dto.ReleaseListDto;
import ru.composerdesk.exception.ReleaseNotFoundException;
import ru.composerdesk.exception.TrackAlreadyInReleaseException;
import ru.composerdesk.exception.TrackNotFoundException;
import ru.composerdesk.model.*;
import ru.composerdesk.repository.*;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReleaseService {

    private static final Logger log = LoggerFactory.getLogger(ReleaseService.class);
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("id", "title", "releaseDate");

    private final ReleaseRepository releaseRepository;
    private final ReleaseTrackRepository releaseTrackRepository;
    private final ArtistRepository artistRepository;
    private final TrackRepository trackRepository;
    private final AttachmentRepository attachmentRepository;
    private final AccountRepository accountRepository;
    private final FileService fileService;
    private final AttachmentService attachmentService;

    public ReleaseService(ReleaseRepository releaseRepository,
                          ReleaseTrackRepository releaseTrackRepository,
                          ArtistRepository artistRepository,
                          TrackRepository trackRepository,
                          AttachmentRepository attachmentRepository,
                          AccountRepository accountRepository,
                          FileService fileService,
                          AttachmentService attachmentService) {
        this.releaseRepository = releaseRepository;
        this.releaseTrackRepository = releaseTrackRepository;
        this.artistRepository = artistRepository;
        this.trackRepository = trackRepository;
        this.attachmentRepository = attachmentRepository;
        this.accountRepository = accountRepository;
        this.fileService = fileService;
        this.attachmentService = attachmentService;
    }

    public List<ReleaseListDto> getUserReleases(UUID accountId) {
        return releaseRepository.findByAccountIdWithArtistAndTrackCount(accountId).stream()
                .map(this::toListDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ReleaseDto getRelease(UUID id) {
        Release release = releaseRepository.findByIdWithTracksAndArtists(id)
                .orElseThrow(() -> new ReleaseNotFoundException(id.toString()));
        Map<UUID, ReleaseTrack> uniqueTracks = new LinkedHashMap<>();
        for (ReleaseTrack rt : release.getReleaseTracks()) {
            if (!uniqueTracks.containsKey(rt.getTrack().getId())) {
                uniqueTracks.put(rt.getTrack().getId(), rt);
            }
        }
        release.getReleaseTracks().clear();
        release.getReleaseTracks().addAll(uniqueTracks.values());

        Hibernate.initialize(release.getAttachments());
        return toDto(release);
    }

    @Transactional
    public ReleaseDto createRelease(UUID accountId) {
        log.info("Creating release for account: {}", accountId);
        Release release = new Release();
        release.setTitle("Новый релиз");
        release.setDescription("");
        release.setType(ReleaseType.SINGLE);
        release.setAccount(accountRepository.getReferenceById(accountId));
        release = releaseRepository.save(release);
        return getRelease(release.getId());
    }

    public ReleaseDto updateRelease(UUID id, String title, String description, String type) {
        Release release = releaseRepository.findById(id)
                .orElseThrow(() -> new ReleaseNotFoundException(id.toString()));
        if (title != null) release.setTitle(title);
        if (description != null) release.setDescription(description);
        if (type != null) release.setType(ReleaseType.valueOf(type));
        release = releaseRepository.save(release);
        return toDto(release);
    }

    @Transactional
    public void deleteRelease(Release release) {
        log.warn("Deleting release: {}", release.getId());
        release.setDeleted(true);
        releaseRepository.save(release);
        releaseTrackRepository.deleteByReleaseId(release.getId());
        Hibernate.initialize(release.getAttachments());
        Set<Attachment> attachments = new HashSet<>(release.getAttachments());
        release.getAttachments().clear();
        releaseRepository.save(release);
        for (Attachment att : attachments) {
            attachmentService.deleteAttachment(att);
        }
    }

    @Transactional
    public void deleteRelease(UUID id) {
        Release release = releaseRepository.findById(id)
                .orElseThrow(() -> new ReleaseNotFoundException(id.toString()));
        deleteRelease(release);
    }

    @Transactional
    public ReleaseDto setArtists(UUID releaseId, List<UUID> artistIds) {
        Release release = releaseRepository.findByIdWithTracksAndArtists(releaseId)
                .orElseThrow(() -> new ReleaseNotFoundException(releaseId.toString()));
        Set<Artist> artists = new HashSet<>(artistRepository.findAllById(artistIds));
        release.setArtists(artists);
        releaseRepository.save(release);
        return getRelease(releaseId);
    }

    @Transactional
    public ReleaseDto uploadCover(UUID releaseId, MultipartFile file) {
        log.info("Uploading cover for release: {}", releaseId);
        Release release = releaseRepository.findById(releaseId)
                .orElseThrow(() -> new ReleaseNotFoundException(releaseId.toString()));
        String s3Key = fileService.uploadFile(file, "releases/" + releaseId + "/cover");
        release.setCoverPath(s3Key);
        releaseRepository.save(release);
        return getRelease(releaseId);
    }

    @Transactional
    public ReleaseDto addTrack(UUID releaseId, UUID trackId) {
        boolean alreadyExists = !releaseTrackRepository.findAllByReleaseIdAndTrackId(releaseId, trackId).isEmpty();
        if (alreadyExists) {
            throw new TrackAlreadyInReleaseException(trackId.toString(), releaseId.toString());
        }
        Release release = releaseRepository.findByIdWithAll(releaseId)
                .orElseThrow(() -> new ReleaseNotFoundException(releaseId.toString()));
        Track track = trackRepository.findById(trackId)
                .orElseThrow(() -> new TrackNotFoundException(trackId.toString()));
        int maxPos = releaseTrackRepository.findMaxPosition(releaseId);
        ReleaseTrack rt = new ReleaseTrack();
        rt.setRelease(release);
        rt.setTrack(track);
        rt.setPosition(maxPos + 1);
        releaseTrackRepository.save(rt);
        release.getReleaseTracks().add(rt);
        return toDto(release);
    }

    @Transactional
    public ReleaseDto removeTrack(UUID releaseId, UUID trackId) {
        ReleaseTrack rt = releaseTrackRepository.findAllByReleaseIdAndTrackId(releaseId, trackId)
                .stream().findFirst()
                .orElseThrow(() -> new TrackNotFoundException(trackId.toString()));
        releaseTrackRepository.delete(rt);
        List<ReleaseTrack> remaining = releaseRepository.findByIdWithTracksAndArtists(releaseId)
                .orElseThrow(() -> new ReleaseNotFoundException(releaseId.toString()))
                .getReleaseTracks();
        for (int i = 0; i < remaining.size(); i++) {
            remaining.get(i).setPosition(i + 1);
        }
        releaseTrackRepository.saveAll(remaining);
        return getRelease(releaseId);
    }

    public List<Release> getReleasesByAccount(UUID accountId) {
        return releaseRepository.findByAccountId(accountId);
    }

    @Transactional
    public ReleaseDto reorderTracks(UUID releaseId, UUID trackId, int newPosition) {
        ReleaseTrack rt = releaseTrackRepository.findAllByReleaseIdAndTrackId(releaseId, trackId)
                .stream().findFirst()
                .orElseThrow(() -> new TrackNotFoundException(trackId.toString()));
        Release release = rt.getRelease();
        int oldPosition = rt.getPosition();
        List<ReleaseTrack> tracks = releaseTrackRepository.findByReleaseId(releaseId);
        for (ReleaseTrack t : tracks) {
            if (newPosition < oldPosition && t.getPosition() >= newPosition && t.getPosition() < oldPosition) {
                t.setPosition(t.getPosition() + 1);
            } else if (newPosition > oldPosition && t.getPosition() > oldPosition && t.getPosition() <= newPosition) {
                t.setPosition(t.getPosition() - 1);
            }
        }
        rt.setPosition(newPosition);
        releaseTrackRepository.saveAll(tracks);
        return toDto(release);
    }

    @Transactional
    public ReleaseDto uploadAttachment(UUID releaseId, MultipartFile file, UUID accountId) {
        log.info("Uploading attachment for release: {}", releaseId);
        Release release = releaseRepository.findById(releaseId)
                .orElseThrow(() -> new ReleaseNotFoundException(releaseId.toString()));
        String s3Key = fileService.uploadFile(file, "releases/" + releaseId + "/attachments");
        Attachment attachment = new Attachment();
        attachment.setOriginalFilename(file.getOriginalFilename());
        attachment.setS3Key(s3Key);
        attachment.setContentType(file.getContentType());
        attachment.setFileSize(file.getSize());
        attachment.setAccount(accountRepository.getReferenceById(accountId));
        attachment = attachmentRepository.save(attachment);
        release.getAttachments().add(attachment);
        releaseRepository.save(release);
        return getRelease(releaseId);
    }

    @Transactional
    public ReleaseDto removeAttachment(UUID releaseId, UUID attachmentId) {
        Release release = releaseRepository.findById(releaseId)
                .orElseThrow(() -> new ReleaseNotFoundException(releaseId.toString()));
        Hibernate.initialize(release.getAttachments());
        release.getAttachments().removeIf(a -> a.getId().equals(attachmentId));
        releaseRepository.save(release);
        attachmentRepository.deleteById(attachmentId);
        return getRelease(releaseId);
    }

    private ReleaseDto toDto(Release release) {
        return new ReleaseDto(release.getId(), release.getTitle(), release.getDescription(), release.getCoverPath(),
                release.getType().name(),
                release.getReleaseTracks().stream()
                        .map(rt -> new ReleaseDto.TrackRef(rt.getTrack().getId(), rt.getTrack().getTitle(),
                                rt.getPosition(), rt.getTrack().getAudioPath(),
                                rt.getTrack().getArtists().stream()
                                        .map(a -> new ReleaseDto.ArtistRef(a.getId(), a.getName()))
                                        .collect(Collectors.toList())))
                        .collect(Collectors.toList()),
                release.getArtists().stream().map(a -> new ReleaseDto.ArtistRef(a.getId(), a.getName())).collect(Collectors.toList()),
                release.getAttachments().stream()
                        .map(a -> new ReleaseDto.AttachmentRef(a.getId(), a.getOriginalFilename(), a.getContentType(), a.getFileSize(), a.getS3Key()))
                        .collect(Collectors.toList()));
    }

    public List<ReleaseListDto> getFilteredReleases(UUID accountId, ReleaseFilterDto filter) {
        Specification<Release> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("account").get("id"), accountId));
            predicates.add(cb.equal(root.get("deleted"), false));
            if (filter.getSearch() != null && !filter.getSearch().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("title")), "%" + filter.getSearch().toLowerCase() + "%"));
            }
            if (filter.getTypes() != null && !filter.getTypes().isEmpty()) {
                predicates.add(root.get("type").in(filter.getTypes().stream().map(ReleaseType::valueOf).toList()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        String sortBy = ALLOWED_SORT_FIELDS.contains(filter.getSortBy()) ? filter.getSortBy() : "id";
        Sort sort = Sort.by("desc".equals(filter.getSortDirection()) ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        return releaseRepository.findAll(spec, sort).stream()
                .map(this::toListDto)
                .collect(Collectors.toList());
    }

    private ReleaseListDto toListDto(Release r) {
        return new ReleaseListDto(
                r.getId(),
                r.getTitle(),
                r.getCoverPath(),
                r.getType().name(),
                r.getArtists().stream().map(Artist::getName).collect(Collectors.joining(", ")),
                r.getReleaseTracks().size()
        );
    }
}