package io.github.ralfspoeth.basix.coll;

import java.util.Collection;
import java.util.Optional;

/**
 * Common operations for first-in, first-out data structures.
 * @param <S> the actual implementation
 * @param <T> the type of the elements in the structure
 */
public interface FiFo<S extends FiFo<S, T>, T> {

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
