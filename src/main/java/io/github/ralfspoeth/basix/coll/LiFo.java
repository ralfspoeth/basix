package io.github.ralfspoeth.basix.coll;

import java.util.Optional;
import java.util.function.Predicate;

public interface LiFo<S extends BaseStack<S, T>, T> {
    Optional<T> popIfNotEmpty();

    Optional<T> popIf(Predicate<? super T> condition);

    S pushIfEmpty(T data);

    S pushUnless(T data, Predicate<? super T> condition);

    boolean isEmpty();

    T pop();

    T top();

    S push(T elem);
}
