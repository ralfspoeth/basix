package io.github.ralfspoeth.basix.coll;

import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Thread-safe FIFO queue, backed by a {@link ConcurrentLinkedDeque}.
 * All operations are non-blocking; the {@link #head()} and {@link #tail()}
 * snapshots reflect the state of the queue at the moment of the call and
 * may be stale by the time the caller inspects them.
 *
 * @param <T> the element type
 */
public final class ConcurrentQueue<T> implements FiFo<ConcurrentQueue<T>, T> {

    /**
     * Creates a new, empty concurrent queue.
     */
    public ConcurrentQueue() {}

    private final ConcurrentLinkedDeque<T> internalQueue = new ConcurrentLinkedDeque<>();

    @Override
    public boolean isEmpty() {
        return internalQueue.isEmpty();
    }

    @Override
    public ConcurrentQueue<T> add(T item) {
        internalQueue.add(item);
        return this;
    }

    @Override
    public T remove() {
        return internalQueue.remove();
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation delegates to {@link ConcurrentLinkedDeque#pollFirst()},
     * which is atomic and lock-free.
     */
    @Override
    public Optional<T> removeIfNotEmpty() {
        return Optional.ofNullable(internalQueue.pollFirst());
    }

    @Override
    public Optional<T> head() {
        return Optional.ofNullable(internalQueue.peekFirst());
    }

    @Override
    public Optional<T> tail() {
        return Optional.ofNullable(internalQueue.peekLast());
    }
}
