package ru.composerdesk.exception;

public class UserNotFoundException extends NotFoundException {
    public UserNotFoundException(String username) {
        super("User not found: " + username);
    }
}

