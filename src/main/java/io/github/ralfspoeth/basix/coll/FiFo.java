package io.github.ralfspoeth.basix.coll;

import java.util.Optional;

sealed interface FiFo<S extends FiFo<S, T>, T> permits Queue, ConcurrentQueue {

    boolean isEmpty();

    S add(T item);

    T remove();

    Optional<T> head();

    Optional<T> tail();
}
