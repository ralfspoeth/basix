package io.github.ralfspoeth.basix.fn;

import io.github.ralfspoeth.basix.fn.SwitchingFunction.Case;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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
    void testFizzBuzzStreamPipeline() {
        // given: no explicit type arguments; T and R are inferred
        // from the Stream<Integer> target type of map()
        var result = IntStream.rangeClosed(1, 15)
                .boxed()
                .map(SwitchingFunction.of(
                        String::valueOf,
                        Case.of(i -> i % 15 == 0, _ -> "FizzBuzz"),
                        Case.of(i -> i % 3 == 0, _ -> "Fizz"),
                        Case.of(i -> i % 5 == 0, _ -> "Buzz")
                ))
                .toList();
        // then
        assertEquals(
                List.of("1", "2", "Fizz", "4", "Buzz", "Fizz", "7", "8",
                        "Fizz", "Buzz", "11", "Fizz", "13", "14", "FizzBuzz"),
                result
        );
    }

    @Test
    void testStringClassificationStreamPipeline() {
        var result = Stream.of("a", "bb", "CCC", "")
                .map(SwitchingFunction.of(
                        _ -> "other",
                        Case.of(String::isEmpty, _ -> "empty"),
                        Case.of(s -> s.length() == 1, _ -> "single")
                ))
                .toList();
        assertEquals(List.of("single", "other", "other", "empty"), result);
    }

    @Test
    void testGroupingByClassifierInPipeline() {
        var groups = Stream.of(-2, -1, 0, 1, 2)
                .collect(Collectors.groupingBy(SwitchingFunction.of(
                        _ -> "zero",
                        Case.of(i -> i < 0, _ -> "negative"),
                        Case.of(i -> i > 0, _ -> "positive")
                )));
        assertEquals(
                Map.of(
                        "negative", List.of(-2, -1),
                        "zero", List.of(0),
                        "positive", List.of(1, 2)
                ),
                groups
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
