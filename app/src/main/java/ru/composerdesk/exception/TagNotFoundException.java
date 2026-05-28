package ru.composerdesk.exception;

public class TagNotFoundException extends NotFoundException {
    public TagNotFoundException(String id) {
        super("Tag not found: " + id);
    }
}
