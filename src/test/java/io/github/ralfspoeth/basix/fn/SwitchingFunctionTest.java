package io.github.ralfspoeth.basix.fn;

import io.github.ralfspoeth.basix.fn.SwitchingFunction.Case;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

class SwitchingFunctionTest {

    @Test
    void testFirstMatchingCaseWins() {
        // given: overlapping cases; order matters
        var f = SwitchingFunction.<Integer, String>of(
                _ -> "default",
                Case.of(i -> i > 0, _ -> "positive"),
                Case.of(i -> i > 10, _ -> "large")
        );
        // then
        assertAll(
                () -> assertEquals("positive", f.apply(5)),
                () -> assertEquals("positive", f.apply(42)), // first match, not "large"
                () -> assertEquals("default", f.apply(-1))
        );
    }

    @Test
    void testDefaultWhenNoCaseMatches() {
        var f = SwitchingFunction.<Integer, String>of(
                Object::toString,
                Case.of(_ -> false, _ -> "never")
        );
        assertEquals("7", f.apply(7));
    }

    @Test
    void testNoCases() {
        var f = SwitchingFunction.<Integer, Integer>of(i -> i * 2);
        assertEquals(6, f.apply(3));
    }

    @Test
    void testListConstructorCopiesCases() {
        // given: a mutable list, cleared after construction
        var cases = new java.util.ArrayList<Case<Integer, String>>();
        cases.add(new Case<>(i -> i == 1, _ -> "one"));
        var f = new SwitchingFunction<>(cases, _ -> "default");
        // when
        cases.clear();
        // then: the function is unaffected
        assertEquals("one", f.apply(1));
    }

    @Test
    void testContravariantCaseOf() {
        // given: predicate and function over Number applied to Integer
        Predicate<Number> even = n -> n.intValue() % 2 == 0;
        Function<Number, String> name = n -> "even:" + n.intValue();
        Case<Integer, String> c = Case.of(even, name);
        var f = SwitchingFunction.of(_ -> "odd", c);
        // then
        assertAll(
                () -> assertEquals("even:4", f.apply(4)),
                () -> assertEquals("odd", f.apply(3))
        );
    }

    @Test
    void testNulls() {
        assertAll(
                () -> assertThrows(NullPointerException.class, () -> new Case<Integer, String>(null, _ -> "")),
                () -> assertThrows(NullPointerException.class, () -> new Case<Integer, String>(_ -> true, null)),
                () -> assertThrows(NullPointerException.class, () -> new SwitchingFunction<Integer, String>(null, _ -> "")),
                () -> assertThrows(NullPointerException.class, () -> new SwitchingFunction<Integer, String>(List.of(), null)),
                () -> assertThrows(NullPointerException.class, () -> SwitchingFunction.of(_ -> "", (Case<Integer, String>) null))
        );
    }

    @Test
    void testComposesAsFunction() {
        var f = SwitchingFunction.<Integer, Integer>of(
                _ -> 0,
                Case.of(i -> i < 0, _ -> -1),
                Case.of(i -> i > 0, _ -> 1)
        ).andThen(i -> i * 100);
        assertAll(
                () -> assertEquals(-100, f.apply(-5)),
                () -> assertEquals(100, f.apply(5)),
                () -> assertEquals(0, f.apply(0))
        );
    }
}
