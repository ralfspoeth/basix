package io.github.ralfspoeth.basix.fn;

import java.util.Comparator;

/**
 * Quite oddly, the {@link Comparator#compare(Object, Object)} function
 * returns negative or positive values or zero indicating smaller or greater relations
 * instead of enumerable values. This enum tries to fix that.
 */
public enum Sign {
    /** Indicates a strictly negative comparison result ({@code < 0}). */
    NEGATIVE,
    /** Indicates an equal comparison result ({@code == 0}). */
    ZERO,
    /** Indicates a strictly positive comparison result ({@code > 0}). */
    POSITIVE;

    /**
     * Tests whether this sign is {@link #NEGATIVE}.
     * @return {@code true} iff this is {@link #NEGATIVE}
     */
    public boolean negative() {
        return this == NEGATIVE;
    }

    /**
     * Tests whether this sign is not {@link #NEGATIVE}, i.e. it is
     * either {@link #ZERO} or {@link #POSITIVE}.
     * @return {@code true} iff this is not {@link #NEGATIVE}
     */
    public boolean nonNegative() {
        return !negative();
    }

    /**
     * Tests whether this sign is {@link #POSITIVE}.
     * @return {@code true} iff this is {@link #POSITIVE}
     */
    public boolean positive() {
        return this == POSITIVE;
    }

    /**
     * Tests whether this sign is not {@link #POSITIVE}, i.e. it is
     * either {@link #ZERO} or {@link #NEGATIVE}.
     * @return {@code true} iff this is not {@link #POSITIVE}
     */
    public boolean nonPositive() {
        return !positive();
    }

    /**
     * Tests whether this sign is {@link #ZERO}.
     * @return {@code true} iff this is {@link #ZERO}
     */
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


    /**
     * Turns the result of comparing {@code t1} to {@code t2} into a {@link Sign},
     * using the natural ordering of {@code T}.
     *
     * @param t1  the first value, compared against {@code t2}; must not be {@code null}
     * @param t2  the second value to compare against; must not be {@code null}
     * @param <T> the element type, which must implement {@link Comparable}
     * @return {@link #NEGATIVE} if {@code t1 < t2}, {@link #ZERO} if equal,
     *         {@link #POSITIVE} if {@code t1 > t2}
     */
    public static <T extends Comparable<? super T>> Sign ofCompare(T t1, T t2 ) {
        return Sign.ofCompare(t1.compareTo(t2));
    }
}
