package io.github.ralfspoeth.basix.coll;

import static java.util.Objects.requireNonNull;

public final class Ring<T> implements Coll {

    private Elem<T> current;

    @Override
    public boolean isEmpty() {
        return current == null;
    }

    public Ring<T> insertAfterCurrent(T item) {
        var tmp = new Elem<>(item);
        if(current == null) {
            current = tmp;
            current.next = current;
            current.previous = current;
        } else {
            tmp.next = current.next;
            tmp.previous = current;
            current.next = tmp;
            if(current.previous==current) {
                current.previous = tmp;
            }
        }
        return this;
    }

    public Ring<T> insertBeforeCurrent(T item) {
        var tmp = new Elem<>(item);
        if(current == null) {
            current = tmp;
            current.next = current;
            current.previous = current;
        } else {
            tmp.next = current;
            tmp.previous = current.previous;
            current.previous = tmp;
            if(current.next == current) {
                current.next = tmp;
            }
        }
        return this;
    }

    public Ring<T> moveNext() {
        current = requireNonNull(current).next;
        return this;
    }

    public Ring<T> movePrevious() {
        current = requireNonNull(current).previous;
        return this;
    }

    public T current(){
        return requireNonNull(current).item;
    }

    public T removeAndNext() {
        final T tmp = requireNonNull(current).item;
        if(current.next==current.previous) {
            current = null;
        } else {
            var ptr = current.previous;
            current = current.next;
            current.previous = ptr;
        }
        return tmp;
    }

}
