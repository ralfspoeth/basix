package io.github.ralfspoeth.basix.coll;

import java.util.Iterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Objects.requireNonNull;

public final class Stack<T> implements Iterable<T> {

    private Elem<T> top = null;

    public boolean isEmpty() {
        return top == null;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            Elem<T> current = top;
            @Override
            public boolean hasNext() {
                return current!=null;
            }

            @Override
            public T next() {
                var tmp = current.item;
                current = current.next;
                return tmp;
            }
        };
    }

    public Stream<T> stream() {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(this.iterator(), 0), false
        );
    }

    public T pop() {
        var tmp = requireNonNull(top).item;
        top = top.next;
        return tmp;
    }

    public T top() {
        return top == null ? null : top.item;
    }

    public Stack<T> push(T elem) {
        var tmp = new Elem<>(elem);
        tmp.next = top;
        top = tmp;
        return this;
    }
    private static class Elem<T> {
        final T item;
        Elem<T> next;

        private Elem(T newItem) {
            this.item = requireNonNull(newItem);
        }
    }
}
