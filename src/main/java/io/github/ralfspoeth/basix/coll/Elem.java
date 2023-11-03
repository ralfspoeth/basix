package io.github.ralfspoeth.basix.coll;

import static java.util.Objects.requireNonNull;

class Elem<T> {
    final T item;
    Elem<T> next;
    Elem<T> previous;

    public Elem(T newItem) {
        this.item = requireNonNull(newItem);
    }
}
