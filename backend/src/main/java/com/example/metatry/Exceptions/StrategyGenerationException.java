package com.example.metatry.Exceptions;

public class StrategyGenerationException extends RuntimeException {
    public StrategyGenerationException(String message, Throwable cause) {
        super(message, cause);
    }

    public StrategyGenerationException(String message) {
        super(message);
    }
}