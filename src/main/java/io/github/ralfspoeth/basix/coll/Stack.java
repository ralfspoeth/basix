package io.github.ralfspoeth.basix.coll;

import static java.util.Objects.requireNonNull;

/**
 * List in, first out (LIFO) data structure.
 * A stack is empty after creation:
 * {@snippet :
 * var stack = new Stack<Integer>();
 * assert stack.isEmpty();
 * assert null==stack.top();
 * }
 * and after adding and removing a single element:
 * {@snippet :
 * var stack = new Stack<Integer>();
 * stack.push(1);
 * assert 1==stack.pop();
 * assert stack.isEmpty();
 * }
 * Elements are removed in LIFO order such that
 * {@snippet :
 * var stack = new Stack<Integer>();
 * stack.push(1).push(2);
 * assert 2==stack.top(); // topmost element without removing it
 * assert 2==stack.pop(); // topmost equals last added
 * assert 1==stack.pop(); // ... and now the added before
 * assert stack.isEmpty(); // leaving the stack empty in the end
 * }
 * @param <T> the element type
 */
public final class Stack<T> {

    private Elem<T> top = null;

    public boolean isEmpty() {
        return top == null;
    }

    /**
     * Return and remove the topmost element of the stack.
     *
     * @return the topmost element
     * @throws NullPointerException when empty
     */
    public T pop() {
        var tmp = requireNonNull(top).item;
        top = top.next;
        return tmp;
    }

    /**
     * The topmost element of the stack, or {@code null}
     * if empty.
     *
     * @return the topmost element
     */
    public T top() {
        return top == null ? null : top.item;
    }

    /**
     * Add an element at the top of the stack.
     * @param elem the element, may not be {@code null}
     * @return this
     */
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
