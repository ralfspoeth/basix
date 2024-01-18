package io.github.ralfspoeth.basix.coll;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class StackTest {

    @Test
    void testEmpty() {
        assertTrue(new Stack<Integer>().isEmpty());
    }

    @Test
    void testSingle() {
        assertAll(
                () -> assertFalse(new Stack<Integer>().push(1).isEmpty()),
                () -> assertThrows(NullPointerException.class, () -> new Stack<>().push(null)),
                () -> assertEquals(1, new Stack<Integer>().push(1).top()),
                () -> assertEquals(1, new Stack<Integer>().push(1).pop()),
                () -> assertThrows(NullPointerException.class, () -> new Stack<Integer>().pop())
        );
    }

    @Test
    void testIterable() {
        var stack = new Stack<Integer>();
        stack.push(1).push(2).push(3);
        var j = 3;
        // 1, 2, 3 in reverse order
        for(var i: stack) {
            assertEquals(j--, i);
        }
        // should be repeatable
        j = 3;
        for(var i: stack) {
            assertEquals(j--, i);
        }
        // stack is unchanged
        assertAll(
                () -> assertFalse(stack.isEmpty()),
                () -> assertEquals(3, stack.pop()),
                () -> assertEquals(2, stack.pop()),
                () -> assertEquals(1, stack.pop()),
                () -> assertTrue(stack.isEmpty())
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

    @Test
    void testStream() {
        var s = new Stack<Integer>();
        IntStream.rangeClosed(1, 10).forEach(s::push);
        assertAll(
                () -> assertEquals(10L, s.stream().count()),
                () -> assertEquals(55, s.stream().reduce(Integer::sum).get()),
                () -> assertEquals(10, s.stream().findFirst().get())
        );
    }

}