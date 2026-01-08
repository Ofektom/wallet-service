package com.ofektom.enums;

import com.ofektom.utils.Money;

/**
 * Transaction type enum using Strategy pattern.
 * Encapsulates transaction behavior (credit/debit logic) within the enum.
 */
public enum TransactionType {
    CREDIT {
        @Override
        public Money apply(Money balance, Money amount) {
            return balance.add(amount);
        }
    },
    DEBIT {
        @Override
        public Money apply(Money balance, Money amount) {
            if (balance.isLessThan(amount)) {
                throw new IllegalStateException("Insufficient balance");
            }
            return balance.subtract(amount);
        }
    };
    
    // Applies transaction to balance using Strategy pattern
    public abstract Money apply(Money balance, Money amount);
    
    // Parses transaction type from string (case-insensitive)
    public static TransactionType fromString(String type) {
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("Transaction type cannot be null or empty");
        }
        try {
            return valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid transaction type: " + type);
        }
    }
}

