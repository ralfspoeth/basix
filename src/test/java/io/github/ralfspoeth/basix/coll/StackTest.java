package io.github.ralfspoeth.basix.coll;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class StackTest {

    @Test
    void testEmpty() {
        var s = new Stack<@NonNull Integer>();
        assertTrue(s.isEmpty());
    }

    @Test
    void testSingle() {
        var emptyStack = new Stack<@NonNull Integer>();
        //noinspection DataFlowIssue
        assertAll(
                () -> assertFalse(new Stack<@NonNull Integer>().push(1).isEmpty()),
                () -> assertThrows(NullPointerException.class, () -> new Stack<>().push(null)),
                () -> assertEquals(1, new Stack<@NonNull Integer>().push(1).top()),
                () -> assertEquals(1, new Stack<@NonNull Integer>().push(1).pop()),
                () -> assertThrows(NoSuchElementException.class, emptyStack::pop)
        );
    }

    @Test
    void testPushIfEmpty() {
        // given: a fresh stack
        var s = new Stack<@NonNull Integer>();
        assertAll(
                // then is empty
                () -> assertTrue(s.isEmpty()),
                // then pushIfEmpty succeeds
                () -> assertFalse(s.pushIfEmpty(1).isEmpty()),
                // then top equals the last element pushed-if-empty
                () -> assertEquals(1, s.top()),
                // then the next pushIfEmpty does nothing
                () -> assertEquals(1, s.pushIfEmpty(2).top()),
                // then prior element is still the only one pushed
                () -> assertEquals(1, s.pop()),
                // then it's empty again and pushIfEmpty succeeds
                () -> assertEquals(2, s.pushIfEmpty(2).top())
        );
    }

    @Test
    void testPushIf() {
        // given: an empty stack
        var s = new Stack<@NonNull Integer>();
        assertAll(
                // then: empty at first
                () -> assertTrue(s.isEmpty()),
                // then: push if top element is not null has no effect; stack is still empty
                () -> assertTrue(s.pushIf(1, Objects::nonNull).isEmpty()),
                // then: push if top element is null succeeds leaving a non-empty stack
                () -> assertFalse(s.pushIf(1, Objects::isNull).isEmpty()),
                // then: that element pushed is the top element
                () -> assertEquals(1, s.top())
        );
    }

    @Test
    void testPopIfNotEmpty() {
        // given: a fresh stack
        var s = new Stack<@NonNull Integer>();
        assertAll(
                // then it is empty
                () -> assertTrue(s.isEmpty()),
                // then popIfNotEmpty returns an empty optional
                () -> assertFalse(s.popIfNotEmpty().isPresent()),
                // then after pushing an element, that is popped by popIfNotEmpty
                () -> assertEquals(1, s.push(1).popIfNotEmpty().orElseThrow())
        );
    }


    @Test
    void testRange() {
        // given
        var stack = new Stack<@NonNull Integer>();
        IntStream.range(0, 10).forEach(stack::push);
        // when
        var al = new ArrayList<Integer>();
        IntStream.iterate(9, i -> i - 1).limit(10).forEach(al::add);
        // then
        var result = new ArrayList<Integer>();
        while (!stack.isEmpty()) {
            result.add(stack.pop());
        }
        assertEquals(al, result);
    }
}