package ru.composerdesk.exception;

public class TrackAlreadyInReleaseException extends ConflictException {
    public TrackAlreadyInReleaseException(String trackId, String releaseId) {
        super("Track " + trackId + " already in release " + releaseId);
    }
}
