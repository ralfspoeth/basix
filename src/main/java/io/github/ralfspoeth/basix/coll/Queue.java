package io.github.ralfspoeth.basix.coll;

import java.util.Objects;

public final class Queue<T> implements Coll {
    private Elem<T> last = null;  // last element added
    private Elem<T> first = null; // first to be removed

    @Override
    public boolean isEmpty() {
        return last == null;
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
        var tmp = Objects.requireNonNull(first).item;
        first = first.previous;
        first.next = null;
        if(first==null) {
            last = null;
        }
        return tmp;
    }

    public T head() {
        return first==null?null:first.item;
    }

    public T tail() {
        return last==null?null:last.item;
    }
}
