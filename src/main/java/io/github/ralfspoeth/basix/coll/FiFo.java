package io.github.ralfspoeth.basix.coll;

import java.util.Optional;

public interface FiFo<S extends BaseQueue<S, T>, T> {
    void growIfExhausted();

    boolean isEmpty();

    S add(T item);

    T remove();

    Optional<T> head();

    Optional<T> tail();
}
