package ru.composerdesk.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.composerdesk.model.Tag;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TagRepository extends JpaRepository<Tag, UUID> {
    Optional<Tag> findByName(String name);
    boolean existsByName(String name);
    List<Tag> findByAccountId(UUID accountId);
}