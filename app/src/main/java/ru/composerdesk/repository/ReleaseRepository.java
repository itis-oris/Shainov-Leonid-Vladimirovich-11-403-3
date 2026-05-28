package ru.composerdesk.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.composerdesk.model.Release;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReleaseRepository extends JpaRepository<Release, UUID>, JpaSpecificationExecutor<Release> {
    List<Release> findByAccountIdOrderByIdDesc(UUID accountId);

    @Query("SELECT r FROM Release r " +
            "LEFT JOIN FETCH r.artists " +
            "LEFT JOIN FETCH r.releaseTracks rt " +
            "LEFT JOIN FETCH rt.track t " +
            "LEFT JOIN FETCH t.artists " +
            "LEFT JOIN FETCH r.attachments " +
            "WHERE r.id = :id")
    Optional<Release> findByIdWithAll(@Param("id") UUID id);

    @Query("SELECT r FROM Release r " +
            "LEFT JOIN FETCH r.artists " +
            "LEFT JOIN FETCH r.releaseTracks rt " +
            "LEFT JOIN FETCH rt.track " +
            "WHERE r.account.id = :accountId " +
            "ORDER BY r.id DESC")
    List<Release> findByAccountIdWithArtistAndTrackCount(@Param("accountId") UUID accountId);

    @Query("SELECT DISTINCT r FROM Release r " +
            "LEFT JOIN FETCH r.artists " +
            "LEFT JOIN FETCH r.releaseTracks rt " +
            "LEFT JOIN FETCH rt.track t " +
            "LEFT JOIN FETCH t.artists " +
            "WHERE r.id = :id")
    Optional<Release> findByIdWithTracksAndArtists(@Param("id") UUID id);
    List<Release> findByAccountId(UUID accountId);
}