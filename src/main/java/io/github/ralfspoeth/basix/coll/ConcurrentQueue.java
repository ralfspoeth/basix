package io.github.ralfspoeth.basix.coll;

import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Concurrent version of a FIFO queue.
 *
 * @param <T>
 */
public final class ConcurrentQueue<T> extends BaseQueue<ConcurrentQueue<T>, T> {

    private final Lock lock = new ReentrantLock();

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
    public ConcurrentQueue<T> add(T item) {
        lock.lock();
        try {
            return super.add(item);
        }
        finally {
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
    public Optional<T> head() {
        lock.lock();
        try {
            return super.head();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Optional<T> tail() {
        lock.lock();
        try {
            return super.tail();
        } finally {
            lock.unlock();
        }
    }
}
