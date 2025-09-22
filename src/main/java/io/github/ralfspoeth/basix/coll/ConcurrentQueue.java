package io.github.ralfspoeth.basix.coll;

import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Concurrent version of a FIFO queue.
 *
 * @param <T>
 */
public final class ConcurrentQueue<T> implements FiFo<ConcurrentQueue<T>, T> {

    private final ConcurrentLinkedDeque<T> internal = new ConcurrentLinkedDeque<>();

    @Override
    public boolean isEmpty() {
        return internal.isEmpty();
    }

    @Override
    public ConcurrentQueue<T> add(T item) {
        internal.add(item);
        return this;
    }

    @Override
    public T remove() {
        return internal.remove();
    }

    @Override
    public Optional<T> head() {
        return Optional.ofNullable(internal.peekFirst());
    }

    @Override
    public Optional<T> tail() {
        return Optional.ofNullable(internal.peekLast());
    }
}
