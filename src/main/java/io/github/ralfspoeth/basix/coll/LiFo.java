package io.github.ralfspoeth.basix.coll;

import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Common operations for last-in, first-out data structures.
 * @param <S> the actual implementation
 * @param <T> the type of the elements in the structure
 */
public interface LiFo<S extends LiFo<S, T>, T> {

    /**
     * Add an element to the structure.
     * This will be the last element added and the first to be removed.
     * @param elem the element to be added
     * @return {@code this}
     */
    S push(T elem);

    /**
     * The most recently added element.
     * @return the topmost element, may be {@code null}
     */
    @Nullable
    T top();

    /**
     * Push only if the topmost elements meets the condition.
     * @param data the element to be added
     * @param condition the condition applied to the topmost element
     * @return {@code this}, regardless of the addition has been successful or not
     */
    S pushIf(T data, Predicate<? super @Nullable T> condition);

    /**
     * Push only if the topmost element is {@code null}.
     * @param data the element
     * @return {@code this}
     */
    default S pushIfEmpty(T data) {
        return pushIf(data, Objects::isNull);
    }

    /**
     * Remove and return the topmost element of the structure.
     * @return the topmost element
     * @throws java.util.NoSuchElementException when empty
     */
    T pop();

    /**
     * Remove and return the topmost element if it meets
     * the given condition.
     * @param condition the condition
     * @return an optional wrapping the topmost element, or an empty optional
     */
    Optional<T> popIf(Predicate<? super @Nullable T> condition);

    /**
     * Remove and return the topmost element if it is not {@code null}.
     */
    default Optional<T> popIfNotEmpty() {
        return popIf(Objects::nonNull);
    }

    /**
     * Test whether empty or not; cf.{@link Collection#isEmpty()}.
     * @return {@code true} if empty
     */
    boolean isEmpty();
}
