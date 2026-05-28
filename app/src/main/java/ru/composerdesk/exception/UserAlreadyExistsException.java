package ru.composerdesk.exception;

public class UserAlreadyExistsException extends BadRequestException {
    public UserAlreadyExistsException(String username) {
        super("User already exists: " + username);
    }
}
