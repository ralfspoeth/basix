package io.github.ralfspoeth.basix.fn;

import java.util.Comparator;

/**
 * Quite oddly, the {@link Comparator#compare(Object, Object)} function
 * returns negative or positive values or zero indicating smaller or greater relations
 * instead of enumerable values. This enum tries to fix that.
 */
public enum Sign {
    NEGATIVE,
    ZERO,
    POSITIVE;

    public boolean negative() {
        return this == NEGATIVE;
    }

    public boolean nonNegative() {
        return !negative();
    }

    public boolean positive() {
        return this == POSITIVE;
    }

    public boolean nonPositive() {
        return !positive();
    }

    public boolean zero() {
        return this == ZERO;
    }

    /**
     * Turns an integer result into either {@link #NEGATIVE}, {@link #POSITIVE}, or {@link #ZERO}.
     *
     * @param num an integer
     * @return the sign
     */
    public static Sign ofCompare(int num) {
        return (num < 0 ? Sign.NEGATIVE : (num == 0 ? Sign.ZERO : Sign.POSITIVE));
    }

}
