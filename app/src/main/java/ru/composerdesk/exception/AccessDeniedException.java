package ru.composerdesk.exception;

public class AccessDeniedException extends ForbiddenException {
    public AccessDeniedException() {
        super("Access denied");
    }
}
