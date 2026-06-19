package io.github.ralfspoeth.basix.fn;

import java.util.Comparator;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import static io.github.ralfspoeth.basix.fn.Sign.ofCompare;

/**
 * Factory methods for {@link Predicate}s built around a value extracted
 * from an object: containment in a set or map, equality, and the family
 * of comparator-based ordering predicates ({@link #smallerThan},
 * {@link #smallerOrEqual}, {@link #equal}, {@link #greaterOrEqual},
 * {@link #greaterThan}, {@link #smallerOrGreater}).
 * <p>
 * All factory methods return new {@link Predicate} instances and never
 * mutate the inputs. This class cannot be instantiated.
 */
public class Predicates {

    private Predicates(){
        // prevent instantiation
    }
    /**
     * This function creates a {@link Predicate}
     * which tests whether some property of an item
     * is an element of a given set.
     * As an example let
     * {@snippet :
     * import java.awt.Point;
     *
     * var s = Set.of(1, 2);
     * var p = new Point(2, 3);
     * }
     * Then
     * {@snippet :
     * assert Stream.of(p)
     *     .filter(Predicates.in(s, Point::getX))
     *     .count() == 1;
     * }
     * will be {@code true}.
     * Both the set and the extraction functions must not be {@code null}.
     * The predicate is as tolerant to {@code null} values
     * as is the extraction function.
     *
     * @param s a {@link Set} of elements either of which must be matched, must not be {@code null}
     * @param extractor a {@link Function} that extracts a property of some object, must not be {@code null}
     * @return a new {@link Predicate} the {@link Predicate#test test} method of which tests
     *  whether the given set contains the extracted property of the given object
     * @param <T> the type of the target object to be tested
     * @param <S> a set
     */
    public static <T, S> Predicate<T> in(Set<S> s, Function<T, ? extends S> extractor) {
        return asPredicate(extractor.andThen(s::contains));
    }

    /**
     * Creates a {@link Predicate} which for some object {@code x}
     * extracts a property {@code p} via {@code extractor} and tests
     * it for equality with the given reference value {@code s} using
     * {@link Object#equals(Object)}.
     *
     * @param s         the reference value to compare against
     * @param extractor the function used to extract the property to compare; must not be {@code null}
     * @param <T>       the type of the target object to be tested
     * @param <S>       the type of the extracted property
     * @return a predicate equivalent to {@code t -> Objects.equals(s, extractor.apply(t))}
     */
    public static <T, S> Predicate<T> eq(S s, Function<T, ? extends S> extractor) {
        return asPredicate(extractor.andThen(s::equals));
    }

    /**
     * Creates a predicate using {@link Sign#NEGATIVE} comparison results only.
     *
     * @param ref        the reference value to compare against
     * @param comparator the comparator to use; must not be {@code null}
     * @param <T>        the element type
     * @return a predicate that returns {@code true} for values strictly smaller than {@code ref}
     */
    public static <T> Predicate<T> smallerThan(T ref, Comparator<? super T> comparator) {
        return x -> ofCompare(comparator.compare(x, ref)).negative();
    }

    /**
     * Same as {@link #smallerThan(Object, Comparator)} with {@link Comparator#naturalOrder()}
     * as comparator.
     *
     * @param ref the reference value to compare against
     * @param <T> the element type, which must implement {@link Comparable}
     * @return a predicate that returns {@code true} for values strictly smaller than {@code ref}
     */
    public static <T extends Comparable<? super T>> Predicate<T> smallerThan(T ref) {
        return smallerThan(ref, Comparator.naturalOrder());
    }

    /**
     * Creates a predicate using {@link Sign#NEGATIVE} and {@link Sign#ZERO} comparison results.
     *
     * @param ref        the reference value to compare against
     * @param comparator the comparator to use; must not be {@code null}
     * @param <T>        the element type
     * @return a predicate that returns {@code true} for values smaller than or equal to {@code ref}
     */
    public static <T> Predicate<T> smallerOrEqual(T ref, Comparator<? super T> comparator) {
        return x -> ofCompare(comparator.compare(x, ref)).nonPositive();
    }

    /**
     * Same as {@link #smallerOrEqual(Object, Comparator)} with {@link Comparator#naturalOrder()}
     * as comparator.
     *
     * @param ref the reference value to compare against
     * @param <T> the element type, which must implement {@link Comparable}
     * @return a predicate that returns {@code true} for values smaller than or equal to {@code ref}
     */
    public static <T extends Comparable<? super T>> Predicate<T> smallerOrEqual(T ref) {
        return smallerOrEqual(ref, Comparator.naturalOrder());
    }

    /**
     * Creates a predicate using {@link Sign#ZERO} comparison results only.
     *
     * @param ref        the reference value to compare against
     * @param comparator the comparator to use; must not be {@code null}
     * @param <T>        the element type
     * @return a predicate that returns {@code true} for values that compare equal to {@code ref}
     *         under the given comparator
     */
    public static <T> Predicate<T> equal(T ref, Comparator<? super T> comparator) {
        return x -> ofCompare(comparator.compare(x, ref)).zero();
    }

