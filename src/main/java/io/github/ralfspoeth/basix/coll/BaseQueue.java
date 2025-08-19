package io.github.ralfspoeth.basix.coll;

import org.jspecify.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.NoSuchElementException;

import static java.util.Objects.requireNonNull;

sealed abstract class BaseQueue<T> permits Queue, ConcurrentQueue {

    @SuppressWarnings("unchecked")
    private @Nullable T[] data = (T[]) Array.newInstance(Object.class, 16);
    private int next = 0;
    private int top = 0;

    private void checkSize() {
        assert top <= next;
        // next == top -> empty
        // we can move both pointers back to the start
        if(top==next) {
            next = top = 0;
        }
        // capa exhausted?
        else if(next==data.length) {
            // less than half of the capa is used
            if(top>data.length/2) {
                System.arraycopy(data, top, data, 0, next-top);
                next = next-top;
                top = 0;
            }
            // or else grow
            else {
                @SuppressWarnings("unchecked")
                var tmp = (T[])Array.newInstance(Object.class, data.length * 2);
                System.arraycopy(data, top, tmp, 0, next - top);
                data = tmp;
                next = next - top;
                top = 0;
            }
        }
    }

    protected BaseQueue() {
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

    /**
     * Removes and returns the element from the head of the queue.
     *
     * @return the element at the head of the queue
     * @throws NoSuchElementException when empty.
     */
    public T remove() {
        if(top==next) {
            throw new NoSuchElementException("queue is empty");
        } else {
            T tmp = data[top++];
            checkSize();
            assert tmp != null;
            return tmp;
        }
    }

    /**
     * The next element available in the queue.
     */
    public @Nullable T head() {
        return top==next ? null : data[top];
    }

    /**
     * The last element added to the queue.
     */
    public @Nullable T tail() {
        return top==next ? null : data[next-1];
    }

}
