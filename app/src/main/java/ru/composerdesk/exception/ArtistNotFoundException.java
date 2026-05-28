package ru.composerdesk.exception;

public class ArtistNotFoundException extends NotFoundException {
    public ArtistNotFoundException(String id) {
        super("Artist not found: " + id);
    }
}
