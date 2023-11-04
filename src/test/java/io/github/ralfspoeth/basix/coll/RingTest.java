package io.github.ralfspoeth.basix.coll;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RingTest {

    @Test
    void isEmpty() {
        assertTrue(new Ring<Integer>().isEmpty());
    }

    @Test
    void testSingle() {
        var rng = new Ring<Integer>();
        assertAll(
                () -> assertFalse(rng.insertAfterCurrent(1).isEmpty()),
                () -> assertEquals(1, rng.current()),
                () -> assertEquals(rng, rng.moveNext()),
                () -> assertEquals(rng, rng.movePrevious()),
                () -> assertEquals(1, rng.removeAndNext()),
                () -> assertTrue(rng.isEmpty())
        );
    }

    @Test
    void testDualInsertAfter() {
        var rng = new Ring<Integer>();
        rng.insertAfterCurrent(1).insertAfterCurrent(2);
        assertAll(
                () -> assertFalse(rng.isEmpty()),
                () -> assertEquals(1, rng.current()),
                () -> assertEquals(2, rng.moveNext().current()),
                () -> assertEquals(1, rng.moveNext().current()),
                () -> assertEquals(2, rng.moveNext().current()),
                () -> assertEquals(1, rng.movePrevious().current()),
                () -> assertEquals(2, rng.movePrevious().current()),
                () -> assertEquals(1, rng.movePrevious().current())
        );
    }

    @Test
    void testDualInsertBefore() {
        var rng = new Ring<Integer>();
        rng.insertBeforeCurrent(1).insertBeforeCurrent(2);
        assertAll(
                () -> assertFalse(rng.isEmpty()),
                () -> assertEquals(1, rng.current()),
                () -> assertEquals(2, rng.moveNext().current()),
                () -> assertEquals(1, rng.moveNext().current()),
                () -> assertEquals(2, rng.moveNext().current()),
                () -> assertEquals(1, rng.movePrevious().current()),
                () -> assertEquals(2, rng.movePrevious().current()),
                () -> assertEquals(1, rng.movePrevious().current())
        );
    }

    @Test
    void testDualInsert() {
        var rng = new Ring<Integer>();
        rng.insertBeforeCurrent(1).insertAfterCurrent(2);
        assertAll(
                () -> assertFalse(rng.isEmpty()),
                () -> assertEquals(1, rng.current()),
                () -> assertEquals(2, rng.moveNext().current()),
                () -> assertEquals(1, rng.moveNext().current()),
                () -> assertEquals(2, rng.moveNext().current()),
                () -> assertEquals(1, rng.movePrevious().current()),
                () -> assertEquals(2, rng.movePrevious().current()),
                () -> assertEquals(1, rng.movePrevious().current())
        );
    }

    @Test
    void testCircle() {
        var rng = new Ring<Integer>();
        rng.insertBeforeCurrent(-1); // center piece
        rng.insertAfterCurrent(1);
        assert rng.current()==-1;
        rng.insertBeforeCurrent(2);
        assert rng.current()==-1;
        // 2 <> -1 <> 1
        assertAll(
                () -> assertEquals(1, rng.moveNext().current()),
                () -> assertEquals(2, rng.moveNext().current()),
                () -> assertEquals(-1, rng.moveNext().current()),
                () -> assertEquals(2, rng.movePrevious().current()),
                () -> assertEquals(1, rng.movePrevious().current()),
                () -> assertEquals(-1, rng.movePrevious().current())
        );

    }


}