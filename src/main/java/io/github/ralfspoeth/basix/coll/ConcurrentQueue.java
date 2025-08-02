package io.github.ralfspoeth.basix.coll;

import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;

/**
 * Concurrent version of a FIFO queue.
 *
 * @param <T>
 */
public final class ConcurrentQueue<T> extends BaseQueue<T> {

    private final Lock lock = new ReentrantLock();

    @Override
    public ConcurrentQueue<T> add(T item) {
        lock.lock();
        try {
            return (ConcurrentQueue<T>) super.add(item);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public ConcurrentQueue<T> addIfNotEmpty(T item) {
        lock.lock();
        try {
            return (ConcurrentQueue<T>) super.addIfNotEmpty(item);
        }
        finally {
            lock.unlock();
        }
    }

    @Override
    public ConcurrentQueue<T> addIfQueue(T item, Predicate<? super BaseQueue<T>> condition) {
        lock.lock();
        try {
            return (ConcurrentQueue<T>) super.addIfQueue(item, condition);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public ConcurrentQueue<T> addIfTail(T item, Predicate<? super T> condition) {
        lock.lock();
        try {
            return (ConcurrentQueue<T>) super.addIfTail(item, condition);
        }
        finally {
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
    public T remove() {
        lock.lock();
        try {
            return super.remove();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Optional<T> removeIfHead(Predicate<? super T> condition) {
        lock.lock();
        try {
            return super.removeIfHead(condition);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Optional<T> removeIfQueue(Predicate<? super BaseQueue<T>> condition) {
        lock.lock();
        try {
            return super.removeIfQueue(condition);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public T head() {
        lock.lock();
        try {
            return super.head();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public T tail() {
        lock.lock();
        try {
            return super.tail();
        } finally {
            lock.unlock();
        }
    }
}
