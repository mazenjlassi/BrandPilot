package com.example.metatry.Exceptions;

import com.example.metatry.Enums.MarketingStrategyStatus;

public class StrategyNotPendingException extends RuntimeException {
    public StrategyNotPendingException(MarketingStrategyStatus status) {
        super("Only PENDING strategies can be approved. Current status: " + status);
    }
}