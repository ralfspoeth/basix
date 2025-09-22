package io.github.ralfspoeth.basix.coll;

import org.jspecify.annotations.Nullable;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;


/**
 * The concurrent, i.e. thread-safe variant of a stack
 * which offers atomic test-and-modify operations.
 */
public final class ConcurrentStack<T> implements LiFo<ConcurrentStack<T>, T> {

    private static class Node<T> {
        final T data;
        @Nullable Node<T> next;

        Node(T data) {
            this.data = data;
        }
    }

    // Assuming the top of the stack is managed by AtomicReference
    private final AtomicReference<Node<T>> top = new AtomicReference<>();

    @Override
    public boolean isEmpty() {
        return top.get() == null;
    }

    @Override
    public Optional<T> popIf(Predicate<? super T> condition) {
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
    public ConcurrentStack<T> pushUnless(T data, Predicate<? super T> condition) {
        Node<T> oldTop;
        Node<T> newNode;
        do {
            oldTop = top.get();
            if(condition.test(data)) {
                return this;
            } else {
                newNode = new Node<T>(data);
                newNode.next = oldTop;
            }
        } while (!top.compareAndSet(oldTop, newNode));
        return this;
    }

    @Override
    public T pop() {
        return popIf(t -> true).orElseThrow(NoSuchElementException::new);
    }

    @Override
    public ConcurrentStack<T> push(T elem){
        Node<T> newNode = new Node(elem);
        Node<T> oldTop;
        do {
            oldTop = top.get();
            newNode.next = oldTop;
        } while (!top.compareAndSet(oldTop, newNode));
        return this;
    }

    @Override
    public T top() {
        Node<T> d = top.get();
        return d == null ? null : d.data;
    }

}
