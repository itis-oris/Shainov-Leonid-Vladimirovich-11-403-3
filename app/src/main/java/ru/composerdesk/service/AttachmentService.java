package ru.composerdesk.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.composerdesk.exception.AttachmentNotFoundException;
import ru.composerdesk.model.Attachment;
import ru.composerdesk.model.Release;
import ru.composerdesk.model.Track;
import ru.composerdesk.repository.AttachmentRepository;
import ru.composerdesk.repository.ReleaseRepository;
import ru.composerdesk.repository.TrackRepository;

import java.util.List;
import java.util.UUID;

@Service
public class AttachmentService {
    private static final Logger log = LoggerFactory.getLogger(AttachmentService.class);

    private final AttachmentRepository attachmentRepository;
    private final TrackRepository trackRepository;
    private final ReleaseRepository releaseRepository;
    private final FileService fileService;

    public AttachmentService(AttachmentRepository attachmentRepository,
                             TrackRepository trackRepository,
                             ReleaseRepository releaseRepository,
                             FileService fileService) {
        this.attachmentRepository = attachmentRepository;
        this.trackRepository = trackRepository;
        this.releaseRepository = releaseRepository;
        this.fileService = fileService;
    }

    @Transactional
    public void deleteAttachment(Attachment attachment) {
        log.warn("Deleting attachment: {}", attachment.getId());

        for (Track track : attachment.getTracks()) {
            track.getAttachments().remove(attachment);
            trackRepository.save(track);
        }
        attachment.getTracks().clear();

        for (Release release : attachment.getReleases()) {
            release.getAttachments().remove(attachment);
            releaseRepository.save(release);
        }
        attachment.getReleases().clear();

        fileService.deleteFile(attachment.getS3Key());
        attachmentRepository.delete(attachment);
    }

    @Transactional
    public void deleteAttachment(UUID id) {
        Attachment attachment = attachmentRepository.findById(id)
                .orElseThrow(() -> new AttachmentNotFoundException(id.toString()));
        deleteAttachment(attachment);
    }

    public List<Attachment> getAttachmentsByAccount(UUID accountId) {
        return attachmentRepository.findByAccountId(accountId);
    }
}