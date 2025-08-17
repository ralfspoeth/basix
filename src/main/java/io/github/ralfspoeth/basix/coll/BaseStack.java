package io.github.ralfspoeth.basix.coll;

import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

/**
 * List in, first out (LIFO) data structure.
 * The package provides an non-concurrent {@link Stack}
 * and a concurrent {@link ConcurrentStack} variant of this baseclass,
 * which cannot be instantiated directly.
 * Most of the
 * A stack is empty after creation:
 * {@snippet :
 * Stack<Integer> stack = new Stack<>(); // new ConcurrentStack<>();
 * assert stack.isEmpty();
 * assert null==stack.top();
 *}
 * and after adding and removing a single element:
 * {@snippet :
 * Stack<Integer> stack = new Stack<>();
 * stack.push(1);
 * var one = stack.pop();
 * assert 1==one;
 * assert stack.isEmpty();
 *}
 * Elements are removed in LIFO order such that
 * {@snippet :
 * Stack<Integer> stack = new Stack<>();
 * stack.push(1).push(2);
 * assert 2==stack.top(); // topmost element without removing it
 * var two = stack.pop();
 * assert 2==two; // topmost equals last added
 * var one = stack.pop();
 * assert 1==one; // ... and now the added before
 * assert stack.isEmpty(); // leaving the stack empty in the end
 *}
 * The concurrent variant provides additional atomic compound operations
 * only; see {@link ConcurrentStack there}.
 *
 * @param <T> the element type
 */
abstract sealed class BaseStack<T> permits Stack, ConcurrentStack {
    private @Nullable Elem<T> top = null;

    protected BaseStack() {
    }

    /**
     * Pop the topmost element if it is not null.
     */
    public Optional<T> popIfNotEmpty() {
        return popIf(Objects::nonNull);
    }

    /**
     * Remove to the top element of this stack only if it meets
     * the given condition.
     *
     * @param condition a condition; note that the object passed to its
     *                  {@link Predicate#test(Object)} method may be {@code null}
     * @return the topmost element of the stack wrapped in an optional, or
     * {@link Optional#empty()}
     */
    public Optional<T> popIf(Predicate<? super @Nullable T> condition) {
        return condition.test(top()) ? Optional.of(pop()) : Optional.empty();
    }


    public BaseStack<T> pushIfEmpty(T data) {
        return pushUnless(data, Objects::nonNull);
    }

    /**
     * Push an element unless some condition is met.
     *
     * @param data the element to be pushed
     * @param condition the condition not be met if the element is to be pushed
     * @return this
     */
    public BaseStack<T> pushUnless(T data, Predicate<? super @Nullable T> condition) {
        return condition.test(top()) ? this : push(data);
    }


    /**
     * An empty stack contains no elements
     *
     * @return true if empty
     */
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
    public @Nullable T top() {
        return top == null ? null : top.item;
    }

    /**
     * Add an element at the top of the stack.
     *
     * @param elem the element, may not be {@code null}
     * @return this
     */
    public BaseStack<T> push(T elem) {
        var tmp = new Elem<>(elem);
        tmp.next = top;
        top = tmp;
        return this;
    }

    private static class Elem<T> {
        final T item;
        @Nullable Elem<T> next;

        private Elem(T newItem) {
            this.item = requireNonNull(newItem);
        }
    }
}