    /**
     * Creates a predicate using {@link Sign#NEGATIVE} and {@link Sign#POSITIVE} comparison results,
     * yet no {@link Sign#ZERO} results.
     *
     * @param ref        the reference value to compare against
     * @param comparator the comparator to use; must not be {@code null}
     * @param <T>        the element type
     * @return a predicate that returns {@code true} for values that compare unequal to {@code ref}
     *         under the given comparator
     */
    public static <T> Predicate<T> nonEqual(T ref, Comparator<? super T> comparator) {
        return smallerOrGreater(ref, comparator);
    }

    /**
     * Same as {@link #nonEqual(Object, Comparator)} with {@link Comparator#naturalOrder()}
     * as comparator.
     *
     * @param ref the reference value to compare against
     * @param <T> the element type, which must implement {@link Comparable}
     * @return a predicate that returns {@code true} for values that compare unequal to {@code ref}
     *         under natural order
     */
    public static <T extends Comparable<? super T>> Predicate<T> nonEqual(T ref) {
        return nonEqual(ref, Comparator.naturalOrder());
    }

    /**
     * Same as {@link #equal(Object, Comparator)} with {@link Comparator#naturalOrder()}
     * as comparator.
     *
     * @param ref the reference value to compare against
     * @param <T> the element type, which must implement {@link Comparable}
     * @return a predicate that returns {@code true} for values that compare equal to {@code ref}
     *         under natural order
     */
    public static <T extends Comparable<? super T>> Predicate<T> equal(T ref) {
        return equal(ref, Comparator.naturalOrder());
    }

    /**
     * Creates a predicate using {@link Sign#POSITIVE} and {@link Sign#ZERO} comparison results.
     *
     * @param ref        the reference value to compare against
     * @param comparator the comparator to use; must not be {@code null}
     * @param <T>        the element type
     * @return a predicate that returns {@code true} for values greater than or equal to {@code ref}
     */
    public static <T> Predicate<T> greaterOrEqual(T ref, Comparator<? super T> comparator) {
        return x -> ofCompare(comparator.compare(x, ref)).nonNegative();
    }

    /**
     * Same as {@link #greaterOrEqual(Object, Comparator)} with {@link Comparator#naturalOrder()}
     * as comparator.
     *
     * @param ref the reference value to compare against
     * @param <T> the element type, which must implement {@link Comparable}
     * @return a predicate that returns {@code true} for values greater than or equal to {@code ref}
     */
    public static <T extends Comparable<? super T>> Predicate<T> greaterOrEqual(T ref) {
        return greaterOrEqual(ref, Comparator.naturalOrder());
    }

    /**
     * Creates a predicate using {@link Sign#POSITIVE} comparison results only.
     *
     * @param ref        the reference value to compare against
     * @param comparator the comparator to use; must not be {@code null}
     * @param <T>        the element type
     * @return a predicate that returns {@code true} for values strictly greater than {@code ref}
     */
    public static <T> Predicate<T> greaterThan(T ref, Comparator<? super T> comparator) {
        return x -> ofCompare(comparator.compare(x, ref)).positive();
    }

    /**
     * Same as {@link #greaterThan(Object, Comparator)} with {@link Comparator#naturalOrder()}
     * as comparator.
     *
     * @param ref the reference value to compare against
     * @param <T> the element type, which must implement {@link Comparable}
     * @return a predicate that returns {@code true} for values strictly greater than {@code ref}
     */
    public static <T extends Comparable<T>> Predicate<T> greaterThan(T ref) {
        return greaterThan(ref, Comparator.naturalOrder());
    }

    /**
     * Creates a predicate using {@link Sign#NEGATIVE} and {@link Sign#POSITIVE} comparison results,
     * yet no {@link Sign#ZERO} results.
     *
     * @param ref        the reference value to compare against
     * @param comparator the comparator to use; must not be {@code null}
     * @param <T>        the element type
     * @return a predicate that returns {@code true} for values that compare unequal to {@code ref}
     *         under the given comparator
     */
    public static <T> Predicate<T> smallerOrGreater(T ref, Comparator<? super T> comparator) {
        return x -> !ofCompare(comparator.compare(x, ref)).zero();
    }

    /**
     * Same as {@link #smallerOrGreater(Object, Comparator)} with {@link Comparator#naturalOrder()}
     * as comparator.
     *
     * @param ref the reference value to compare against
     * @param <T> the element type, which must implement {@link Comparable}
     * @return a predicate that returns {@code true} for values that compare unequal to {@code ref}
     *         under natural order
     */
    public static <T extends Comparable<? super T>> Predicate<T> smallerOrGreater(T ref) {
        return smallerOrGreater(ref, Comparator.naturalOrder());
    }

    private static <T> Predicate<T> asPredicate(Function<T, Boolean> f) {
        return f::apply;
    }
}
