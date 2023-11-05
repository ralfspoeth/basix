package io.github.ralfspoeth.basix.fn;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FunctionsTest {

    @Test
    void testOfMapAndExtrFunc() {
        var m = Map.of(1, "one", 2, "two");
        record Int(int x) {
        }
        var x = new Int(1);
        var y = new Int(3);
        var f = Functions.of(m, Int::x);
        assertAll(
                () -> assertEquals("one", f.apply(x)),
                () -> assertNull(f.apply(y))
        );
    }

    @Test
    void testOfInt() {
        var l = List.of("one", "two", "three");
        var f = Functions.of(l);
        assertAll(
                () -> assertEquals("three", f.apply(2)),
                () -> assertThrows(Exception.class, ()->f.apply(-1)),
                () -> assertThrows(Exception.class, ()->f.apply(3))
        );
    }

    @Test
    void filterAndCast() {
        var values = List.of(1, 2d, true, false, "string", 'c');
        var numList = fnc(values, Number.class);
        var charList = fnc(values, Character.class);
        assertAll(
                () -> assertEquals(2, numList.size()),
                () -> assertTrue(numList.contains(1)),
                () -> assertTrue(numList.contains(2d)),
                () -> assertEquals(1, charList.size()),
                () -> assertTrue(charList.contains('c'))
        );
    }

    private static <T> List<T> fnc(List<?> l, Class<T> c) {
        return l.stream().flatMap(Functions.filterAndCast(c)).toList();
    }
}