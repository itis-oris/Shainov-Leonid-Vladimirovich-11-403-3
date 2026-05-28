package ru.composerdesk.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.composerdesk.exception.TagNotFoundException;
import ru.composerdesk.model.Account;
import ru.composerdesk.model.Tag;
import ru.composerdesk.model.Track;
import ru.composerdesk.repository.AccountRepository;
import ru.composerdesk.repository.TagRepository;
import ru.composerdesk.repository.TrackRepository;

import java.util.List;
import java.util.UUID;

@Service
public class TagService {

    private static final Logger log = LoggerFactory.getLogger(TagService.class);

    private final TagRepository tagRepository;
    private final TrackRepository trackRepository;
    private final AccountRepository accountRepository;

    public TagService(TagRepository tagRepository, TrackRepository trackRepository, AccountRepository accountRepository) {
        this.tagRepository = tagRepository;
        this.trackRepository = trackRepository;
        this.accountRepository = accountRepository;
    }

    public Tag getOrCreateTag(String name, String color, UUID accountId) {
        return tagRepository.findByName(name)
                .orElseGet(() -> {
                    log.info("Creating tag: {}", name);
                    Tag tag = new Tag();
                    tag.setName(name);
                    tag.setColor(color != null ? color : "#808080");
                    tag.setAccount(accountRepository.getReferenceById(accountId));
                    return tagRepository.save(tag);
                });
    }

    @Transactional
    public void deleteTag(Tag tag) {
        log.warn("Deleting tag: {}", tag.getId());
        for (Track track : tag.getTracks()) {
            track.getTags().remove(tag);
            trackRepository.save(track);
        }
        tag.getTracks().clear();
        tagRepository.delete(tag);
    }

    @Transactional
    public void deleteTag(UUID id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new TagNotFoundException(id.toString()));
        deleteTag(tag);
    }

    public List<Tag> getTagsByAccount(UUID accountId) {
        return tagRepository.findByAccountId(accountId);
    }
}