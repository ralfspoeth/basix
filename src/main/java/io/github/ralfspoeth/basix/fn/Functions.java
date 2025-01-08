package io.github.ralfspoeth.basix.fn;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
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
     * {@snippet :
     * int x = 7;
     * int y = conditional(
     * t -> t==0,
     * _ -> "NULL",
     * t -> "not nully: " + t
     * ).apply(x); // "not nully: 7"
     *}
     *
     * @param condition the test condition
     * @param ifTrue    function that returns a result if the test condition evaluates to true
     * @param ifFalse   function that returns a result if the test condition evaluates to false
     * @param <T>       the type to be filtered
     * @param <R>       the return type
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
     * Stateless gatherer to be used with streams  which combines
     * filtering for the given type and casting to that.
     * Usage:
     * {@snippet :
     * List<Object> list = List.<Object>of(); // @replace substring="List.<Object>of()" replacement="..."
     * List<Long> result = list.stream().gather(filterAndCast(Long.class)).toList();
     *}
     * <p>
     * The type parameters {@code T} are mostly inferred,
     * yet the target/filter type needs to be explicit.
     * The integrator used is stateless and may easily be used in parallel streams.
     *
     * @param type the type to filter for and to cast elements to
     * @param <T>  the element type of the stream
     * @param <R>  the return determined by the given type
     * @return a gatherer
     */
    public static <T, R> Gatherer<T, ?, R> filterAndCast(Class<R> type) {
        return Gatherer.of((_, element, downstream) -> {
            if (element != null && type.isAssignableFrom(element.getClass())) {
                downstream.push(type.cast(element));
            }
            return true;
        });
    }

    /**
     * Sequential gatherer that removes any consecutively equal elements producing
     * a stream of alternating elements (hence the name).
     * {@snippet :
     * assert List.of(1).equals(Stream.of(1, 1, 1).gather(alternating()).toList());
     * assert List.of(1, 2).equals(Stream.of(1, 2, 2).gather(alternating()).toList());
     * assert List.of(1, 2, 1).equals(Stream.of(1, 2, 1).gather(alternating()).toList());
     *}
     * <p>
     * Note that this gatherer is different from the {@link Stream#distinct()} built-in method
     * since every element of upstream is compared to the last element pushed downstream only,
     * in contrast to all previous elements as {@link Stream#distinct()} does.
     * Furthermore, note that {@code null}s are swallowed.
     *
     * @param <T> the element type
     * @return a gatherer producing alternating elements
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


    /**
     * Stateful gatherer which pushes an element downstream only if it is greater
     * than the one most recent element.
     * {@snippet :
     * assert List.of(1, 2, 3, 4).equals(
     *         Stream.of(1, 2, 1, 3, 1, 4)
     *         .gather(increasing()).toList());
     *}
     * Note that in the given example the first occurrences of 2, 3, and 4, respectively
     * are being pushed downstream.
     * This variant uses the implicit {@link Comparator#naturalOrder()} comparator,
     * see {@link #increasing() here}.
     * {@code null}s are silently swallowed.
     *
     * @param <T> the type of the stream elements.
     * @return a gather producing a stream of (strictly) increasing elements
     */
    public static <T> Gatherer<T, AtomicReference<T>, T> increasing(Comparator<? super T> comparator) {
        return Gatherer.ofSequential(
                (Supplier<AtomicReference<T>>) AtomicReference::new,
                monotone(Order.inc, comparator)
        );
    }

    /**
     * Same as {@link #increasing(Comparator)} using {@link Comparator#naturalOrder()}} as comparator.
     */
    public static <T extends Comparable<? super T>> Gatherer<T, AtomicReference<T>, T> increasing() {
        return increasing(Comparator.naturalOrder());
    }

    /**
     * Same as {@link #increasing} yet the opposite order.
     *
     * @param <T> the type of the stream elements.
     * @return a gather producing a stream of (strictly) decreasing elements
     */
    public static <T> Gatherer<T, AtomicReference<T>, T> decreasing(Comparator<? super T> comparator) {
        return Gatherer.ofSequential(
                (Supplier<AtomicReference<T>>) AtomicReference::new,
                monotone(Order.dec, comparator)
        );
    }

    /**
     * Same as {@link #decreasing(Comparator)} using {@link Comparator#naturalOrder()}} as comparator.
     */
    public static <T extends Comparable<? super T>> Gatherer<T, AtomicReference<T>, T> decreasing() {
        return decreasing(Comparator.naturalOrder());
    }

    private enum Order {inc, dec}

    private static <T> Gatherer.Integrator<AtomicReference<T>, T, T> monotone(Order order, Comparator<? super T> comparator) {
        return (state, element, downstream) -> {
            if (element != null) {
                if (state.get() == null || switch (order) {
                    case inc -> comparator.compare(state.get(), element) < 0;
                    case dec -> comparator.compare(state.get(), element) > 0;
                }) {
                    downstream.push(element);
                    state.set(element);
                }
            }
            return true;
        };
    }

    /**
     * Stateful sequential gatherer which reverses the stream of elements.
     * {@snippet :
     * assert List.of(3, 2, 1).equals(Stream.of(1, 2, 3).gather(reverse()).toList());
     *}
     *
     * @param <T> the element type of the stream
     * @return a gatherer which reverses the encountering order
     */
    public static <T> Gatherer<T, Stack<T>, T> reverse() {
        return Gatherer.ofSequential(
                Stack::new,
                (stack, element, _) -> {
                    stack.push(element);
                    return true;
                },
                (stack, downstream) -> {
                    while (!stack.empty()) downstream.push(stack.pop());
                }
        );
    }

    /**
     * A gatherer which pushes an element from the upstream down
     * if it is the only non-null element in the upstream; {@code null}s are swallowed.
     * {@snippet :
     * import java.util.stream.Stream;
     * Stream.of(null, 1, null)
     *     .gather(single())
     *     .findFirst()
     *     .orElseThrow(); // 1
     * Stream.of(1, 2)
     *     .gather(single())
     *     .findFirst()
     *     .isEmpty(); // true
     * }
     *
     * @param <T> the element type
     * @return a gatherer, may be used in parallel streams
     */
    public static <T> Gatherer<T, Set<T>, T> single() {
        return Gatherer.of(
                HashSet::new,
                (set, elem, _) -> {
                    if (elem != null) {
                        set.add(elem);
                    }
                    return set.size() < 2;
                },
                (setA, setB) -> {
                    setA.addAll(setB);
                    return setA;
                },
                (set, downstream) -> {
                    if (set.size() == 1) {
                        downstream.push(set.stream().findFirst().orElseThrow());
                    }
                }
        );
    }
}
