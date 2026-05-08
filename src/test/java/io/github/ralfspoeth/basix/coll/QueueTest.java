package io.github.ralfspoeth.basix.coll;

import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@NullMarked
class QueueTest {

    @Test
    void testNullElems() {
        var q = new Queue<Integer>();
        //noinspection DataFlowIssue
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
                () -> assertTrue(q.isEmpty()),
                () -> assertFalse(q.add(1).isEmpty()),
                () -> assertEquals(q.head(), q.tail()),
                () -> assertEquals(1, q.head().orElseThrow()),
                () -> assertEquals(1, q.remove()),
                () -> assertThrows(NoSuchElementException.class, q::remove)
        );
    }

    @Test
    void testTwo() {
        var q = new Queue<Integer>();
        assertAll(
                () -> assertTrue(q.isEmpty()),
                () -> assertFalse(q.add(1).isEmpty()),
                () -> assertFalse(q.add(2).isEmpty()),
                () -> assertEquals(1, q.head().orElseThrow()),
                () -> assertEquals(2, q.tail().orElseThrow()),
                () -> assertEquals(1, q.remove()),
                () -> assertEquals(2, q.remove()),
                () -> assertTrue(q.isEmpty()),
                () -> assertThrows(RuntimeException.class, q::remove)
        );
    }

    @Test
    void testRange() {
        var q = new Queue<Integer>();
        IntStream.range(0, 10).forEach(q::add);
        var al = new ArrayList<Integer>();
        while (!q.isEmpty()) {
            al.add(q.remove());
        }
        assertEquals(IntStream.range(0, 10).boxed().toList(), al.stream().toList());
    }

    @Test
    void testCheckRange() {
        var q = new Queue<Integer>();
        IntStream.range(0, 16).forEach(q::add);
        assertAll(
                () -> assertEquals(0, q.head().orElseThrow()),
                () -> assertEquals(15, q.tail().orElseThrow()),
                () -> assertEquals(0, q.remove()),
                () -> assertEquals(1, q.remove()),
                () -> assertEquals(2, q.remove()),
                () -> assertEquals(3, q.remove()),
                () -> assertEquals(4, q.remove()),
                () -> assertEquals(5, q.remove()),
                () -> assertEquals(6, q.remove()),
                () -> assertEquals(7, q.remove()),
                () -> assertNotNull(q.add(16)),
                () -> assertEquals(16, q.tail().orElseThrow())
        );
    }

    @Test
    void testRemoveAfterWrapFillsCompletely() {
        // Regression test: when add()s wrap around the internal array
        // and refill it back up to `top`, the queue is in the full-wrap
        // state (top == next > 0). A subsequent remove() must succeed
        // and return the elements in FIFO order, not throw
        // NoSuchElementException.
        var q = new Queue<Integer>();
        q.add(1).add(2).add(3).add(4);     // top=0, next=4
        assertEquals(1, q.remove());       // top=1, next=4
        q.add(5);                          // top=1, next=1, data=[5,2,3,4]
        // queue now logically holds [2, 3, 4, 5] in FIFO order
        assertEquals(2, q.remove());
        assertEquals(3, q.remove());
        assertEquals(4, q.remove());
        assertEquals(5, q.remove());
        assertTrue(q.isEmpty());
        assertThrows(NoSuchElementException.class, q::remove);
    }

    @Test
    void testReorg() {
        // given
        var q = new Queue<Integer>();
        // insert four, remove one, add one
        q.add(1).add(2).add(3).add(4);
        var one = q.remove();
        q.add(5);
        assertAll(
                () -> assertEquals(1, one),
                () -> assertEquals(5, q.tail().orElseThrow()),
                () -> assertEquals(2, q.head().orElseThrow())
        );
    }

}