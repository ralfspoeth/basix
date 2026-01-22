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
        var s = new Stack<@NonNull Integer>();
        assertAll(
                () -> assertTrue(s.isEmpty()),
                () -> assertFalse(s.pushIfEmpty(1).isEmpty()),
                () -> assertEquals(1, s.top()),
                () -> assertEquals(1, s.pushIfEmpty(2).top()),
                () -> assertEquals(1, s.pop()),
                () -> assertEquals(2, s.pushIfEmpty(2).top())
        );
    }

    @Test
    void testPushIf() {
        var s = new Stack<@NonNull Integer>();
        assertAll(
                () -> assertTrue(s.isEmpty()),
                () -> assertTrue(s.pushIf(1, Objects::nonNull).isEmpty()),
                () -> assertFalse(s.pushIf(1, Objects::isNull).isEmpty()),
                () -> assertEquals(1, s.top()),
                () -> assertEquals(1, s.top())
        );
    }

    @Test
    void testPopIfNotEmpty() {
        // given
        var s = new Stack<@NonNull Integer>();
        // when/then
        assertAll(
                () -> assertTrue(s.isEmpty()),
                () -> assertFalse(s.popIfNotEmpty().isPresent()),
                () -> assertTrue(s.push(1).popIfNotEmpty().isPresent())
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