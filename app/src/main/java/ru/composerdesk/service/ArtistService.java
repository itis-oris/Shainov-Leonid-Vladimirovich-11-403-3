package ru.composerdesk.service;

import jakarta.persistence.criteria.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.composerdesk.dto.ArtistFilterDto;
import ru.composerdesk.exception.ArtistNotFoundException;
import ru.composerdesk.model.Artist;
import ru.composerdesk.repository.AccountRepository;
import ru.composerdesk.repository.ArtistRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class ArtistService {

    private static final Logger log = LoggerFactory.getLogger(ArtistService.class);
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("name");

    private final ArtistRepository artistRepository;
    private final FileService fileService;
    private final AccountRepository accountRepository;

    public ArtistService(ArtistRepository artistRepository, FileService fileService, AccountRepository accountRepository) {
        this.artistRepository = artistRepository;
        this.fileService = fileService;
        this.accountRepository = accountRepository;
    }

    public List<Artist> getUserArtists(UUID accountId) {
        return artistRepository.findByAccountIdOrderByName(accountId);
    }

    public Artist getArtist(UUID id) {
        return artistRepository.findById(id)
                .orElseThrow(() -> new ArtistNotFoundException(id.toString()));
    }

    public Artist createArtist(UUID accountId, String name, String description) {
        log.info("Creating artist: {} for account: {}", name, accountId);
        Artist artist = new Artist();
        artist.setName(name);
        artist.setDescription(description);
        artist.setAccount(accountRepository.getReferenceById(accountId));
        return artistRepository.save(artist);
    }

    public Artist updateArtist(UUID id, String name, String description) {
        Artist artist = getArtist(id);
        artist.setName(name);
        artist.setDescription(description);
        return artistRepository.save(artist);
    }

    @Transactional
    public void deleteArtist(Artist artist) {
        log.warn("Deleting artist: {}", artist.getId());
        artist.setDeleted(true);
        artistRepository.save(artist);
    }

    @Transactional
    public void deleteArtist(UUID id) {
        Artist artist = getArtist(id);
        deleteArtist(artist);
    }

    public List<Artist> getArtistsByAccount(UUID accountId) {
        return artistRepository.findByAccountId(accountId);
    }

    public Artist uploadAvatar(UUID id, MultipartFile file) {
        log.info("Uploading avatar for artist: {}", id);
        Artist artist = getArtist(id);
        String s3Key = fileService.uploadFile(file, "avatars/" + id);
        artist.setAvatarPath(s3Key);
        return artistRepository.save(artist);
    }

    public List<Artist> getFilteredArtists(UUID accountId, ArtistFilterDto filter) {
        Specification<Artist> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("account").get("id"), accountId));
            predicates.add(cb.equal(root.get("deleted"), false));
            if (filter.getSearch() != null && !filter.getSearch().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + filter.getSearch().toLowerCase() + "%"));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        String sortBy = ALLOWED_SORT_FIELDS.contains(filter.getSortBy()) ? filter.getSortBy() : "name";
        Sort sort = Sort.by("desc".equals(filter.getSortDirection()) ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        return artistRepository.findAll(spec, sort);
    }
}