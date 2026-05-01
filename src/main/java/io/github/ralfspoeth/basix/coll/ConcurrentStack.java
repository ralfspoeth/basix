package io.github.ralfspoeth.basix.coll;

import org.jspecify.annotations.Nullable;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;


/**
 * The concurrent, i.e. thread-safe variant of a stack
 * which offers atomic test-and-modify operations.
 * <p>
 * Implemented as a lock-free Treiber stack using a single
 * {@link AtomicReference} as the head pointer, so {@link #push},
 * {@link #pop}, {@link #pushIf}, and {@link #popIf} are atomic
 * with respect to one another.
 *
 * @param <T> the element type
 */
public final class ConcurrentStack<T> implements LiFo<ConcurrentStack<T>, T> {

    /**
     * Creates a new, empty concurrent stack.
     */
    public ConcurrentStack() {}

    private static class Node<T> {
        final T data;
        @Nullable Node<T> next;

        Node(T data) {
            this.data = data;
        }
    }

    // Assuming the top of the stack is managed by AtomicReference
    private final AtomicReference<@Nullable Node<T>> top = new AtomicReference<>();

    @Override
    public boolean isEmpty() {
        return top.get() == null;
    }

    @Override
    public Optional<T> popIf(Predicate<? super @Nullable T> condition) {
        Node<T> currentTop, next;
        T data;
        do {
            currentTop = top.get();
            if (currentTop == null) {
                throw new NoSuchElementException();
            }
            else {
                data = currentTop.data;
                if(condition.test(data)) {
                    next = currentTop.next;
                } else {
                    return Optional.empty();
                }
            }
        } while (!top.compareAndSet(currentTop, next));
        return Optional.of(data);
    }

    @Override
    public ConcurrentStack<T> pushIf(T data, Predicate<? super @Nullable T> condition) {
        Node<T> newNode = new Node<>(data);
        Node<T> oldTop;
        do {
            oldTop = top.get();
            if (!condition.test(oldTop == null ? null : oldTop.data)) {
                return this;
            }
            newNode.next = oldTop;
        } while (!top.compareAndSet(oldTop, newNode));
        return this;
    }

    @Override
    public T pop() {
        return popIf(_ -> true).orElseThrow(NoSuchElementException::new);
    }


    @Override
    public ConcurrentStack<T> push(T elem){
        Node<T> newNode = new Node<>(elem);
        Node<T> oldTop;
        do {
            oldTop = top.get();
            newNode.next = oldTop;
        } while (!top.compareAndSet(oldTop, newNode));
        return this;
    }

    @Override
    public @Nullable T top() {
        Node<T> d = top.get();
        return d == null ? null : d.data;
    }

}
