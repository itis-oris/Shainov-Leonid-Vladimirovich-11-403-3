package ru.composerdesk.exception;

public class ReleaseNotFoundException extends NotFoundException {
    public ReleaseNotFoundException(String id) {
        super("Release not found: " + id);
    }
}
