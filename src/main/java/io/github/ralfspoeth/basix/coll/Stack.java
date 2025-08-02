package io.github.ralfspoeth.basix.coll;

/**
 * Non-concurrent implementation of a stack.
 */
public final class Stack<T> extends BaseStack<T> {
    @Override
    public Stack<T> push(T elem) {
        return (Stack<T>) super.push(elem);
    }

    @Override
    public Stack<T> pushIfEmpty(T data) {
        return (Stack<T>) super.pushIfEmpty(data);
    }

    @Override
    public Stack<T> pushUnless(T data, java.util.function.Predicate<? super T> condition) {
        return (Stack<T>) super.pushUnless(data, condition);
    }
}
