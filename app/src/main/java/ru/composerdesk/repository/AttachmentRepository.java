package ru.composerdesk.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.composerdesk.model.Attachment;

import java.util.List;
import java.util.UUID;

public interface AttachmentRepository extends JpaRepository<Attachment, UUID> {
    List<Attachment> findByAccountId(UUID accountId);
}