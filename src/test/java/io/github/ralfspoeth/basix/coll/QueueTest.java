package io.github.ralfspoeth.basix.coll;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.NoSuchElementException;
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
                () -> assertTrue(q.isEmpty()),
                () -> assertFalse(q.add(1).isEmpty()),
                () -> assertEquals(q.head(), q.tail()),
                () -> assertEquals(1, q.head()),
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
                () -> assertEquals(1, q.head()),
                () -> assertEquals(2, q.tail()),
                () -> assertEquals(1, q.remove()),
                () -> assertEquals(2, q.remove()),
                () -> assertTrue(q.isEmpty()),
                () -> assertThrows(NoSuchElementException.class, q::remove)
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
}