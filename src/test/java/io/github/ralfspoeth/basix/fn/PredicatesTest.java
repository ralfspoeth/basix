package io.github.ralfspoeth.basix.fn;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
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
                () -> assertEquals(1, Arrays.stream(new Object[]{null}).filter(in(s, _ -> 1)).count())
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
    void testEq() {
        var seven = "seven";
        record Named(String name, Object value) { }
        var namedList = List.of(new Named("seven", 7), new Named("eight", 8));
        var sevenList = namedList.stream().filter(eq(seven, Named::name)).toList();
        assertAll(
                () -> assertEquals(1, sevenList.size()),
                () -> assertEquals(seven, sevenList.getFirst().name),
                () -> assertEquals(7, sevenList.getFirst().value)
        );
    }

    private static final List<Integer> DATA = List.of(1, 2, 3, 4, 5);
    // reverse order: useful to confirm the comparator argument is actually honored
    private static final Comparator<Integer> REVERSE = Comparator.reverseOrder();

    @Test
    void testSmallerThan() {
        assertAll(
                () -> assertEquals(List.of(1, 2), DATA.stream().filter(smallerThan(3)).toList()),
                () -> assertEquals(List.of(), DATA.stream().filter(smallerThan(1)).toList()),
                // under reverse order, "smaller than 3" means ranked after 3, i.e. 4, 5
                () -> assertEquals(List.of(4, 5), DATA.stream().filter(smallerThan(3, REVERSE)).toList())
        );
    }

    @Test
    void testSmallerOrEqual() {
        assertAll(
                () -> assertEquals(List.of(1, 2, 3), DATA.stream().filter(smallerOrEqual(3)).toList()),
                () -> assertEquals(List.of(1), DATA.stream().filter(smallerOrEqual(1)).toList()),
                () -> assertEquals(List.of(3, 4, 5), DATA.stream().filter(smallerOrEqual(3, REVERSE)).toList())
        );
    }

    @Test
    void testEqual() {
        assertAll(
                () -> assertEquals(List.of(3), DATA.stream().filter(equal(3)).toList()),
                () -> assertEquals(List.of(), DATA.stream().filter(equal(9)).toList()),
                () -> assertEquals(List.of(3), DATA.stream().filter(equal(3, REVERSE)).toList())
        );
    }

    @Test
    void testNonEqual() {
        assertAll(
                () -> assertEquals(List.of(1, 2, 4, 5), DATA.stream().filter(nonEqual(3)).toList()),
                () -> assertEquals(DATA, DATA.stream().filter(nonEqual(9)).toList()),
                () -> assertEquals(List.of(1, 2, 4, 5), DATA.stream().filter(nonEqual(3, REVERSE)).toList())
        );
    }

    @Test
    void testGreaterOrEqual() {
        assertAll(
                () -> assertEquals(List.of(3, 4, 5), DATA.stream().filter(greaterOrEqual(3)).toList()),
                () -> assertEquals(List.of(5), DATA.stream().filter(greaterOrEqual(5)).toList()),
                () -> assertEquals(List.of(1, 2, 3), DATA.stream().filter(greaterOrEqual(3, REVERSE)).toList())
        );
    }

    @Test
    void testGreaterThan() {
        assertAll(
                () -> assertEquals(List.of(4, 5), DATA.stream().filter(greaterThan(3)).toList()),
                () -> assertEquals(List.of(), DATA.stream().filter(greaterThan(5)).toList()),
                () -> assertEquals(List.of(1, 2), DATA.stream().filter(greaterThan(3, REVERSE)).toList())
        );
    }

    @Test
    void testSmallerOrGreater() {
        assertAll(
                () -> assertEquals(List.of(1, 2, 4, 5), DATA.stream().filter(smallerOrGreater(3)).toList()),
                () -> assertEquals(DATA, DATA.stream().filter(smallerOrGreater(9)).toList()),
                () -> assertEquals(List.of(1, 2, 4, 5), DATA.stream().filter(smallerOrGreater(3, REVERSE)).toList())
        );
    }
}