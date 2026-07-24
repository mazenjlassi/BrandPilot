package com.example.metatry.Exceptions;

public class StrategyConflictException extends RuntimeException {
    public StrategyConflictException(String message) {
        super(message);
    }
}