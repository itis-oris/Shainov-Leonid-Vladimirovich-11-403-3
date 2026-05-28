package ru.composerdesk.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import ru.composerdesk.exception.*;
import ru.composerdesk.model.*;
import ru.composerdesk.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.composerdesk.dto.ChangePasswordRequest;
import ru.composerdesk.dto.ChangeUsernameRequest;

import java.util.List;
import java.util.UUID;

@Service
public class AccountService {
    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final TagService tagService;
    private final TrackService trackService;
    private final AttachmentService attachmentService;
    private final ReleaseService releaseService;
    private final ArtistService artistService;

    public AccountService(AccountRepository accountRepository, PasswordEncoder passwordEncoder,
                          TagService tagService, TrackService trackService,
                          AttachmentService attachmentService, ReleaseService releaseService,
                          ArtistService artistService) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.tagService = tagService;
        this.trackService = trackService;
        this.attachmentService = attachmentService;
        this.releaseService = releaseService;
        this.artistService = artistService;
    }

    public Account register(String username, String rawPassword, Role role) {
        log.info("Registering user: {}", username);

        String trimmed = username.trim();
        if (trimmed.isEmpty()) {
            throw new BadRequestException("Username cannot be empty");
        }
        if (rawPassword == null || rawPassword.length() < 4) {
            throw new BadRequestException("Password must be at least 4 characters");
        }
        if (accountRepository.existsByUsername(trimmed)) {
            log.warn("User already exists: {}", trimmed);
            throw new UserAlreadyExistsException(trimmed);
        }

        Account account = new Account();
        account.setUsername(trimmed);
        account.setPassword(passwordEncoder.encode(rawPassword));
        account.setRole(role != null ? role : Role.USER);
        account.setEnabled(true);
        log.info("User registered: {}", trimmed);
        return accountRepository.save(account);
    }

    public Account getById(UUID id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id.toString()));
    }

    @Transactional
    public Account changeUsername(UUID id, ChangeUsernameRequest request) {
        log.info("Changing username for user: {}", id);
        String newUsername = request.getNewUsername().trim();
        if (newUsername.isEmpty()) {
            throw new BadRequestException("Username cannot be empty");
        }

        if (accountRepository.existsByUsername(newUsername)) {
            log.warn("Username already taken: {}", newUsername);
            throw new UserAlreadyExistsException(newUsername);
        }
        Account account = getById(id);
        account.setUsername(newUsername);
        return accountRepository.save(account);
    }

    @Transactional
    public void changePassword(UUID id, ChangePasswordRequest request) {
        log.info("Changing password for user: {}", id);

        if (request.getNewPassword() == null || request.getNewPassword().length() < 4) {
            throw new BadRequestException("New password must be at least 4 characters");
        }
        if (request.getOldPassword().equals(request.getNewPassword())) {
            throw new BadRequestException("New password must differ from old");
        }

        Account account = getById(id);
        if (!passwordEncoder.matches(request.getOldPassword(), account.getPassword())) {
            log.warn("Invalid old password for user: {}", id);
            throw new InvalidCredentialsException();
        }
        account.setPassword(passwordEncoder.encode(request.getNewPassword()));
        accountRepository.save(account);
    }

    @Transactional
    public void deleteAccount(UUID id) {
        log.warn("Deleting account: {}", id);
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id.toString()));

        trackService.getTracksByAccount(id)
                .forEach(t -> trackService.deleteTrack(t));
        releaseService.getReleasesByAccount(id)
                .forEach(r -> releaseService.deleteRelease(r));
        artistService.getArtistsByAccount(id)
                .forEach(a -> artistService.deleteArtist(a));
        tagService.getTagsByAccount(id)
                .forEach(t -> tagService.deleteTag(t));
        attachmentService.getAttachmentsByAccount(id)
                .forEach(a -> attachmentService.deleteAttachment(a));

        account.setDeleted(true);
        accountRepository.save(account);
        log.info("Account deleted: {}", id);
    }
}