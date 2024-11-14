package io.github.ralfspoeth.basix.fn;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Functions {

    private Functions() {
        // prevent instantiation
    }

    /**
     * Resembles the ternary operator {@code ? :}.
     * <p>
     * {@code
     * int x;
     * condition(
     *     t -> t==0,
     *     t -> "NULL",
     *     t -> "not nully: " + t
     * )}
     *
     * @param condition the test condition
     * @param ifTrue  function that returns a result if the test condition evaluates to true
     * @param ifFalse function that returns a result if the test condition evaluates to false
     * @return the return value of either {@code ifTrue} or {@code ifFalse}
     * @param <T> the type to be filtered
     * @param <R> the return tye=pe
     */
    public static <T, R> Function<T, R> conditional(
            Predicate<? super T> condition,
            Function<? super T, ? extends R> ifTrue,
            Function<? super T, ? extends R> ifFalse
    ) {
        return t -> condition.test(t)?ifTrue.apply(t): ifFalse.apply(t);
    }

    /**
     * Create a {@link Function function} based on a {@link Map map}
     * and an extraction function which extracts some property
     * of some object.
     *
     * @param m a map
     * @param extr a function
     * @return a function that maps the extracted key to a value
     * @param <T> target type of the function
     * @param <R> return type of the function
     */
    public static <T, R> Function<T, R> of(Map<?, R> m, Function<T, ?> extr) {
        m = Map.copyOf(m);
        return extr.andThen(m::get);
    }

    /**
     * Considers a {@link List list} to be a {@link Function function}
     * of an index {@code i}.
     *
     * @param l the list; defensively copied using {@link List#copyOf(Collection)}
     * @return the function
     * @param <R> the content type of the list
     */
    public static <R> IntFunction<R> of(List<R> l) {
        l = List.copyOf(l);
        return l::get;
    }

    public static <T> Function<T, Indexed<T>> indexed(int startWith) {
        var seq = new AtomicInteger(startWith);
        return x -> new Indexed<>(seq.getAndIncrement(), x);
    }

    public static <T> Stream<Indexed<T>> indexed(Iterable<T> array, int offset) {
        return StreamSupport.stream(array.spliterator(), false).map(indexed(offset));
    }

    public static <T> Stream<Indexed<T>> indexed(Iterable<T> array) {
        return indexed(array, 0);
    }

    public static <L, T> Stream<Labeled<L, T>> labeled(Map<L, T> map) {
        return map.entrySet()
                .stream()
                .map(e -> new Labeled<>(e.getKey(), e.getValue()));
    }

    public static <L, T> Stream<Labeled<L, T>> labeled(Iterable<T> list, Function<T, L> label) {
        return StreamSupport
                .stream(list.spliterator(), false)
                .map(t -> new Labeled<>(label.apply(t), t));
    }
}
