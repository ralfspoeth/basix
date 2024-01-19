package io.github.ralfspoeth.basix.coll;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class StackTest {

    @Test
    void testEmpty() {
        var s = new Stack<Integer>();
        assertTrue(s.isEmpty());
    }

    @Test
    void testSingle() {
        var emptyStack = new Stack<Integer>();
        assertAll(
                () -> assertFalse(new Stack<Integer>().push(1).isEmpty()),
                () -> assertThrows(NullPointerException.class, () -> new Stack<>().push(null)),
                () -> assertEquals(1, new Stack<Integer>().push(1).top()),
                () -> assertEquals(1, new Stack<Integer>().push(1).pop()),
                () -> assertThrows(NullPointerException.class, emptyStack::pop)
        );
    }

    @Test
    void testRange() {
        var stack = new Stack<Integer>();
        IntStream.range(0, 10).forEach(stack::push);
        var al = new ArrayList<Integer>();
        while (!stack.isEmpty()) {
            al.add(stack.pop());
        }
        assertEquals(IntStream.range(0, 10).boxed().toList().reversed(), al.stream().toList());
    }
}