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
     * Example:
     * {@snippet :
     * // given
     * var m = Map.of(1, "one", 2, "two", 3, "three");
     * record R(int x) {}
     * // when
     * var l = List.of(new R(2), new R(3));
     * // then
     * assert List.of("two", "three").equals(l.stream().map(of(m, R::x)).toList());
     *}
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

    /**
     * Create a {@link Function} which turns any object into an {@link Indexed} object,
     * that is, a record composed of an index with this object.
     * The index is incremented everytime the function is called.
     * Example:
     * {@snippet :
     * // given
     * var l = List.of("a", "b", "c");
     * // when
     * var r = List.of(new Indexed<>(1, "a"), new Indexed<>(2, "b"), new Indexed<>(3, "c"));
     * // then
     * assert l.stream().map(indexed(1)).toList().equals(r);
     *
     *}
     *
     * @param startWith the first index value
     * @param <T>       the value type wrapped
     * @return a function
     */
    public static <T> Function<T, Indexed<T>> indexed(int startWith) {
        var seq = new AtomicInteger(startWith);
        return x -> new Indexed<>(seq.getAndIncrement(), x);
    }

    /**
     * Applies {@link #indexed(int)} to the given array.
     *
     * @param array     an array or some other {@link Iterable}
     * @param startWith the first index value
     * @param <T>       the type of the values of the array
     * @return a stream of {@link Indexed} values
     */
    public static <T> Stream<Indexed<T>> indexed(Iterable<T> array, int startWith) {
        return StreamSupport.stream(array.spliterator(), false).map(indexed(startWith));
    }

    /**
     * Applies {@link #indexed(int)} with a first index value of 0
     * to the given array.
     *
     * @see #indexed(Iterable, int)
     */
    public static <T> Stream<Indexed<T>> indexed(Iterable<T> array) {
        return indexed(array, 0);
    }

    /**
     * Turns a map into a stream of its values labeled with
     * the respective key.
     * Example:
     * {@snippet :
     * // given
     * var m = Map.of("one", 11, "two", 22);
     * // then
     * assert labeled(m).toList().equals(List.of(new Labeled<>("one", 11), new Labeled<>("two", 22)));
     *}
     *
     * @param map a map of key-value pairs
     * @param <K> the type of the keys in the map
     * @param <T> the type of the values in the map
     * @return a stream of labeled values
     */
    public static <K, T> Stream<Labeled<K, T>> labeled(Map<K, T> map) {
        return map.entrySet()
                .stream()
                .map(e -> new Labeled<>(e.getKey(), e.getValue()));
    }

    /**
     * Turns an iterable list of values into stream of {@link Labeled} values
     * where the label is extracted from each value by applying the given labelling function.
     * Example:
     * {@snippet :
     * // given
     * record R(String name, int x) {}
     * var rs = List.of(new R("one", 1), new R("two", 2));
     * // when
     * var result = List.of(new Labeled<>("o", new R("one", 1)), new Labeled<>("t", new R("two", 2)));
     * // then
     * assert result.equals(labeled(rs, r -> r.name().substring(0, 1)).toList());
     *}
     *
     * @param list  a list or array of values.
     * @param label a labelling function
     * @param <L>   the type of the label
     * @param <T>   the type of the value
     * @return a stream of labeled values
     */
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
                        if (order == Order.inc && comparator.compare(elements.getLast(), item) > 0
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
     * // given
     * var input = List.of(1, 2, 3, 1, 2, 3);
     * var comparator = Comparator.<Integer>naturalOrder();
     * // when
     * var result = input.stream().gather(monotoneSequences(comparator)).toList();
     * // then
     * // result == [1, 2, 3], [3, 1], [1, 2, 3]
     *}
     * </p>
     * {@code null}s are silently swallowed.
     * <p>
     * The resulting collections are sequenced so that one may detect the order of a list
     * by comparing the first and the last element easily:
     * {@snippet :
     * var result = List.<Integer>of(); // @replace substring="List.<Integer>of()" replacement="(see above)"
     * int ordering = Comparator.<Integer>naturalOrder().compare(result.getFirst(), result.getLast()); // @replace substring="Comparator.<Integer>naturalOrder()" replacement="comparator"
     *}
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
     *
     * @param comparator a comparator
     * @param <T>        the item type
     * @return a gatherer producing a stream of monotone sequences
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

    /**
     * Creates a sequenced map by "zipping" two independent arrays whereby the order
     * of the arrays is preserved, and the shorter array determines the size of the map.
     * Example:
     * {@snippet :
     * // given
     * var keys = new int[]{1, 2, 3}; // 3 items
     * var vals = new String[]{"one", "two", "three", "four"}; // 4 items
     * // when
     * var seqMap = zipMap(keys, vals);
     * // then
     * assert 3==seqMap.size();
     * assert List.of(keys).equals(seqMap.sequencedKeySet().stream().toList());
     * assert List.of("one", "two", "three").equals(seqMap.sequencedValues().stream().toList());
     *}
     *
     * @param keys   array of keys, may contain {@code null} keys
     * @param values array of values, may contain {@code null} values
     * @param <K>    the type of the keys
     * @param <V>    the type of the values
     * @return a sequenced map where the first key/value pair is made of the first key and the first value,
     * the second key/value pair made of the second key and the second value and so forth;
     * note that the map may contain {@code} null keys and/or values
     */
    @SuppressWarnings("unchecked")
    public static <K, V> SequencedMap<K, V> zipMap(Iterable<K> keys, Iterable<V> values) {
        SequencedMap<K, V> tmp = new LinkedHashMap<>();
        for (Iterator<?> itk = keys.iterator(), itv = values.iterator(); itk.hasNext() && itv.hasNext(); ) {
            tmp.put((K) itk.next(), (V) itv.next());
        }
        return tmp;
    }

    /**
     * A sequential gatherer which pushes an additional item downstream after each item it receives.
     * Example generating a constant value:
     * {@snippet :
     * // given
     * var list = List.of(1, 2, 3);
     * // when
     * var result = list.stream().gather(interleave(()->7)).toList();
     * //then
     * assert result.equals(List.of(1, 7, 2, 7, 3, 7));
     * }
     * Example generating a random number:
     * {@snippet :
     * // given
     * import java.util.concurrent.ThreadLocalRandom;
     * var list = List.of(1, 2, 3);
     * var rnd = ThreadLocalRandom.current();
     * // when
     * var result = list.stream().gather(interleave(rnd::nextInt)).toList();
     * // then
     * assert result.size()==6;
     * assert result.get(0)==1 && result.get(2)==2 && result.get(4)==3;
     * }
     *
     * @param generator a supplier which may produces the same or a new item everytime
     *                  it is called; doesn't prevent {@code null} elements
     * @return a gatherer
     * @param <T> the element type
     */
    public static <T> Gatherer<T, Void, T> interleave(Supplier<? extends T> generator) {
        return Gatherer.ofSequential((_, item, downstream) -> {
            if (downstream.isRejecting()) {
                return false;
            } else {
                downstream.push(item);
                if (downstream.isRejecting()) {
                    return false;
                } else {
                    downstream.push(generator.get());
                    return true;
                }
            }
        });
    }
}
