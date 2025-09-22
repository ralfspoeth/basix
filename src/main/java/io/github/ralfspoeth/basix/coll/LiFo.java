package io.github.ralfspoeth.basix.coll;

import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

sealed interface LiFo<S extends LiFo<S, T>, T> permits Stack, ConcurrentStack {

    /**
     * Pop the topmost element if it is not null.
     */
    default Optional<T> popIfNotEmpty() {
        return popIf(Objects::nonNull);
    }

    Optional<T> popIf(Predicate<? super T> condition);

    /**
     *
     * @param data
     * @return
     */
    default S pushIfEmpty(T data) {
        return pushUnless(data, Objects::nonNull);
    }

    S pushUnless(T data, Predicate<? super T> condition);

    boolean isEmpty();

    T pop();

    @Nullable T top();

    S push(T elem);
}
