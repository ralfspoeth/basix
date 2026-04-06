package io.github.ralfspoeth.basix.coll;

import org.jspecify.annotations.Nullable;

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
public class Queue<T> implements FiFo<Queue<T>, T> {

    @SuppressWarnings("unchecked")
    private @Nullable T[] data = (T[]) new Object[4];
    private int next = 0; // next available slot
    private int top = 0; // next slot to be removed

    private void growIfExhausted() {
        // next insertion point out of bounds?
        if (next == data.length) {
            // move next to the beginning of the queue,
            if (top == 0) {
                @SuppressWarnings("unchecked")
                var tmp = (T[]) new Object[data.length * 2];
                System.arraycopy(data, 0, tmp, 0, data.length);
                data = tmp;
            } else {
                next = 0;
            }
        }
        // next at top but not empty
        else if (next == top && next > 0) {
            @SuppressWarnings("unchecked")
            var tmp = (T[]) new Object[data.length * 2];
            System.arraycopy(data, top, tmp, 0, data.length - top);
            System.arraycopy(data, 0, tmp, data.length - top, top);
            top = 0;
            next = data.length;
            data = tmp;
        }
    }

    @Override
    public boolean isEmpty() {
        return next == top && next == 0;
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
        if (top == next) {
            throw new NoSuchElementException("queue is empty");
        } else {
            T tmp = data[top];
            data[top++] = null; // prevent memory leak
            // next == top -> empty
            // we can move both pointers back to the start
            if (top == next) {
                next = top = 0;
            } else if (top == data.length) {
                top = 0;
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
        return isEmpty() ? Optional.empty() : Optional.of(requireNonNull(data[top]));
    }

    /**
     * The last element added to the queue.
     */
    @Override
    public Optional<T> tail() {
        return isEmpty() ? Optional.empty() : Optional.of(requireNonNull(
                data[next == 0 ? data.length - 1 : next - 1]
        ));
    }

}
