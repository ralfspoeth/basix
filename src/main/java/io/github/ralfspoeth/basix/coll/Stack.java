package io.github.ralfspoeth.basix.coll;

import static java.util.Objects.requireNonNull;

public final class Stack<T> implements Coll {

    private Elem<T> top = null;

    @Override
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

}
