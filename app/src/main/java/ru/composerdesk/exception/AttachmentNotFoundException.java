package ru.composerdesk.exception;

public class AttachmentNotFoundException extends NotFoundException {
    public AttachmentNotFoundException(String id) {
        super("Attachment not found: " + id);
    }
}
