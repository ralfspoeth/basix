package io.github.ralfspoeth.basix.fn;

import io.github.ralfspoeth.basix.fn.Switch.Case;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class SwitchTest {

    @Test
    void testFirstMatchingCaseWins() {
        // given: overlapping cases; order matters
        var f = Switch.<Integer, String>when(i -> i > 0).then(_ -> "positive")
                .when(i -> i > 10).then(_ -> "large")
                .otherwise(_ -> "default");
        // then
        assertAll(
                () -> assertEquals("positive", f.apply(5)),
                () -> assertEquals("positive", f.apply(42)), // first match, not "large"
                () -> assertEquals("default", f.apply(-1))
        );
    }

    @Test
    void testDefaultWhenNoCaseMatches() {
        var f = Switch.<Integer, String>when(_ -> false).then(_ -> "never")
                .otherwise(Object::toString);
        assertEquals("7", f.apply(7));
    }

    @Test
    void testNoCases() {
        var f = new Switch<Integer, Integer>(List.of(), i -> i * 2);
        assertEquals(6, f.apply(3));
    }

    @Test
    void testListConstructorCopiesCases() {
        // given: a mutable list, cleared after construction
        var cases = new java.util.ArrayList<Case<Integer, String>>();
        cases.add(new Case<>(i -> i == 1, _ -> "one"));
        var f = new Switch<>(cases, _ -> "default");
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
        var f = new Switch<>(List.of(c), (Integer _) -> "odd");
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
                () -> assertThrows(NullPointerException.class, () -> new Switch<Integer, String>(null, _ -> "")),
                () -> assertThrows(NullPointerException.class, () -> new Switch<Integer, String>(List.of(), null)),
                () -> assertThrows(NullPointerException.class, () -> Switch.<Integer, String>when(null)),
                () -> assertThrows(NullPointerException.class, () -> Switch.<Integer, String>when(_ -> true).then(null)),
                () -> assertThrows(NullPointerException.class, () -> Switch.<Integer, String>when(_ -> true).then(_ -> "").otherwise(null))
        );
    }

    @Test
    void testFizzBuzzStreamPipeline() {
        // given: no explicit type arguments; T and R are inferred through
        // the diamond operator from the target type of map()
        var result = IntStream.rangeClosed(1, 15)
                .boxed()
                .map(new Switch<>(List.of(
                        Case.of(i -> i % 15 == 0, _ -> "FizzBuzz"),
                        Case.of(i -> i % 3 == 0, _ -> "Fizz"),
                        Case.of(i -> i % 5 == 0, _ -> "Buzz")
                ), String::valueOf))
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
                .map(new Switch<>(List.of(
                        Case.of(String::isEmpty, _ -> "empty"),
                        Case.of(s -> s.length() == 1, _ -> "single")
                ), _ -> "other"))
                .toList();
        assertEquals(List.of("single", "other", "other", "empty"), result);
    }

    @Test
    void testGroupingByClassifierInPipeline() {
        var groups = Stream.of(-2, -1, 0, 1, 2)
                .collect(Collectors.groupingBy(new Switch<>(List.of(
                        Case.of((Integer i) -> i < 0, _ -> "negative"),
                        Case.of((Integer i) -> i > 0, _ -> "positive")
                ), _ -> "zero")));
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
    void testCompactAndTwoStepFormsAreEquivalent() {
        // both forms may be mixed freely within one chain
        var f = Switch.<Integer, String>when(i -> i < 0, _ -> "negative")
                .when(i -> i > 0).then(_ -> "positive")
                .otherwise(_ -> "zero");
        assertAll(
                () -> assertEquals("negative", f.apply(-1)),
                () -> assertEquals("zero", f.apply(0)),
                () -> assertEquals("positive", f.apply(1))
        );
    }

    @Test
    void testThenValueAndOtherwiseValue() {
        var f = Switch.<Integer, String>when(i -> i == 0).thenValue("zero")
                .when(i -> i > 0, _ -> "positive")
                .otherwiseValue("negative");
        assertAll(
                () -> assertEquals("zero", f.apply(0)),
                () -> assertEquals("positive", f.apply(3)),
                () -> assertEquals("negative", f.apply(-3))
        );
    }

    @Test
    void testTypeGuardedCasesNarrow() {
        // the functions receive the already-cast value,
        // mirroring case Integer i -> ... type patterns
        var f = Switch.<Object, String>builder()
                .when(Integer.class).then(i -> "int:" + (i + 1))
                .when(String.class).then(s -> "str:" + s.length())
                .when(Long.class).thenValue("long")
                .otherwise(x -> "other:" + x);
        assertAll(
                () -> assertEquals("int:42", f.apply(41)),
                () -> assertEquals("str:3", f.apply("abc")),
                () -> assertEquals("long", f.apply(5L)),
                () -> assertEquals("other:2.5", f.apply(2.5d))
        );
    }

    @Test
    void testTypeGuardNulls() {
        assertAll(
                () -> assertThrows(NullPointerException.class,
                        () -> Switch.<Object, String>builder().when((Class<Integer>) null)),
                () -> assertThrows(NullPointerException.class,
                        () -> Switch.<Object, String>builder().when(Integer.class).then(null))
        );
    }

    @Test
    void testRulesAssembledAtRuntime() {
        // the README use case: cases as data — impossible with a switch
        // expression, whose cases are compile-time constants
        record Order(BigDecimal total, boolean firstOrder, String loyaltyLevel) {}
        // e.g. loaded from configuration, in priority order
        var rules = List.of(
                Case.of(Order::firstOrder, _ -> new BigDecimal("0.15")),
                Case.of((Order o) -> o.total().compareTo(new BigDecimal(500)) > 0, _ -> new BigDecimal("0.10")),
                Case.of((Order o) -> "gold".equals(o.loyaltyLevel()), _ -> new BigDecimal("0.05"))
        );
        var discount = new Switch<>(rules, _ -> BigDecimal.ZERO);
        assertAll(
                // first order wins over all other rules
                () -> assertEquals(new BigDecimal("0.15"), discount.apply(new Order(new BigDecimal(1000), true, "gold"))),
                // large total wins over loyalty
                () -> assertEquals(new BigDecimal("0.10"), discount.apply(new Order(new BigDecimal(1000), false, "gold"))),
                () -> assertEquals(new BigDecimal("0.05"), discount.apply(new Order(new BigDecimal(100), false, "gold"))),
                () -> assertEquals(BigDecimal.ZERO, discount.apply(new Order(new BigDecimal(100), false, "none")))
        );
    }

    @Test
    void testComposesAsFunction() {
        var f = Switch.<Integer, Integer>when(i -> i < 0).then(_ -> -1)
                .when(i -> i > 0).then(_ -> 1)
                .otherwise(_ -> 0)
                .andThen(i -> i * 100);
        assertAll(
                () -> assertEquals(-100, f.apply(-5)),
                () -> assertEquals(100, f.apply(5)),
                () -> assertEquals(0, f.apply(0))
        );
    }
}
