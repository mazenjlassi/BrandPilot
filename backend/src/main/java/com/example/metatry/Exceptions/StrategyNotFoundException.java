package com.example.metatry.Exceptions;

public class StrategyNotFoundException extends RuntimeException {
    public StrategyNotFoundException(Long id) {
        super("Strategy not found: " + id);
    }
}