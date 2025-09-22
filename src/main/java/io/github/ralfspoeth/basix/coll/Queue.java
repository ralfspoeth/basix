package io.github.ralfspoeth.basix.coll;

import org.jspecify.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Optional;

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
 * var one = queue.remove();
 * assert 1==one;
 * var two = queue.remove();
 * assert 2==two;
 * assert queue.isEmpty();
 *}
 *
 * @param <T> the element type.
 */

public final class Queue<T> implements FiFo<Queue<T>,T>  {

    @SuppressWarnings("unchecked")
    private @Nullable T[] data = (T[]) Array.newInstance(Object.class, 16);
    private int next = 0;
    private int top = 0;

    private void growIfExhausted() {
        assert top <= next;
        // capa exhausted?
        if(next==data.length) {
            // less than half of the capa is used
            if(top>=data.length/2) {
                System.arraycopy(data, top, data, 0, next-top);
                Arrays.fill(data, top, data.length, null);
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

    @Override
    public boolean isEmpty() {
        return next == top;
    }

    /**
     * Add an element at the tail.
     *
     * @param item an element, must not be {@code null}
     * @return this
     */
    @Override
    public Queue<T> add(T item) {
        growIfExhausted();
        data[next++] = requireNonNull(item);
        return this;
    }

    /**
     * Removes and returns the element from the head of the queue.
     *
     * @return the element at the head of the queue
     * @throws NoSuchElementException when empty.
     */
    @Override
    public T remove() {
        if(top==next) {
            throw new NoSuchElementException("queue is empty");
        } else {
            T tmp = data[top];
            data[top++] = null; // prevent memory leak
            // next == top -> empty
            // we can move both pointers back to the start
            if(top==next) {
                next = top = 0;
            }
            assert tmp != null;
            return tmp;
        }
    }

    /**
     * The next element available in the queue.
     */
    @Override
    public Optional<T> head() {
        return top==next ? Optional.empty() : Optional.of(data[top]);
    }

    /**
     * The last element added to the queue.
     */
    @Override
    public Optional<T> tail() {
        return top==next ? Optional.empty() : Optional.of(data[next-1]);
    }

}
