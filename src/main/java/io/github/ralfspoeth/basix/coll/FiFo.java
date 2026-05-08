package io.github.ralfspoeth.basix.coll;

import java.util.Collection;
import java.util.Optional;

/**
 * Common operations for first-in, first-out data structures.
 * @param <S> the actual implementation
 * @param <T> the type of the elements in the structure
 */
public sealed interface FiFo<S extends FiFo<S, T>, T> permits Queue, ConcurrentQueue {

    /**
     * Test whether empty or not; cf.{@link Collection#isEmpty()}.
     * @return {@code true} if empty
     */
    boolean isEmpty();

    /**
     * Add an element to the logical end of the structure.
     * @param item the element to be added
     * @return {@code  this}
     */
    S add(T item);

    /**
     * Remove and return an element from the logical start of the structure.
     * @return the element removed
     * @throws java.util.NoSuchElementException when empty
     */
    T remove();

    /**
     * Atomically remove and return the element at the head of the structure
     * if it is non-empty, or return an empty {@link Optional} otherwise.
     * <p>
     * Unlike {@link #remove()}, this method never throws
     * {@link java.util.NoSuchElementException}.
     * <p>
     * The default implementation performs a separate {@link #isEmpty()} test
     * followed by {@link #remove()} and is therefore <em>not</em> atomic;
     * concurrent implementations must override this method to provide
     * test-and-remove atomicity.
     *
     * @return an {@link Optional} wrapping the removed head element, or an
     *         empty optional if the structure was empty
     */
    default Optional<T> removeIfNotEmpty() {
        return isEmpty() ? Optional.empty() : Optional.of(remove());
    }

    /**
     * The logical start (or head) of the structure, wrapped in an {@link Optional}.
     * @return the head wrapped in an {@link Optional}, or an empty optional
     */
    Optional<T> head();

    /**
     * The logical end (or tail) of the structure, wrapped in an {@link Optional}.
     * @return the head wrapped in an {@link Optional}, or an empty optional
     */
    Optional<T> tail();
}
