package io.github.ralfspoeth.basix.fn;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
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
        record Comp(String name, int age) {
        }
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
        var input = new ArrayList<Number>(List.of(1, 2d, 3f, 4L, 1));
        // when
        input.add(null);
        // then
        assertAll(
                () -> assertEquals(List.of(4L), input.stream().gather(filterAndCast(Long.class)).toList()),
                () -> assertEquals(List.of(2d), input.stream().gather(filterAndCast(Double.class)).toList()),
                () -> assertEquals(List.of(1, 1), input.stream().gather(filterAndCast(Integer.class)).toList())
        );
    }

    @Test
    void testAlternating() {
        // given
        var input = List.of(
                1,
                2, 2,
                3,
                2, 2, 2,
                4, 4,
                3, 3,
                2,
                3,
                1
        );
        // when
        // then
        assertAll(
                () -> assertEquals(List.of(), Stream.of().gather(alternating()).toList()),
                () -> assertEquals(List.of(1), Stream.of(1).gather(alternating()).toList()),
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
                () -> assertEquals(List.of(3, 2, 1), input.stream().gather(reverse()).toList()),
                () -> assertEquals(input, input.stream().gather(reverse()).gather(reverse()).toList()),
                () -> assertEquals(input, input.stream().gather(reverse()).gather(alternating()).gather(reverse()).toList()),
                () -> assertEquals(input, input.stream().gather(reverse().andThen(reverse()).andThen(alternating())).toList())
        );
    }

    @Test
    void testSingle() {
        // given
        var input = List.of(1, 2, 3);
        // when
        Predicate<Integer> filterNone = _ -> false;
        Predicate<Integer> filterOne = i -> i == 3;
        Predicate<Integer> filterAll = _ -> true;
        Predicate<Integer> filterSome = i -> i > 1;
        // then
        assertAll(
                () -> assertTrue(input.stream().filter(filterNone).gather(single()).findFirst().isEmpty()),
                () -> assertEquals(3, input.stream().filter(filterOne).gather(single()).findFirst().orElseThrow()),
                () -> assertTrue(input.stream().filter(filterAll).gather(single()).findFirst().isEmpty()),
                () -> assertTrue(input.stream().filter(filterSome).gather(single()).findFirst().isEmpty())
        );
    }

    @Test
    void testSingleInParallel() {
        // given
        var input = IntStream.generate(() -> 1).limit(1_000_000L).boxed().parallel();
        // then
        assertTrue(input.gather(single()).findFirst().isEmpty());
    }

    @Test
    void testMonoSeq() {
        // given
        var monotoneSequencesGatherer = Functions.<Integer>monotoneSequences();
        // then
        assertAll(
                () -> assertEquals(
                        List.of(List.of(1, 2, 3)),
                        Stream.of(1, 2, 3).gather(monotoneSequencesGatherer).toList()
                ), () -> assertEquals(
                        List.of(List.of(1, 2, 3), List.of(3, 2, 1)),
                        Stream.of(1, 2, 3, 2, 1).gather(monotoneSequencesGatherer).toList()
                ), () -> assertEquals(
                        List.of(List.of(1, 2, 3), List.of(3, 1)),
                        Stream.of(1, 2, 3, 1).gather(monotoneSequencesGatherer).toList()
                ), () -> assertEquals(
                        List.of(List.of(1, 2, 3), List.of(3, 1), List.of(1, 2, 3)),
                        Stream.of(1, 2, 3, 1, 2, 3).gather(monotoneSequencesGatherer).toList()
                ), () -> assertEquals(
                        List.of(List.of(1, 1, 1, 2)),
                        Stream.of(1, 1, 1, 2).gather(monotoneSequencesGatherer).toList()
                ), () -> assertEquals(
                        List.of(List.of(1, 1, 1, 2), List.of(2, 1)),
                        Stream.of(1, 1, 1, 2, 1).gather(monotoneSequencesGatherer).toList()
                ), () -> assertEquals(
                        List.of(List.of(1, 1, 1)),
                        Stream.of(1, 1, 1).gather(monotoneSequencesGatherer).toList()
                ), () -> assertEquals(
                        List.of(List.of(1, 1)),
                        Stream.of(1, 1).gather(monotoneSequencesGatherer).toList()
                ), () -> assertEquals(
                        List.of(List.of(1)),
                        Stream.of(1).gather(monotoneSequencesGatherer).toList()
                )
        );
    }


    @Test
    void combinedTest() {
        var input = List.of(1, 2, 2, 3, 3, 3, 4, 4, 3, 2, -1, -1, -1);
        System.out.println(input.stream().gather(alternating()).gather(monotoneSequences()).toList());
        System.out.println(input.stream().gather(increasing()).toList());
        System.out.println(input.stream().gather(decreasing()).toList());
    }
}