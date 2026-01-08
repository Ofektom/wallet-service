package com.ofektom.utils;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Immutable Value Object representing monetary amounts.
 * Stores amounts in minor units (kobo) to avoid floating-point precision issues.
 */
public final class Money {
    private final long amountInMinorUnits;
    
    private Money(long amountInMinorUnits) {
        if (amountInMinorUnits < 0) {
            throw new IllegalArgumentException("Amount cannot be negative: " + amountInMinorUnits);
        }
        this.amountInMinorUnits = amountInMinorUnits;
    }
    
    public static Money ofMinorUnits(long minorUnits) {
        return new Money(minorUnits);
    }
    
    public static Money zero() {
        return new Money(0L);
    }
    
    public Money add(Money other) {
        Objects.requireNonNull(other, "Money to add cannot be null");
        return new Money(this.amountInMinorUnits + other.amountInMinorUnits);
    }
    
    public Money subtract(Money other) {
        Objects.requireNonNull(other, "Money to subtract cannot be null");
        if (this.amountInMinorUnits < other.amountInMinorUnits) {
            throw new IllegalStateException(
                String.format("Insufficient balance: %d < %d", this.amountInMinorUnits, other.amountInMinorUnits)
            );
        }
        return new Money(this.amountInMinorUnits - other.amountInMinorUnits);
    }
    
    public boolean isLessThan(Money other) {
        Objects.requireNonNull(other, "Money to compare cannot be null");
        return this.amountInMinorUnits < other.amountInMinorUnits;
    }
    
    public boolean isGreaterThanOrEqual(Money other) {
        Objects.requireNonNull(other, "Money to compare cannot be null");
        return this.amountInMinorUnits >= other.amountInMinorUnits;
    }
    
    public boolean isZero() {
        return this.amountInMinorUnits == 0L;
    }
    
    public long getAmountInMinorUnits() {
        return amountInMinorUnits;
    }
    
    public BigDecimal toMajorUnits() {
        return BigDecimal.valueOf(amountInMinorUnits).divide(new BigDecimal(100));
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Money money = (Money) o;
        return amountInMinorUnits == money.amountInMinorUnits;
    }
    
    @Override
    public int hashCode() {
        return Long.hashCode(amountInMinorUnits);
    }
    
    @Override
    public String toString() {
        return String.format("Money{minorUnits=%d, majorUnits=%s}", amountInMinorUnits, toMajorUnits());
    }
}

