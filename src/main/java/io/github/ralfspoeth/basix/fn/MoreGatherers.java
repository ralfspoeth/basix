package io.github.ralfspoeth.basix.fn;

import io.github.ralfspoeth.basix.coll.Stack;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.function.Supplier;
import java.util.stream.Gatherer;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * Factory methods for {@link Gatherer}s which complement those
 * in {@link java.util.stream.Gatherers}.
 * <p>
 * Three families of gatherers are provided:
 * <ul>
 *     <li>comparison-based: {@link #distinctUntilChanged()}, {@link #distinctUntilChanged(Comparator)},
 *     {@link #increasing(Comparator)}, {@link #decreasing(Comparator)},
 *     and {@link #monotoneSequences(Comparator)}</li>
 *     <li>buffering: {@link #reverse()}, {@link #single()}, and {@link #exactly(int)}</li>
 *     <li>interleaving: {@link #interleave(Supplier)}, {@link #intersperse(Object)},
 *     {@link #interleaveRotating(Collection)}, {@link #interleaveAvailable(Collection)},
 *     and {@link #interleaveAppendRest(Collection)}</li>
 * </ul>
 * <p>
 * Null policy: the comparison-based gatherers reject {@code null} elements
 * with a {@link NullPointerException} while the stream is being processed,
 * in line with the non-null type parameter bounds implied by {@code @NullMarked}.
 * Gatherers which are declared with a {@code <T extends @Nullable Object>} type
 * parameter tolerate {@code null} elements and pass them through.
 */
public final class MoreGatherers {

    // prevent instantiation
    private MoreGatherers() {}

    private enum Order {
        INCREASING,
        DECREASING
    }

    /**
     * Sequential gatherer that removes any consecutively equal elements,
     * pushing an element downstream only when it differs from the last one pushed.
     * The name follows the convention established by reactive libraries
     * for this operation.
     * {@snippet :
     * assert List.of(1).equals(Stream.of(1, 1, 1).gather(MoreGatherers.distinctUntilChanged()).toList());
     * assert List.of(1, 2).equals(Stream.of(1, 2, 2).gather(MoreGatherers.distinctUntilChanged()).toList());
     * assert List.of(1, 2, 1).equals(Stream.of(1, 2, 1).gather(MoreGatherers.distinctUntilChanged()).toList());
     *}
     * <p>
     * Note that this gatherer is different from the {@link Stream#distinct()} built-in method
     * since every element of upstream is compared to the last element pushed downstream only,
     * in contrast to all previous elements as {@link Stream#distinct()} does.
     * Furthermore, note that {@code null} elements are rejected with a
     * {@link NullPointerException} while the stream is being processed.
     *
     * @param <T> the element type
     * @return a gatherer removing consecutively equal elements
     */
    public static <T> Gatherer<T, ?, T> distinctUntilChanged() {
        return Gatherer.<T, AtomicReference<@Nullable T>, T>ofSequential(
                AtomicReference::new,
                (state, element, downstream) -> {
                    // this check is load-bearing: the paths below may push nothing,
                    // so it is the only way to observe downstream cancellation
                    if (downstream.isRejecting()) {
                        return false;
                    } else if (!requireNonNull(element).equals(state.get())) {
                        state.set(element);
                        return downstream.push(element);
                    } else {
                        return true;
                    }
                }
        );
    }

    /**
     * Same as {@link #distinctUntilChanged()} but with the elements being compared
     * to the previous element pushed downstream using a {@link Comparator};
     * an element is pushed if and only if it compares to non-zero with the last one pushed.
     * {@code null} elements are rejected with a {@link NullPointerException}
     * while the stream is being processed.
     *
     * @param comparator the comparator
     * @param <T>        the type of the elements
     * @return a gatherer removing consecutive elements which compare to zero
     */
    public static <T> Gatherer<T, ?, T> distinctUntilChanged(Comparator<? super T> comparator) {
        return comparingLastPushed(comparator, compared -> compared != 0);
    }

    /**
     * Stateful gatherer which pushes an element downstream only if it is strictly greater
     * than the most recently pushed element, as determined by the given comparator;
     * all other elements are dropped.
     * {@snippet :
     * assert List.of(1, 2, 3, 4).equals(
     *         Stream
     *             .of(1, 2, 1, 3, 1, 4)
     *             .gather(MoreGatherers.increasing())
     *             .toList()
     * );
     *}
     * Note that in the given example the first occurrences of 2, 3, and 4, respectively
     * are being pushed downstream.
     * {@code null} elements are rejected with a {@link NullPointerException}
     * while the stream is being processed.
     *
     * @param comparator the comparator
     * @param <T>        the type of the stream elements.
     * @return a gatherer producing a stream of strictly increasing elements
     */
    public static <T> Gatherer<T, ?, T> increasing(Comparator<? super T> comparator) {
        return comparingLastPushed(comparator, compared -> compared > 0);
    }

    /**
     * Same as {@link #increasing(Comparator)} using {@link Comparator#naturalOrder()} as comparator.
     */
    public static <T extends Comparable<? super T>> Gatherer<T, ?, T> increasing() {
        return increasing(Comparator.naturalOrder());
    }

    /**
     * Same as {@link #increasing(Comparator)} yet the opposite order.
     *
     * @param comparator the comparator
     * @param <T>        the type of the stream elements.
     * @return a gatherer producing a stream of strictly decreasing elements
     */
    public static <T> Gatherer<T, ?, T> decreasing(Comparator<? super T> comparator) {
        return comparingLastPushed(comparator, compared -> compared < 0);
    }

    /**
     * Same as {@link #decreasing(Comparator)} using {@link Comparator#naturalOrder()} as comparator.
     */
    public static <T extends Comparable<? super T>> Gatherer<T, ?, T> decreasing() {
        return decreasing(Comparator.naturalOrder());
    }

    /**
     * Common implementation of {@link #distinctUntilChanged(Comparator)}, {@link #increasing(Comparator)},
     * and {@link #decreasing(Comparator)}: compares each element with the last element pushed
     * downstream and pushes it only if the comparison result satisfies the given predicate.
     */
    private static <T> Gatherer<T, AtomicReference<@Nullable T>, T> comparingLastPushed(
            Comparator<? super T> comparator,
            IntPredicate acceptComparisonResult
    ) {
        return Gatherer.ofSequential(
                AtomicReference::new,
                (state, element, downstream) -> {
                    requireNonNull(element);
                    // this check is load-bearing: the paths below may push nothing,
                    // so it is the only way to observe downstream cancellation
                    if (downstream.isRejecting()) {
                        return false;
                    } else {
                        var current = state.get();
                        if (current == null || acceptComparisonResult.test(comparator.compare(element, current))) {
                            state.set(element);
                            return downstream.push(element);
                        } else {
                            return true;
                        }
                    }
                }
        );
    }

    /**
     * Stateful sequential gatherer which reverses the stream of elements.
     * {@snippet :
     * assert List.of(3, 2, 1)
     *     .equals(Stream.of(1, 2, 3)
     *         .gather(MoreGatherers.reverse())
     *         .toList()
     * );
     *}
     *
     * @param <T> the element type of the stream
     * @return a gatherer which reverses the encountering order
     */
    public static <T> Gatherer<T, ?, T> reverse() {
        return Gatherer.<T, Stack<T>, T>ofSequential(
                Stack::new,
                (stack, element, downstream) -> {
                    // this check is load-bearing: the paths below may push nothing,
                    // so it is the only way to observe downstream cancellation
                    if (downstream.isRejecting()) {
                        return false;
                    } else {
                        stack.push(element);
                        return true;
                    }
                },
                (stack, downstream) -> {
                    while (!stack.isEmpty() && !downstream.isRejecting()) {
                        downstream.push(stack.pop());
                    }
                }
        );
    }

    /**
     * A gatherer which pushes an element from the upstream down
     * if it is the only element in the upstream.
     * {@snippet :
     * import java.util.stream.Stream;
     * Stream.of(1)
     *     .gather(MoreGatherers.single())
     *     .findFirst()
     *     .orElseThrow(); // 1
     * assert Stream.of(1, 2)
     *     .gather(MoreGatherers.single())
     *     .findFirst()
     *     .isEmpty(); // true
     *}
     * Note that empty streams remain exactly this: empty.
     * {@snippet :
     * var list = Stream.of()
     *      .gather(MoreGatherers.single())
     *      .toList(); // empty list
     *}
     * A single {@code null} element is passed through, see {@link #exactly(int)}.
     *
     * @param <T> the element type
     * @return a gatherer, may be used in parallel streams
     */
    public static <T extends @Nullable Object> Gatherer<T, ?, T> single() {
        return exactly(1);
    }

    /**
     * A gatherer which pushes exactly {@code n} elements downstream
     * if and only if the upstream delivers exactly {@code n}.
     * Note that {@code null}s are accepted and passed through.
     *
     * @param n   the number of elements to be met, must not be negative
     * @param <T> the element type
     * @return a gatherer, may be used in parallel streams
     * @throws IllegalArgumentException if {@code n} is negative
     */
    public static <T extends @Nullable Object> Gatherer<T, ?, T> exactly(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("n must not be negative: " + n);
        }
        return Gatherer.<T, Collection<T>, T>of(
                ArrayList::new,
                (elementsSoFar, elem, downstream) -> {
                    // this check is load-bearing: the paths below may push nothing,
                    // so it is the only way to observe downstream cancellation
                    if (downstream.isRejecting()) {
                        return false;
                    } else {
                        return elementsSoFar.add(elem) && elementsSoFar.size() <= n;
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

    /**
     * Same as {@link #monotoneSequences(Comparator)} using {@link Comparator#naturalOrder()}
     * as comparator.
     */
    public static <T extends Comparable<? super T>> Gatherer<T, ?, List<T>> monotoneSequences() {
        return monotoneSequences(Comparator.naturalOrder());
    }

    /**
     * Sequential gatherer which produces a stream of lists each of which are in increasing or decreasing order.
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
     * var result = input.stream().gather(MoreGatherers.monotoneSequences(comparator)).toList();
     * // then
     * // result == [1, 2, 3], [3, 1], [1, 2, 3]
     *}
     * </p>
     * {@code null} elements are rejected with a {@link NullPointerException}
     * while the stream is being processed.
     * <p>
     * The resulting lists are never empty so that one may detect the order of a list
     * by comparing the first and the last element easily:
     * {@snippet :
     * var result = List.<Integer>of(); // @replace substring="List.<Integer>of()" replacement="(see above)"
     * int ordering = Comparator.<Integer>naturalOrder().compare(result.getFirst(), result.getLast()); // @replace substring="Comparator.<Integer>naturalOrder()" replacement="comparator"
     *}
     * </p>
     * <p>
     * Comparing the first and the last element is the preferred way to determine the ordering
     * of the lists because it covers the edge cases
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
    public static <T> Gatherer<T, ?, List<T>> monotoneSequences(Comparator<? super T> comparator) {
        return Gatherer.<T, ContCollection<T>, List<T>>ofSequential(
                () -> new ContCollection<>(comparator),
                (coll, item, downstream) -> {
                    requireNonNull(item);
                    // this check is load-bearing: the paths below may push nothing,
                    // so it is the only way to observe downstream cancellation
                    if (downstream.isRejecting()) {
                        return false;
                    } else if (coll.add(item)) {
                        return true;
                    } else {
                        boolean more = downstream.push(coll.stream().toList());
                        T last = coll.getLast();
                        coll.clear();
                        coll.add(last);
                        coll.add(item);
                        return more;
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
     * var result = list.stream().gather(MoreGatherers.interleave(()->7)).toList();
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
     * var result = list.stream().gather(MoreGatherers.interleave(rnd::nextInt)).toList();
     * // then
     * assert result.size()==6;
     * assert result.get(0)==1 && result.get(2)==2 && result.get(4)==3;
     *}
     * <p>
     * Note that the generated item is also pushed after the <em>last</em> upstream
     * element; if you are looking for separator semantics — an item <em>between</em>
     * any two consecutive elements only — use {@link #intersperse(Object)} instead.
     *
     * @param generator a supplier which may produces the same or a new item everytime
     *                  it is called; doesn't prevent {@code null} elements
     * @param <T>       the element type
     * @return a gatherer
     */
    public static <T extends @Nullable Object> Gatherer<T, ?, T> interleave(Supplier<? extends T> generator) {
        // no isRejecting() check needed: every path pushes,
        // and push() reports rejection through its return value
        return Gatherer.ofSequential((_, item, downstream) ->
                downstream.push(item) && downstream.push(generator.get())
        );
    }

    /**
     * A sequential gatherer which pushes the given separator between any two
     * consecutive upstream elements — but neither before the first nor after the last.
     * {@snippet :
     * // given
     * var list = List.of(1, 2, 3);
     * // when
     * var result = list.stream().gather(MoreGatherers.intersperse(0)).toList();
     * // then
     * assert result.equals(List.of(1, 0, 2, 0, 3));
     *}
     * Empty and singleton streams are passed through unchanged.
     * In contrast to {@link #interleave(Supplier)} no separator trails the last element.
     * The separator may be {@code null}.
     *
     * @param separator the separator to push between consecutive elements
     * @param <T>       the element type
     * @return a gatherer
     */
    public static <T extends @Nullable Object> Gatherer<T, ?, T> intersperse(T separator) {
        return Gatherer.ofSequential(
                AtomicBoolean::new, // false: nothing pushed downstream yet
                // no isRejecting() check needed: every path pushes,
                // and push() reports rejection through its return value
                (anyPushed, element, downstream) -> {
                    if (anyPushed.getAndSet(true) && !downstream.push(separator)) {
                        return false;
                    } else {
                        return downstream.push(element);
                    }
                }
        );
    }

    /**
     * A gatherer which interposes elements from the given {@code source} collection,
     * re-starting with the first element when the last has been used; it's
     * {@link #interleave(Supplier)} with a rotating or circulating supplier.
     * Example:
     * {@snippet :
     * // given
     * import java.util.stream.IntStream;
     * var onetwo = List.of(1, 2);
     * // when
     * var rg = IntStream.range(0, 10).boxed();
     * // then
     * var result = rg.gather(interleaveRotating(onetwo)).toList();
     * assert result.equals(List.of(0, 1, 1, 2, 2, 1, 3, 2, 4, 1, 5, 2, 6, 1, 7, 2, 8, 1, 9, 2));
     *}
     *
     * @param source the collection the elements of which are inserted into the downstream,
     *               must not be empty
     * @throws IllegalArgumentException if {@code source} is empty
     */
    public static <T> Gatherer<T, ?, T> interleaveRotating(Collection<? extends T> source) {
        if (requireNonNull(source).isEmpty()) {
            throw new IllegalArgumentException("The source cannot be empty");
        }
        return Gatherer.<T, AtomicReference<Iterator<? extends T>>, T>ofSequential(
                () -> new AtomicReference<>(source.iterator()),
                // no isRejecting() check needed: every path pushes,
                // and push() reports rejection through its return value
                (state, element, downstream) -> {
                    boolean down = downstream.push(element);
                    if (down) {
                        if (!state.get().hasNext()) {
                            state.set(source.iterator());
                        }
                        down = downstream.push(state.get().next());
                    }
                    return down;
                }
        );
    }

    /**
     * A gatherer which interposes elements from the given {@code source} collection
     * until it is exhausted; the stream continues to push downstream every element from the upstream
     * even then.
     * {@snippet :
     * // given
     * var list = List.of(1, 2, 3, 4);
     * var interleave = List.of(100, 1000); // just two of them
     * // when
     * var result = list.stream().gather(MoreGatherers.interleaveAvailable(interleave)).toList();
     * // then
     * assert result.equals(List.of(1, 100, 2, 1000, 3, 4));
     *}
     *
     * @param source the collection
     */
    public static <T> Gatherer<T, ?, T> interleaveAvailable(Collection<? extends T> source) {
        return Gatherer.<T, Iterator<? extends T>, T>ofSequential(
                source::iterator,
                MoreGatherers::pushAndInterpose
        );
    }

    /**
     * A gatherer which interposes elements from a collection
     * into the stream until the elements from collection are exhausted,
     * and adds the remaining elements to the tail of the stream if it is done.
     * If the source collection is smaller than the upstream it behaves like
     * {@link #interleaveAvailable(Collection)}.
     * Example:
     * {@snippet :
     * // given
     * var stream = Stream.of(1, 2);
     * var coll = List.of(91, 92, 93, 94);
     * // when
     * var result = stream.gather(interleaveAppendRest(coll)).toList();
     * // then
     * assert result.equals(List.of(1, 91, 2, 92, 93, 94));
     *}
     *
     * @param source the collection of elements to inserted and finally appended to the stream
     */
    public static <T> Gatherer<T, ?, T> interleaveAppendRest(Collection<? extends T> source) {
        return Gatherer.<T, Iterator<? extends T>, T>ofSequential(
                source::iterator,
                MoreGatherers::pushAndInterpose,
                (iterator, downstream) -> {
                    while (iterator.hasNext() && !downstream.isRejecting()) {
                        downstream.push(iterator.next());
                    }
                }
        );
    }

    /**
     * Common integrator of {@link #interleaveAvailable(Collection)} and
     * {@link #interleaveAppendRest(Collection)}: pushes the upstream element
     * followed by the next element of the iterator, if any.
     */
    private static <T> boolean pushAndInterpose(
            Iterator<? extends T> iterator,
            T element,
            Gatherer.Downstream<? super T> downstream
    ) {
        // no isRejecting() check needed: every path pushes,
        // and push() reports rejection through its return value
        boolean down = downstream.push(element);
        if (down && iterator.hasNext()) {
            down = downstream.push(iterator.next());
        }
        return down;
    }

    /**
     * Returns a {@code Gatherer} that applies the given function to each element and pushes the
     * contained value downstream whenever the resulting {@code Optional} is present. Elements for
     * which the function returns an empty {@code Optional} are discarded.
     *
     * <p>This combines mapping and filtering into a single step, which is convenient for operations
     * whose result may be absent, such as parsing or lookups:
     * <p>
     * {@snippet :
     * var _ = Stream.of("1", "two", "3")
     *       .gather(present(Parsers::tryParseInt))
     *       .toList(); // [1, 3]
     *}
     *
     * <p>The returned gatherer is stateless and its integrator is greedy: every element is consumed
     * and handled independently, in encounter order, so the gatherer can be used with parallel
     * streams and does not prevent short-circuiting downstream.
     *
     * @param f   the function applied to each element; must not be {@code null} and must not
     *            return {@code null}
     * @param <T> the type of the input elements
     * @param <R> the type of the elements pushed downstream
     * @return a stateless {@code Gatherer} that maps and filters in one step
     */
    public static <T, R> Gatherer<T, ?, R> present(Function<? super @Nullable T, Optional<? extends R>> f) {
        return Gatherer.of(
                Gatherer.Integrator.ofGreedy(
                        (_, t, d) -> f.apply(t).map(d::push).orElse(true)
                )
        );
    }

    // helper class used in continuous gatherers
    private static class ContCollection<T> extends AbstractSequentialList<T> {

        private final Comparator<? super T> comparator;

        private final List<T> elements = new ArrayList<>();
        private @Nullable Order order = null;

        private ContCollection(Comparator<? super T> comparator) {
            this.comparator = comparator;
        }

        @Override
        public boolean add(T item) {
            requireNonNull(item);
            if (elements.isEmpty()) {
                return elements.add(item);
            } else if (order == null) {
                order = switch (Sign.ofCompare(comparator.compare(elements.getLast(), item))) {
                    case NEGATIVE -> Order.INCREASING;
                    case POSITIVE -> Order.DECREASING;
                    case ZERO -> null;
                };
                return elements.add(item);
            } else {
                return (order == Order.INCREASING
                        && comparator.compare(elements.getLast(), item) <= 0
                        || order == Order.DECREASING
                        && comparator.compare(elements.getLast(), item) >= 0
                ) && elements.add(item);
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
