package com.example.metatry.Exceptions;

import com.example.metatry.Enums.MarketingStrategyStatus;

public class StrategyNotEditableException extends RuntimeException {
    public StrategyNotEditableException(MarketingStrategyStatus status) {
        super("Cannot edit a " + status.name().toLowerCase() + " strategy");
    }
}