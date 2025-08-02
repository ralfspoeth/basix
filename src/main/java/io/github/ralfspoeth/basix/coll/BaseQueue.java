package io.github.ralfspoeth.basix.coll;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;

public sealed class BaseQueue<T> permits Queue, ConcurrentQueue {

    @SuppressWarnings("unchecked")
    private T[] data = (T[]) Array.newInstance(Object.class, 16);
    private int next = 0;
    private int top = 0;

    private void checkSize() {
        assert top <= next;
        // next == top -> empty
        // we can move both pointers back to the start
        if (top == next) {
            Arrays.fill(data, 0, next, null);
            next = top = 0;
        }
        // capa exhausted?
        else if (next == data.length) {
            // less than half of the capa is used
            if (top > data.length / 2) {
                System.arraycopy(data, top, data, 0, next - top);
                Arrays.fill(data, next - top, next, null);
                next = next - top;
                top = 0;
            }
            // or else grow
            else {
                @SuppressWarnings("unchecked")
                var tmp = (T[]) Array.newInstance(Object.class, data.length * 2);
                System.arraycopy(data, top, tmp, 0, next - top);
                data = tmp;
                next = next - top;
                top = 0;
            }
        }
    }

    public boolean isEmpty() {
        return next == top;
    }

    /**
     * Add an element at the tail.
     *
     * @param item an element, must not be {@code null}
     * @return this
     */
    public BaseQueue<T> add(T item) {
        checkSize();
        data[next++] = requireNonNull(item);
        return this;
    }

    public BaseQueue<T> addIfTail(T item, Predicate<? super T> condition) {
        return condition.test(tail()) ? add(item) : this;
    }

    public BaseQueue<T> addIfQueue(T item, Predicate<? super BaseQueue<T>> condition) {
        return condition.test(this) ? add(item) : this;
    }

    public BaseQueue<T> addIfNotEmpty(T item) {
        return addIfQueue(item, not(BaseQueue::isEmpty));
    }

    /**
     * Removes and returns the element from the head of the queue.
     *
     * @return the element at the head of the queue
     * @throws NoSuchElementException when empty.
     */
    public T remove() {
        if (top == next) {
            throw new NoSuchElementException("queue is empty");
        } else {
            T tmp = data[top];
            data[top++] = null;
            checkSize();
            return tmp;
        }
    }

    public Optional<T> removeIfNotEmpty() {
        return removeIfHead(Objects::nonNull);
    }

    public Optional<T> removeIfHead(Predicate<? super T> condition) {
        if(condition.test(head())) {
            return Optional.of(remove());
        } else {
            return Optional.empty();
        }
    }

    public Optional<T> removeIfQueue(Predicate<? super BaseQueue<T>> condition) {
        if(condition.test(this)) {
            return Optional.of(remove());
        } else {
            return Optional.empty();
        }
    }
    /**
     * The next element available in the queue.
     */
    public T head() {
        assert top < data.length;
        return top == next ? null : data[top];
    }

    /**
     * The last element added to the queue.
     */
    public T tail() {
        assert next <= data.length;
        return top == next ? null : data[next - 1];
    }
}
