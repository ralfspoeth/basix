package io.github.ralfspoeth.basix.fn;

import java.util.Comparator;
import java.util.Set;
import java.util.function.Predicate;

import static io.github.ralfspoeth.basix.fn.Predicates.in;

/**
 * Quite oddly, the {@link Comparator#compare(Object, Object)} function
 * returns negative or positive values or zero indicating smaller or greater relations
 * instead of enumerable values. This enum tries to fix that.
 * The predicate generating methods are built by checking the sign
 * of comparisons to some reference object.
 */
public enum Sign {
    NEGATIVE,
    ZERO,
    POSITIVE;

    /**
     * Turns an integer result into either {@link #NEGATIVE}, {@link #POSITIVE}, or {@link #ZERO}.
     *
     * @param num an integer
     * @return the sign
     */
    public static Sign sign(int num) {
        return (num < 0 ? Sign.NEGATIVE : (num == 0 ? Sign.ZERO : Sign.POSITIVE));
    }

    /**
     * Creates a predicate using {@link #NEGATIVE} comparison results only.
     */
    public static <T> Predicate<T> smallerThan(T ref, Comparator<? super T> comparator) {
        return comparison(ref, comparator, NEGATIVE);
    }

    /**
     * Same as {@link #smallerThan(Object, Comparator)} with {@link Comparator#naturalOrder()}
     * as comparator.
     */
    public static <T extends Comparable<? super T>> Predicate<T> smallerThan(T ref) {
        return smallerThan(ref, Comparator.naturalOrder());
    }

    /**
     * Creates a predicate using {@link #NEGATIVE} and {@link #ZERO} comparison results.
     */
    public static <T> Predicate<T> smallerOrEqual(T ref, Comparator<? super T> comparator) {
        return comparison(ref, comparator, NEGATIVE, ZERO);
    }

    /**
     * Same as {@link #smallerOrEqual(Object, Comparator)} )} with {@link Comparator#naturalOrder()}
     * as comparator.
     */
    public static <T extends Comparable<? super T>> Predicate<T> smallerOrEqual(T ref) {
        return smallerOrEqual(ref, Comparator.naturalOrder());
    }

    /**
     * Creates a predicate using {@link #ZERO} comparison results only.
     */
    public static <T> Predicate<T> equal(T ref, Comparator<? super T> comparator) {
        return comparison(ref, comparator, ZERO);
    }

    /**
     * Same as {@link #equal(Object, Comparator)} )} with {@link Comparator#naturalOrder()}
     * as comparator.
     */
    public static <T extends Comparable<? super T>> Predicate<T> equal(T ref) {
        return equal(ref, Comparator.naturalOrder());
    }

    /**
     * Creates a predicate using {@link #POSITIVE} and {@link #ZERO} comparison results.
     */
    public static <T> Predicate<T> greaterOrEqual(T ref, Comparator<? super T> comparator) {
        return comparison(ref, comparator, ZERO, POSITIVE);
    }

    /**
     * Same as {@link #greaterOrEqual(Object, Comparator)} )} with {@link Comparator#naturalOrder()}
     * as comparator.
     */
    public static <T extends Comparable<? super T>> Predicate<T> greaterOrEqual(T ref) {
        return greaterOrEqual(ref, Comparator.naturalOrder());
    }

    /**
     * Creates a predicate using {@link #NEGATIVE} comparison results only.
     */
    public static <T> Predicate<T> greaterThan(T ref, Comparator<? super T> comparator) {
        return comparison(ref, comparator, POSITIVE);
    }

    /**
     * Same as {@link #greaterThan(Object, Comparator)} )} with {@link Comparator#naturalOrder()}
     * as comparator.
     */
    public static <T extends Comparable<T>> Predicate<T> greaterThan(T ref) {
        return greaterThan(ref, Comparator.naturalOrder());
    }

    /**
     * Creates a predicate using {@link #NEGATIVE} and {@link #POSITIVE} comparison results,
     * yet no {@link #ZERO} results.
     */
    public static <T> Predicate<T> smallerOrGreater(T ref, Comparator<? super T> comparator) {
        return comparison(ref, comparator, NEGATIVE, POSITIVE);
    }

    /**
     * Same as {@link #smallerOrGreater(Object, Comparator)} )} with {@link Comparator#naturalOrder()}
     * as comparator.
     */
    public static <T extends Comparable<? super T>> Predicate<T> smallerOrGreater(T ref) {
        return smallerOrGreater(ref, Comparator.naturalOrder());
    }

    // helper method
    private static <T> Predicate<T> comparison(T ref, Comparator<? super T> comparator, Sign... sns) {
        return in(Set.of(sns), t -> sign(comparator.compare(t, ref)));
    }
}
