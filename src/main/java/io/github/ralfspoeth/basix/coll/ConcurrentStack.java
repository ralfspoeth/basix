package io.github.ralfspoeth.basix.coll;

import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;


/**
 * The concurrent, i.e. thread-safe variant of a stack
 * which offers atomic test-and-modify operations.
 */
public final class ConcurrentStack<T> extends BaseStack<ConcurrentStack<T>, T> {

    private static class Node<T> {
        final T data;
        @Nullable Node<T> next;

        Node(T data) {
            this.data = data;
        }
    }

    // Assuming the top of the stack is managed by AtomicReference
    private final AtomicReference<Node<T>> top = new AtomicReference<>();

    /**
     * {@link BaseStack#popIf(Predicate)} implemented in a thread-safe way.
     */
    @Override
    public Optional<T> popIf(Predicate<? super @Nullable T> condition) {
        lock.lock();
        try {
            return condition.test(top()) ? Optional.of(pop()) : Optional.empty();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public ConcurrentStack<T> push(T elem) {
        lock.lock();
        try {
            return super.push(elem);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean isEmpty() {
        lock.lock();
        try {
            return super.isEmpty();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public T pop() {
        lock.lock();
        try {
            return super.pop();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public @Nullable T top() {
        lock.lock();
        try {
            return super.top();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public ConcurrentStack<T> pushIf(T data, Predicate<? super @Nullable T> condition) {
        lock.lock();
        try {
            return super.pushIf(data, condition);
        } finally {
            lock.unlock();
        }
    }
}
