package io.github.ralfspoeth.basix.coll;

import java.util.Iterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Objects.requireNonNull;

public final class Queue<T> implements Iterable<T> {
    private Elem<T> last = null;  // last element added
    private Elem<T> first = null; // first to be removed

    public boolean isEmpty() {
        return last == null;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            Elem<T> current = first;
            @Override
            public boolean hasNext() {
                return current!=null;
            }

            @Override
            public T next() {
                var tmp = current.item;
                current = current.previous;
                return tmp;
            }
        };
    }

    public Stream<T> stream() {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(this.iterator(), 0), false
        );
    }

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
