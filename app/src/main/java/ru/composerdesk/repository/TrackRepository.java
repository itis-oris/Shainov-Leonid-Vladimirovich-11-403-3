package ru.composerdesk.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.composerdesk.model.Track;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TrackRepository extends JpaRepository<Track, UUID>, JpaSpecificationExecutor<Track> {
    List<Track> findByAccountId(UUID accountId);
    List<Track> findByAccountIdOrderByUpdatedDateDesc(UUID accountId);

    @Query("SELECT t FROM Track t " +
            "LEFT JOIN FETCH t.artists " +
            "LEFT JOIN FETCH t.tags " +
            "LEFT JOIN FETCH t.attachments " +
            "WHERE t.id = :id")
    Optional<Track> findByIdWithAll(@Param("id") UUID id);

    @Query("SELECT DISTINCT t FROM Track t " +
            "LEFT JOIN FETCH t.artists " +
            "LEFT JOIN FETCH t.tags " +
            "WHERE t.account.id = :accountId " +
            "ORDER BY t.updatedDate DESC")
    List<Track> findByAccountIdWithArtists(@Param("accountId") UUID accountId);

    @Query("SELECT t FROM Track t WHERE t.account.id = :accountId AND t.deleted = false AND t.id NOT IN " +
            "(SELECT rt.track.id FROM ReleaseTrack rt WHERE rt.release.id = :releaseId)")
    List<Track> findTracksNotInRelease(@Param("accountId") UUID accountId, @Param("releaseId") UUID releaseId);
}