package com.cyberpunk.exception;

public class InvalidSkillDistributionException extends GameException {

    private static final long serialVersionUID = 1L;

    public InvalidSkillDistributionException(String message) {
        super(message);
    }
}