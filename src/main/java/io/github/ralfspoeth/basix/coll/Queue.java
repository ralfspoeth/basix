package io.github.ralfspoeth.basix.coll;

import java.util.function.Predicate;

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
 *}
 *
 * @param <T> the element type.
 */
public final class Queue<T> extends BaseQueue<T> {

    @Override
    public Queue<T> add(T item) {
        return (Queue<T>)super.add(item);
    }

    @Override
    public Queue<T> addIfNotEmpty(T item) {
        return (Queue<T>)super.addIfNotEmpty(item);
    }

    @Override
    public Queue<T> addIfQueue(T item, Predicate<? super BaseQueue<T>> condition) {
        return(Queue<T>)super.addIfQueue(item, condition);
    }

    @Override
    public Queue<T> addIfTail(T item, Predicate<? super T> condition) {
        return (Queue<T>)super.addIfTail(item, condition);
    }
}
