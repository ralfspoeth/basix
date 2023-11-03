package io.github.ralfspoeth.basix.coll;

public final class Ring<T> implements Coll {

    private Elem<T> current;

    @Override
    public boolean isEmpty() {
        return current == null;
    }

    public Ring<T> insertNext(T item) {
        var tmp = new Elem<>(item);
        if(current == null) {
            current = tmp;
            current.next = current;
            current.previous = current;
        } else {
            tmp.next = current.next;
            tmp.previous = current;
            current.next = tmp;
        }
        return this;
    }
}
