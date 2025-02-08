package io.github.ralfspoeth.basix.fn;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import static io.github.ralfspoeth.basix.fn.Functions.unboxedBoolean;

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
        return unboxedBoolean(extractor.andThen(s::contains));
    }

    /**
     * Creates a {@link Predicate} which for some object {@code x}
     * of which an {@code extractor} function extracts some property
     * which must then be in the {@link Map#keySet() keySet} of the
     * {@code m}ap.
     *
     * @param m the map
     * @param extractor an extractor function
     * @return a Predicate which extracts some value of an object and tests whether it is an element of the keySet of the map
     * @param <T> The for which the predicate will be defined
     * @param <S> the type of the elements in the keySet
     */
    public static <T, S> Predicate<T> in(Map<S, ?> m, Function<T, ? extends S> extractor) {
        return unboxedBoolean(extractor.andThen(m::containsKey));
    }

    /**
     * Creates a {@link Predicate} which for some object {@code x}
     * of which an {@code extractor} function extracts some property
     * {@code p} which is tested for equality with the given
     * parameter {@code s}.
     */
    public static <T, S> Predicate<T> eq(S s, Function<T, ? extends S> extractor) {
        return unboxedBoolean(extractor.andThen(s::equals));
    }
}
