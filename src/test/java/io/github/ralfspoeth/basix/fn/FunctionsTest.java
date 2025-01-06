package io.github.ralfspoeth.basix.fn;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static io.github.ralfspoeth.basix.fn.Functions.*;
import static org.junit.jupiter.api.Assertions.*;

class FunctionsTest {

    @Test
    void testConditional() {
        var l = List.of(1, 2, 3);
        var evenSquaredOfL = l.stream().map(
                conditional(i -> i % 2 == 0, i -> i * i, i -> i)
        ).toList();
        assertEquals(List.of(1, 4, 3), evenSquaredOfL);
    }

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
                () -> assertThrows(Exception.class, () -> f.apply(-1)),
                () -> assertThrows(Exception.class, () -> f.apply(3))
        );
    }

    @Test
    void testLabeled() {
        record Comp(String name, int age) {}
        var compList = List.of(new Comp("Ada", 50), new Comp("Lisp", 90), new Comp("Java", 30));
        var labeledList = compList.stream().map(l -> new Labeled<>(l.name, l.age)).toList();
        assertAll(
                () -> assertEquals(compList.size(), labeledList.size()),
                () -> IntStream.range(0, compList.size()).forEach(
                        i -> assertEquals(compList.get(i).name, labeledList.get(i).label())
                ),
                () -> IntStream.range(0, labeledList.size()).forEach(
                        i -> assertEquals(compList.get(i).age, labeledList.get(i).value())
                )
        );
    }

    @Test
    void testFilterAndCast() {
        // given
        var input = new ArrayList<Number>();
        // when
        input.addAll(List.of(1, 2d, 3f, 4L, 1));
        input.add(null);
        // then
        assertAll(
                () -> assertEquals(List.of(4L), input.stream().gather(filterAndCast(Long.class)).toList()),
                () -> assertEquals(List.of(1, 1), input.stream().gather(filterAndCast(Integer.class)).toList())
        );
    }

    @Test
    void testAlternating() {
        // given
        var input = List.of(1, 2, 2, 3, 2, 2, 2, 4, 4, 3, 3, 2, 3, 1);
        // when
        // then
        assertAll(
                () -> assertEquals(List.of(1), Stream.of(1, 1, 1).gather(alternating()).toList()),
                () -> assertEquals(List.of(1, 2), Stream.of(1, 2, 2).gather(alternating()).toList()),
                () -> assertEquals(List.of(1, 2, 1), Stream.of(1, 2, 1).gather(alternating()).toList()),
                () -> assertEquals(List.of(1, 2, 3, 2, 4, 3, 2, 3, 1), input.stream().gather(alternating()).toList())
        );
    }

    @Test
    void testMonotone123() {
        // given
        var input = List.of(1, 2, 3);
        // then
        assertAll(
                () -> assertEquals(input, input.stream().gather(increasing()).toList()),
                () -> assertEquals(List.of(1), input.stream().gather(decreasing()).toList())
        );
    }

    @Test
    void testMonotone120() {
        // given
        var input = List.of(1, 2, 0);
        // then
        assertAll(
                () -> assertEquals(List.of(1, 2), input.stream().gather(increasing()).toList()),
                () -> assertEquals(List.of(1, 0), input.stream().gather(decreasing()).toList())
        );
    }

    @Test
    void testMonotone1213() {
        // given
        var input = List.of(1, 2, 1, 3);
        // then
        assertAll(
                () -> assertEquals(List.of(1, 2, 3), input.stream().gather(increasing()).toList()),
                () -> assertEquals(List.of(3, 1), input.reversed().stream().gather(decreasing()).toList())
        );
    }

    @Test
    void testReverse() {
        // given
        var input = List.of(1, 2, 3);
        // then
        assertAll(
                ()-> assertEquals(List.of(3, 2, 1), input.stream().gather(reverse()).toList()),
                ()-> assertEquals(input, input.stream().gather(reverse()).gather(reverse()).toList()),
                ()-> assertEquals(input, input.stream().gather(reverse()).gather(alternating()).gather(reverse()).toList()),
                ()-> assertEquals(input, input.stream().gather(reverse().andThen(reverse()).andThen(alternating())).toList())
        );
    }
}