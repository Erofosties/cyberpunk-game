package com.cyberpunk.exception;

public class GameRuleViolationException extends GameException {

    private static final long serialVersionUID = 1L;

    public GameRuleViolationException(String message) {
        super(message);
    }
}