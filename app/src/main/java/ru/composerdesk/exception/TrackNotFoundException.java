package ru.composerdesk.exception;

public class TrackNotFoundException extends NotFoundException {
    public TrackNotFoundException(String id) {
        super("Track not found: " + id);
    }
}
