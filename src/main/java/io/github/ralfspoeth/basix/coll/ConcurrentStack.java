package io.github.ralfspoeth.basix.coll;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;


/**
 * The concurrent, i.e. thread-safe variant of a stack
 * which offers atomic test-and-modify operations.
 */
public final class ConcurrentStack<T> extends BaseStack<T> {

    // used to protect access to the internal state of the stack
    private final Lock lock = new ReentrantLock();

    /**
     * {@link super#popIf(Predicate)} implemented in a thread-safe way.
     */
    @Override
    public Optional<T> popIf(Predicate<? super T> condition) {
        lock.lock();
        try {
            return condition.test(top()) ? Optional.of(pop()) : Optional.empty();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public ConcurrentStack<T> pushIfEmpty(T data) {
        return pushUnless(data, Objects::nonNull);
    }

    @Override
    public ConcurrentStack<T> pushUnless(T data, Predicate<? super T> condition) {
        lock.lock();
        try {
            return condition.test(super.top()) ? this : (ConcurrentStack<T>) super.push(data);
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
    public T top() {
        lock.lock();
        try {
            return super.top();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public ConcurrentStack<T> push(T elem) {
        lock.lock();
        try {
            return (ConcurrentStack<T>) super.push(elem);
        } finally {
            lock.unlock();
        }
    }
}
