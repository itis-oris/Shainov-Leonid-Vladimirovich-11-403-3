package ru.composerdesk.exception;

public class InvalidCredentialsException extends BadRequestException {
    public InvalidCredentialsException() {
        super("Invalid username or password");
    }
}
