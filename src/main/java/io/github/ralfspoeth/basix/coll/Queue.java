package io.github.ralfspoeth.basix.coll;

import static java.util.Objects.requireNonNull;

/**
 * First in first out (FIFO) data structure.
 * Elements are appended at the end of and removed from the head of the queue.
 * {@snippet :
 * var queue = new Queue<Integer>();
 * assert queue.isEmpty();
 * queue.add(1);
 * assert !queue.isEmpty();
 * assert queue.head().equals(queue.tail);
 * assert queue.head()==1;
 * queue.add(2);
 * assert queue.head()==1;
 * assert queue.tail()==2;
 * assert 1==queue.remove();
 * assert 2==queue.remove();
 * assert queue.isEmpty();
 * }
 * @param <T> the element type.
 */
public final class Queue<T> {
    private Elem<T> last = null;  // last element added
    private Elem<T> first = null; // first to be removed

    public boolean isEmpty() {
        return last == null;
    }

    /**
     * Add an element at the tail.
     * @param item an element, must not be {@code null}
     * @return this
     */
    public Queue<T> add(T item) {
        var tmp = new Elem<>(item);
        if(last == null) {
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
        if(first==null) {
            last = null;
        } else {
            first.next = null;
        }
        return tmp;
    }

    public T head() {
        return first==null?null:first.item;
    }

    public T tail() {
        return last==null?null:last.item;
    }

    private static class Elem<T> {
        final T item;
        Elem<T> next;
        Elem<T> previous;

        private Elem(T newItem) {
            this.item = requireNonNull(newItem);
        }
    }
}
