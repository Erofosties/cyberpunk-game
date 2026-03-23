package com.cyberpunk.exception;

public class EntityNotFoundException extends GameException {

    private static final long serialVersionUID = 1L;

    public EntityNotFoundException(String message) {
        super(message);
    }
}