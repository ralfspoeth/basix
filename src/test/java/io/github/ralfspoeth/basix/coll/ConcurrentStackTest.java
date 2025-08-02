package io.github.ralfspoeth.basix.coll;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConcurrentStackTest {

    @Test
    void testPopIfNotEmpty() {
        // given
        var stack = new ConcurrentStack<Integer>();
        // when
        stack.push(1);
        // then
        assertAll(
                () -> assertFalse(stack::isEmpty),
                () -> assertEquals(1, stack.popIfNotEmpty().orElse(0)),
                () -> assertEquals(0, stack.popIfNotEmpty().orElse(0)),
                () -> assertTrue(stack::isEmpty)
        );
    }

    @Test
    void testPushIfEmpty() {
        // given
        var stack = new ConcurrentStack<Integer>();
        // then
        assertAll(
                () -> assertTrue(stack.isEmpty()),
                () -> assertFalse(stack.pushIfEmpty(1).isEmpty()),
                () -> assertFalse(stack.pushIfEmpty(2).isEmpty()),
                () -> assertFalse(stack.isEmpty()),
                () -> assertEquals(1, stack.top()),
                () -> {}
        );
    }

}
