package ru.composerdesk.model;

import java.util.Set;

public enum Role {
    USER(Set.of("ACCOUNT_READ")),

    ADMIN(Set.of("ACCOUNT_READ", "ACCOUNT_WRITE", "ACCOUNT_DELETE"));

    private final Set<String> authorities;

    Role(Set<String> authorities) {
        this.authorities = authorities;
    }

    public Set<String> getAuthorities() {
        return authorities;
    }
}