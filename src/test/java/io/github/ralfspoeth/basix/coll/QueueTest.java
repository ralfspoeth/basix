package io.github.ralfspoeth.basix.coll;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class QueueTest {

    @Test
    void testNullElems() {
        var q = new Queue<Integer>();
        assertThrows(NullPointerException.class, () -> q.add(null));
    }

    @Test
    void testEmpty() {
        assertTrue(new Queue<>().isEmpty());
    }

    @Test
    void testSingle() {
        var q = new Queue<Integer>();
        assertAll(
                () -> assertFalse(q.add(1).isEmpty()),
                () -> assertEquals(q.head(), q.tail()),
                () -> assertEquals(1, q.head()),
                () -> assertEquals(1, q.remove()),
                () -> assertThrows(NullPointerException.class, q::remove)
        );
    }

    @Test
    void testTwo() {
        var q = new Queue<Integer>();
        assertAll(
                () -> assertFalse(q.add(1).isEmpty()),
                () -> assertFalse(q.add(2).isEmpty()),
                () -> assertEquals(1, q.head()),
                () -> assertEquals(2, q.tail()),
                () -> assertEquals(1, q.remove()),
                () -> assertEquals(2, q.remove()),
                () -> assertTrue(q.isEmpty()),
                () -> assertThrows(NullPointerException.class, q::remove)
        );
    }


    @Test
    void testIterable() {
        var queue = new Queue<Integer>();
        queue.add(1).add(2).add(3);
        var j = 0;
        // 1, 2, 3 in reverse order
        for(var i: queue) {
            assertEquals(++j, i);
        }
        // should be repeatable
        j = 0;
        for(var i: queue) {
            assertEquals(++j, i);
        }
        // queue is unchanged
        assertAll(
                () -> assertFalse(queue.isEmpty()),
                () -> assertEquals(1, queue.remove()),
                () -> assertEquals(2, queue.remove()),
                () -> assertEquals(3, queue.remove()),
                () -> assertTrue(queue.isEmpty())
        );
    }
    @Test
    void testRange() {
        var q = new Queue<Integer>();
        IntStream.range(0, 10).forEach(q::add);
        var al = new ArrayList<Integer>();
        while(!q.isEmpty()) {
            al.add(q.remove());
        }
        assertEquals(IntStream.range(0, 10).boxed().toList(), al.stream().toList());
    }

    @Test
    void testStream() {
        var q = new Queue<Integer>();
        IntStream.rangeClosed(1, 10).forEach(q::add);
        assertAll(
                () -> assertEquals(10L, q.stream().count()),
                () -> assertEquals(55, q.stream().reduce(Integer::sum).get()),
                () -> assertEquals(1, q.stream().findFirst().get())
        );

    }
}