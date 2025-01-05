package io.github.ralfspoeth.basix.fn;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Gatherer;
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
     * t -> t==0,
     * t -> "NULL",
     * t -> "not nully: " + t
     * )}
     *
     * @param condition the test condition
     * @param ifTrue    function that returns a result if the test condition evaluates to true
     * @param ifFalse   function that returns a result if the test condition evaluates to false
     * @param <T>       the type to be filtered
     * @param <R>       the return tye=pe
     * @return the return value of either {@code ifTrue} or {@code ifFalse}
     */
    public static <T, R> Function<T, R> conditional(
            Predicate<? super T> condition,
            Function<? super T, ? extends R> ifTrue,
            Function<? super T, ? extends R> ifFalse
    ) {
        return t -> condition.test(t) ? ifTrue.apply(t) : ifFalse.apply(t);
    }

    /**
     * Create a {@link Function function} based on a {@link Map map}
     * and an extraction function which extracts some property
     * of some object.
     *
     * @param m    a map
     * @param extr a function
     * @param <T>  target type of the function
     * @param <R>  return type of the function
     * @return a function that maps the extracted key to a value
     */
    public static <T, R> Function<T, R> of(Map<?, R> m, Function<T, ?> extr) {
        m = Map.copyOf(m);
        return extr.andThen(m::get);
    }

    /**
     * Considers a {@link List list} to be a {@link Function function}
     * of an index {@code i}.
     *
     * @param l   the list; defensively copied using {@link List#copyOf(Collection)}
     * @param <R> the content type of the list
     * @return the function
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

    /**
     * Stateless integrator to be used with stream {@link Gatherer}s which combines
     * filtering for the given type and casting to that.
     * Usage:
     * {@snippet :
     * List<Object> list = List.<Object>of(); // @replace substring="List.<Object>of()" replacement="..."
     * List<Long> result = list.stream().gather(Gatherer.of(filterAndCast(Long.class))).toList();
     *}
     * <p>
     * The type parameters {@code A} and {@code B} are mostly inferred,
     * yet the target/filter type needs to be explicit.
     * The integrator is stateless and may easily be used in parallel streams.
     *
     * @param type the type to filter for and to cast elements to
     * @param <A>  state type, irrelevant since it is stateless
     * @param <T>  the element type of the stream
     * @param <R>  the return determined by the given type
     * @return an integrator
     */
    public static <A, T, R> Gatherer.Integrator<A, T, R> filterAndCast(final Class<R> type) {
        return (_, element, downstream) -> {
            if (element != null && type.isAssignableFrom(element.getClass())) {
                downstream.push(type.cast(element));
            }
            return true;
        };
    }

    /**
     * Stateful gatherer that removes any consecutive elements producing
     * a stream of alternating elements (hence the name).
     * {@snippet :
     * assert List.of(1).equals(Stream.of(1, 1, 1).gather(alternating()).toList());
     * assert List.of(1, 2).equals(Stream.of(1, 2, 2).gather(alternating()).toList());
     * assert List.of(1, 2, 1).equals(Stream.of(1, 2, 1).gather(alternating()).toList());
     * }
     *
     * Note that this gatherer is different from the {@link Stream#distinct()} built-in method
     * since every element of upstream is compared to the last element pushed downstream.
     * Furthermore, note that {@code null}s are swallowed.
     *
     * @return a gatherer producing alternating elements
     * @param <T> the element type
     */
    public static <T> Gatherer<T, AtomicReference<T>, T> alternating() {
        return Gatherer.ofSequential(
                AtomicReference::new,
                (state, element, downstream) -> {
                    if (element != null && !Objects.equals(state.get(), element)) {
                        downstream.push(element);
                        state.set(element);
                    }
                    return true;
                }
        );
    }
}
