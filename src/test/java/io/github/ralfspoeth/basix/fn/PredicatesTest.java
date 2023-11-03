package io.github.ralfspoeth.basix.fn;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static io.github.ralfspoeth.basix.fn.Predicates.*;

import static java.util.function.Function.identity;
import static java.util.function.Predicate.not;
import static org.junit.jupiter.api.Assertions.*;

class PredicatesTest {

    @Test
    void testIn() {
        var s = Set.of(1, 2, 3);
        record Int(int x) {
        }
        final var pMatch = new Int(2);
        final var pNoMatch = new Int(4);
        var l = List.of(3, 1, 2);
        assertAll(
                () -> assertEquals(1, Stream.of(pMatch).filter(in(s, Int::x)).count()),
                () -> assertEquals(0, Stream.of(pNoMatch).filter(in(s, Int::x)).count()),
                () -> assertTrue(l.stream().anyMatch(in(s, identity()))),
                () -> assertEquals(1, Arrays.stream(new Object[]{null}).filter(in(s, x -> 1)).count())
        );
    }

    @Test
    void testNotIn() {
        var s = Set.of(1, 2, 3);
        record Int(int x) {
        }
        var pNoMatch = new Int(2);
        var pMatch = new Int(4);
        var l = List.of(3, 1, 2);
        assertAll(
                () -> assertEquals(0, Stream.of(pNoMatch).filter(not(in(s, Int::x))).count()),
                () -> assertEquals(1, Stream.of(pMatch).filter(not(in(s, Int::x))).count()),
                () -> assertTrue(l.stream().noneMatch(not(in(s, identity()))))
        );
    }

    @Test
    void testKeyIn() {
        var m = Map.of(1, 1, 2, 2);
        record Int(int x) {
        }
        var pMatch = new Int(2);
        var pNoMatch = new Int(3);

        var l = List.of(1, 2);
        assertAll(
                () -> assertEquals(1, Stream.of(pMatch).filter(Predicates.in(m, Int::x)).count()),
                () -> assertEquals(0, Stream.of(pNoMatch).filter(Predicates.in(m, Int::x)).count()),
                () -> assertTrue(l.stream().allMatch(Predicates.in(m, identity())))
        );
    }
}