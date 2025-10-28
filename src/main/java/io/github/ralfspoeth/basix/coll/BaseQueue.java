package io.github.ralfspoeth.basix.coll;

import org.jspecify.annotations.Nullable;
import java.util.NoSuchElementException;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

sealed abstract class BaseQueue<S extends BaseQueue<S, T>, T> permits Queue, ConcurrentQueue {

    @SuppressWarnings("unchecked")
    private @Nullable T[] data = (T[]) new Object[4];
    private int next = 0; // next available slot
    private int top = 0; // next slot to be removed

    private void growIfExhausted() {
        // next insertion point out of bounds?
        if (next == data.length) {
            // move next to the beginning of the queue,
            if(top == 0) {
                var tmp = (T[]) new Object[data.length * 2];
                System.arraycopy(data, 0, tmp, 0, data.length);
                data = tmp;
            } else {
                next = 0;
            }
        }
        // insertion point overtakes top
        else if(next == top && top > 0) {
            var tmp = (T[]) new Object[data.length * 2];
            System.arraycopy(data, top, tmp, 0, data.length - top);
            System.arraycopy(data, 0, tmp, data.length - top, top);
            top = 0;
            next = data.length;
            data = tmp;
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
    @SuppressWarnings("unchecked")
    public S add(T item) {
        growIfExhausted();
        data[next++] = requireNonNull(item);
        return (S) this;
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
            data[top++] = null; // prevent memory leak
            // next == top -> empty
            // we can move both pointers back to the start
            if (top == next) {
                next = top = 0;
            }
            assert tmp != null;
            return tmp;
        }
    }

    /**
     * The next element available in the queue.
     */
    public Optional<T> head() {
        return top == next ? Optional.empty() : Optional.of(data[top]);
    }

    /**
     * The last element added to the queue.
     */
    public Optional<T> tail() {
        return top == next ? Optional.empty() : Optional.of(data[next==0?data.length-1:next - 1]);
    }

}
