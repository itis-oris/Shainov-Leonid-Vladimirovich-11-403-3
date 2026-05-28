package ru.composerdesk.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ru.composerdesk.model.Artist;
import ru.composerdesk.model.Release;

import java.util.List;
import java.util.UUID;

public interface ArtistRepository extends JpaRepository<Artist, UUID>, JpaSpecificationExecutor<Artist> {
    List<Artist> findByAccountIdOrderByName(UUID accountId);
    List<Artist> findByAccountId(UUID accountId);
}