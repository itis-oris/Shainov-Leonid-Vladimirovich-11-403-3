package ru.composerdesk.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.composerdesk.model.ReleaseTrack;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReleaseTrackRepository extends JpaRepository<ReleaseTrack, UUID> {
    List<ReleaseTrack> findAllByReleaseIdAndTrackId(UUID releaseId, UUID trackId);

    @Query("SELECT COALESCE(MAX(rt.position), 0) FROM ReleaseTrack rt WHERE rt.release.id = :releaseId")
    int findMaxPosition(@Param("releaseId") UUID releaseId);

    void deleteByTrackId(UUID trackId);
    void deleteByReleaseId(UUID releaseId);
    List<ReleaseTrack> findByReleaseId(UUID releaseId);
}