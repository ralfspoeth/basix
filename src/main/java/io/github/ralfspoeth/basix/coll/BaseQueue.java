package io.github.ralfspoeth.basix.coll;

import org.jspecify.annotations.Nullable;

import java.lang.reflect.Array;

import static java.util.Objects.requireNonNull;

sealed class BaseQueue<T> permits Queue, ConcurrentQueue {

    @SuppressWarnings("unchecked")
    private T[] data = (T[]) Array.newInstance(Object.class, 16);
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

    public boolean isEmpty() {
        return last == null;
    }

    /**
     * Add an element at the tail.
     *
     * @param item an element, must not be {@code null}
     * @return this
     */
    public BaseQueue<T> add(T item) {
        Elem<T> tmp = new Elem<>(item);
        if (last == null) {
            assert first == null;
            last = tmp;
            first = tmp;
        } else {
            tmp.next = last;
            last.previous = tmp;
            last = tmp;
        }
        return this;
    }

    /**
     * Removes and returns the element from the head of the queue.
     *
     * @return the element at the head of the queue
     * @throws NullPointerException when empty.
     */
    public T remove() {
        var tmp = requireNonNull(first).item;
        first = first.previous;
        if (first == null) {
            last = null;
        } else {
            first.next = null;
        }
        return tmp;
    }

    public @Nullable T head() {
        return first == null ? null : first.item;
    }

    public @Nullable T tail() {
        return last == null ? null : last.item;
    }

    private static class Elem<T> {
        final T item;
        @Nullable Elem<T> next;
        @Nullable Elem<T> previous;

        private Elem(T newItem) {
            this.item = requireNonNull(newItem);
        }
    }
}
