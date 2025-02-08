package io.github.ralfspoeth.basix.fn;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Gatherer;
import java.util.stream.Stream;

import static io.github.ralfspoeth.basix.fn.Sign.sign;
import static java.util.Objects.requireNonNull;

public class Gatherers {
    /**
     * Stateless gatherer to be used with streams  which combines
     * filtering for the given type and casting to that.
     * Usage:
     * {@snippet :
     * List<Object> list = List.<Object>of(); // @replace substring="List.<Object>of()" replacement="..."
     * List<Long> result = list.stream().gather(Gatherers.filterAndCast(Long.class)).toList();
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
     * assert List.of(1).equals(Stream.of(1, 1, 1).gather(Gatherers.alternatingEquality()).toList());
     * assert List.of(1, 2).equals(Stream.of(1, 2, 2).gather(Gatherers.alternatingEquality()).toList());
     * assert List.of(1, 2, 1).equals(Stream.of(1, 2, 1).gather(Gatherers.alternatingEquality()).toList());
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
    public static <T> Gatherer<T, AtomicReference<T>, T> alternatingEquality() {
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
     * Same as {@link #alternating(Comparator)} with a {@link Comparator#naturalOrder()} passed
     * in as comparator.
     */
    public static <T extends Comparable<? super T>> Gatherer<T, AtomicReference<T>, T> alternating() {
        return alternating(Comparator.naturalOrder());
    }

    /**
     * Similar to {@link #alternatingEquality()} but with the elements being compared
     * to the previous element pushed downstream using a {@link Comparator}.
     *
     * @param comparator the comparator
     * @return a gatherer
     * @param <T> the type of the elements
     */
    public static <T> Gatherer<T, AtomicReference<T>, T> alternating(Comparator<? super T> comparator) {
        return Gatherer.ofSequential(
                AtomicReference::new,
                (state, element, downstream) -> {
                    if(downstream.isRejecting()) {
                        return false;
                    } else if (state.get() == null || 0 != comparator.compare(element, state.get())) {
                        state.set(element);
                        return downstream.push(element);
                    } else {
                        return true;
                    }
                }
        );
    }

    /**
     * Stateful gatherer which pushes an element downstream only if it is greater
     * than the one most recent element.
     * {@snippet :
     * assert List.of(1, 2, 3, 4).equals(
     *         Stream.of(1, 2, 1, 3, 1, 4)
     *         .gather(Gatherers.increasing()).toList());
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
                monotone(Order.INCREASING, comparator)
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
                monotone(Order.DECREASING, comparator)
        );
    }

    /**
     * Same as {@link #decreasing(Comparator)} using {@link Comparator#naturalOrder()}} as comparator.
     */
    public static <T extends Comparable<? super T>> Gatherer<T, AtomicReference<T>, T> decreasing() {
        return decreasing(Comparator.naturalOrder());
    }

    private static <T> Gatherer.Integrator<AtomicReference<T>, T, T> monotone(Order order, Comparator<? super T> comparator) {
        return (state, element, downstream) -> {
            if (downstream.isRejecting()) {
                return false;
            }
            if (element != null) {
                if (state.get() == null || switch (order) {
                    case INCREASING -> comparator.compare(state.get(), element) < 0;
                    case DECREASING -> comparator.compare(state.get(), element) > 0;
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
     *         .gather(Gatherers.reverse())
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
     * if it is the only non-null element in the upstream.
     * {@snippet :
     * import java.util.stream.Stream;
     * Stream.of(1)
     *     .gather(Gatherers.single())
     *     .findFirst()
     *     .orElseThrow(); // 1
     * Stream.of(1, 2)
     *     .gather(Gatherers.single())
     *     .findFirst()
     *     .isEmpty(); // true
     *}
     * Note that empty streams remain exactly this: empty.
     * {@snippet :
     * Stream.of()
     *      .gather(Gatherers.single())
     *      .toList(); // empty list
     *}
     *
     * @param <T> the element type
     * @return a gatherer, may be used in parallel streams
     */
    public static <T> Gatherer<T, Collection<T>, T> single() {
        return exactly(1);
    }

    /**
     * A gatherer which pushes exactly {@code n} elements downstream
     * if and only if the upstream delivers exactly {@code n}.
     * Note that {@code null}s are accepted and passed through.
     *
     * @param n   the number of elements to be met
     * @param <T> the element type
     * @return a gatherer
     */
    public static <T> Gatherer<T, Collection<T>, T> exactly(int n) {
        return Gatherer.of(
                ArrayList::new,
                (elementsSoFar, elem, downstream) -> {
                    if (downstream.isRejecting()) {
                        return false;
                    } else {
                        elementsSoFar.add(elem);
                        return elementsSoFar.size() <= n;
                    }
                },
                (visitedA, visitedB) -> {
                    visitedA.addAll(visitedB);
                    return visitedA;
                },
                (visited, downstream) -> {
                    if (!downstream.isRejecting() && visited.size() == n) {
                        visited.forEach(downstream::push);
                    }
                }
        );
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
     * var result = input.stream().gather(Gatherers.monotoneSequences(comparator)).toList();
     * // then
     * // result == [1, 2, 3], [3, 1], [1, 2, 3]
     *}
     * </p>
     * {@code null}s are swallowed.
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
     * A sequential gatherer which pushes an additional item downstream after each item it receives.
     * Example generating a constant value:
     * {@snippet :
     * // given
     * var list = List.of(1, 2, 3);
     * // when
     * var result = list.stream().gather(Gatherers.interleave(()->7)).toList();
     * //then
     * assert result.equals(List.of(1, 7, 2, 7, 3, 7));
     *}
     * Example generating a random number:
     * {@snippet :
     * // given
     * import java.util.concurrent.ThreadLocalRandom;
     * var list = List.of(1, 2, 3);
     * var rnd = ThreadLocalRandom.current();
     * // when
     * var result = list.stream().gather(Gatherers.interleave(rnd::nextInt)).toList();
     * // then
     * assert result.size()==6;
     * assert result.get(0)==1 && result.get(2)==2 && result.get(4)==3;
     *}
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

    public static <T> Gatherer<T, Void, T> interleaveRotating(Collection<? extends T> source) {
        if(requireNonNull(source).isEmpty()) {
            throw new IllegalArgumentException("The source cannot be empty");
        }
        return interleave(
                new Supplier<>() {
                    private Iterator<? extends T> iter = source.iterator();
                    @Override
                    public T get() {
                        if(!iter.hasNext()) {
                            iter = source.iterator();
                        }
                        return iter.next();
                    }
                }
        );
    }

    public static <T> Gatherer<T, Iterator<? extends T>, T> interleaveAvailable(Collection<? extends T> source) {
        return Gatherer.ofSequential(
                source::iterator,
                (Iterator<? extends T> iterator, T element, Gatherer.Downstream<? super T> downstream) -> {
                    if(downstream.isRejecting()) {
                        return false;
                    } else {
                        boolean down = downstream.push(element);
                        if(down && iterator.hasNext()) {
                            down = downstream.push(iterator.next());
                        }
                        return down;
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
                        order = switch (sign(comparator.compare(elements.getLast(), item))) {
                            case NEGATIVE -> Order.INCREASING;
                            case POSITIVE -> Order.DECREASING;
                            default -> null;
                        };
                        return elements.add(item);
                    } else {
                        if (order == Order.INCREASING && comparator.compare(elements.getLast(), item) > 0
                                || order == Order.DECREASING && comparator.compare(elements.getLast(), item) < 0
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
}
