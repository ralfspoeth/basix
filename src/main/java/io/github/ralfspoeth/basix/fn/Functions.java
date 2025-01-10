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
            if (downstream.isRejecting()) {
                return false;
            }
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
            if (downstream.isRejecting()) {
                return false;
            }
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
     * assert List.of(3, 2, 1)
     *     .equals(Stream.of(1, 2, 3)
     *         .gather(reverse())
     *         .toList()
     * );
     *}
     *
     * @param <T> the element type of the stream
     * @return a gatherer which reverses the encountering order
     */
    public static <T> Gatherer<T, Stack<T>, T> reverse() {
        return Gatherer.ofSequential(
                Stack::new,
                (stack, element, downstream) -> {
                    if (downstream.isRejecting()) {
                        return false;
                    }
                    stack.push(element);
                    return true;
                },
                (stack, downstream) -> {
                    while (!stack.empty() && !downstream.isRejecting()) {
                        downstream.push(stack.pop());
                    }
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
     *}
     *
     * @param <T> the element type
     * @return a gatherer, may be used in parallel streams
     */
    public static <T> Gatherer<T, Collection<T>, T> single() {
        return Gatherer.of(
                ArrayList::new,
                (elementsSoFar, elem, downstream) -> {
                    if (downstream.isRejecting()) {
                        return false;
                    } else {
                        if (elem != null) {
                            elementsSoFar.add(elem);
                        }
                        return elementsSoFar.size() < 2;
                    }
                },
                (visitedA, visitedB) -> {
                    visitedA.addAll(visitedB);
                    return visitedA;
                },
                (visited, downstream) -> {
                    if (!downstream.isRejecting() && visited.size() == 1) {
                        downstream.push(visited.stream().findAny().orElseThrow());
                    }
                }
        );
    }

    private static class ContCollection<T> extends AbstractSequentialList<T> {

        private final Comparator<? super T> comparator;

        private final List<T> elements = new ArrayList<>();
        private Order order = null;

        private ContCollection(Comparator<? super T> comparator) {
            this.comparator = comparator;
        }

        @Override
        public boolean add(T item) {
            if (item == null) {
                return false;
            } else {
                if (elements.isEmpty()) {
                    return elements.add(item);
                } else {
                    if (order == null) {
                        order = switch (comparator.compare(elements.getLast(), item)) {
                            case int i when i < 0 -> Order.inc;
                            case int j when j > 0 -> Order.dec;
                            default -> null;
                        };
                        return elements.add(item);
                    } else {
                        if (    order == Order.inc && comparator.compare(elements.getLast(), item) > 0
                             || order == Order.dec && comparator.compare(elements.getLast(), item) < 0
                        ) {
                            return false;
                        } else {
                            return elements.add(item);
                        }
                    }
                }
            }
        }

        @Override
        public void clear() {
            elements.clear();
            order = null;
        }

        @Override
        public Iterator<T> iterator() {
            return elements.iterator();
        }

        @Override
        public ListIterator<T> listIterator(int index) {
            return elements.listIterator(index);
        }

        @Override
        public int size() {
            return elements.size();
        }
    }

    public static <T extends Comparable<? super T>> Gatherer<T, SequencedCollection<T>, List<T>> monotoneSequences() {
        return monotoneSequences(Comparator.naturalOrder());
    }

    /**
     * Sequential gatherer which produces a stream of list each of which are in increasing or decreasing order.
     * Considering the simple case {@code [1, 2, 1]}. We can see that we produce two lists of increasing and
     * then decreasing elements {@code [1, 2], [2, 1]}. Note how the last element in the first list
     * is the first element in the last list.
     * <p>
     * Usage:
     * {@snippet :
     * var input = List.of(1, 2, 3, 1, 2, 3);
     * var comparator = Comparator.<Integer>naturalOrder();
     * var result = input.stream().gather(monotoneSequences()).toList();
     * // [1, 2, 3], [3, 1], [1, 2, 3]
     * }
     * </p>
     * {@code null}s are swallowed.
     * <p>
     * The resulting collections are sequenced so that one may detect the order of a list
     * by comparing the first and the last element easily:
     * {@snippet :
     * var result = List.<Integer>of(); // @replace substring="List.<Integer>of()" replacement="(see above)"
     * int ordering = Comparator.<Integer>naturalOrder().compare(result.getFirst(), result.getLast()); // @replace substring="Comparator.<Integer>naturalOrder()" replacement="comparator"
     * }
     * </p>
     * <p>
     * Comparing the first and the last element is the preferred way in order to determine the ordering
     * of the lists because it is covers the edge cases
     * </p>
     * <ul>
     *     <li>{@code [1]->[[1]]}: a singleton list produces a list with a singleton list; note that {@code assert ordering==0;}</li>
     *     <li>{@code [1, 1]->[[1, 1]]}: a list with all-equal elements produces a list of itself, again such that {@code assert ordering==0}</li>
     *     <li>{@code [1, 1, 1,... , 2]->[[1, 1, 1, ..., 2]]}: comparing first and last now yields a negative value</li>
     * </ul>
     * @param comparator a comparator
     * @return a gatherer producing a stream of monotone sequences
     * @param <T> the item type
     */
    public static <T> Gatherer<T, SequencedCollection<T>, List<T>> monotoneSequences(Comparator<? super T> comparator) {
        return Gatherer.ofSequential(
                () -> new ContCollection<>(comparator),
                (coll, item, downstream) -> {
                    if (downstream.isRejecting()) {
                        return false;
                    } else {
                        if (!coll.add(item)) {
                            downstream.push(coll.stream().toList());
                            T last = coll.getLast();
                            coll.clear();
                            coll.add(last);
                            coll.add(item);
                        }
                        return true;
                    }
                },
                (coll, downstream) -> {
                    if (!downstream.isRejecting() && !coll.isEmpty()) {
                        downstream.push(coll.stream().toList());
                    }
                }
        );
    }

    public static <K, V> Map<K, V> zipmap(Iterable<K> keys, Iterable<V> values) {
        Map<K, V> tmp = new HashMap<>();
        var itk = keys.iterator();
        var itv = values.iterator();
        while(itk.hasNext() && itv.hasNext()) {
            tmp.put(itk.next(), itv.next());
        }
        return Map.copyOf(tmp);
    }
}
