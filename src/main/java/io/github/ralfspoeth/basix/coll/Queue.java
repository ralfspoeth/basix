package io.github.ralfspoeth.basix.coll;

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
public final class Queue<T> extends BaseQueue<T> {

    @Override
    public Queue<T> add(T item) {
        return (Queue<T>)super.add(item);
    }
}
