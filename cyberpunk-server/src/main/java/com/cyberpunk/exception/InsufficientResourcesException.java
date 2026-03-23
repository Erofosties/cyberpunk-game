package com.cyberpunk.exception;

public class InsufficientResourcesException extends GameException {

    private static final long serialVersionUID = 1L;

    public InsufficientResourcesException(String message) {
        super(message);
    }
}