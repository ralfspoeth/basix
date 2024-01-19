package io.github.ralfspoeth.basix.coll;

import static java.util.Objects.requireNonNull;

public final class Stack<T> {

    private Elem<T> top = null;

    public boolean isEmpty() {
        return top == null;
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
