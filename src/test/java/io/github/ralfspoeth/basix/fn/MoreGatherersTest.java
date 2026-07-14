package io.github.ralfspoeth.basix.fn;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static io.github.ralfspoeth.basix.fn.Functions.filterAndCast;
import static io.github.ralfspoeth.basix.fn.MoreGatherers.*;
import static org.junit.jupiter.api.Assertions.*;

class MoreGatherersTest {

    @Test
    void testFilterAndCast() {
        List<Number> list = List.of(1L, 2, 3.14d, 4L);
        assertEquals(
                List.of(1L, 4L),
                list.stream().gather(filterAndCast(Long.class)).toList()
        );
    }

    @Test
    void testFilterAndCastParallel() {
        var longs = IntStream.range(0, 10_000)
                .mapToObj(i -> i % 2 == 0 ? (Number) (long) i : (Number) i)
                .toList();
        assertEquals(
                5_000,
                longs.parallelStream().gather(filterAndCast(Long.class)).count()
        );
    }

    @Test
    void testDistinctUntilChanged() {
        assertAll(
                () -> assertEquals(List.of(1), Stream.of(1, 1, 1).gather(distinctUntilChanged()).toList()),
                () -> assertEquals(List.of(1, 2), Stream.of(1, 2, 2).gather(distinctUntilChanged()).toList()),
                () -> assertEquals(List.of(1, 2, 1), Stream.of(1, 2, 1).gather(distinctUntilChanged()).toList()),
                () -> assertEquals(List.of(), Stream.of().gather(distinctUntilChanged()).toList())
        );
    }

    @Test
    void testDistinctUntilChangedComparator() {
        assertAll(
                () -> assertEquals(List.of(1, 2, 1), Stream.of(1, 1, 2, 2, 1, 1).gather(distinctUntilChanged(Comparator.naturalOrder())).toList()),
                () -> assertEquals(List.of(3, 1, 3), Stream.of(3, 3, 1, 3).gather(distinctUntilChanged(Comparator.naturalOrder())).toList())
        );
    }

    @Test
    void testIncreasing() {
        assertAll(
                // javadoc example: first occurrences of 2, 3, 4 pass through
                () -> assertEquals(List.of(1, 2, 3, 4), Stream.of(1, 2, 1, 3, 1, 4).gather(increasing()).toList()),
                // strictly increasing: equal elements are dropped
                () -> assertEquals(List.of(1, 2, 3), Stream.of(1, 1, 2, 2, 3, 3).gather(increasing()).toList()),
                // entirely decreasing input yields just the first element
                () -> assertEquals(List.of(3), Stream.of(3, 2, 1).gather(increasing()).toList()),
                () -> assertEquals(List.of(), Stream.<Integer>of().gather(increasing()).toList())
        );
    }

    @Test
    void testDecreasing() {
        assertAll(
                () -> assertEquals(List.of(4, 3, 2, 1), Stream.of(4, 3, 4, 2, 4, 1).gather(decreasing()).toList()),
                () -> assertEquals(List.of(3, 2, 1), Stream.of(3, 3, 2, 2, 1, 1).gather(decreasing()).toList()),
                () -> assertEquals(List.of(1), Stream.of(1, 2, 3).gather(decreasing()).toList())
        );
    }

    @Test
    void testReverse() {
        assertAll(
                () -> assertEquals(List.of(3, 2, 1), Stream.of(1, 2, 3).gather(reverse()).toList()),
                () -> assertEquals(List.of(), Stream.of().gather(reverse()).toList())
        );
    }

    @Test
    void testSingle() {
        assertAll(
                () -> assertEquals(1, Stream.of(1).gather(single()).findFirst().orElseThrow()),
                () -> assertTrue(Stream.of(1, 2).gather(single()).findFirst().isEmpty()),
                () -> assertEquals(List.of(), Stream.of().gather(single()).toList())
        );
    }

    @Test
    void testExactly() {
        assertAll(
                () -> assertEquals(List.of(1, 2), Stream.of(1, 2).gather(exactly(2)).toList()),
                () -> assertEquals(List.of(), Stream.of(1, 2, 3).gather(exactly(2)).toList()),
                () -> assertEquals(List.of(), Stream.of(1).gather(exactly(2)).toList()),
                // parallel evaluation must yield the same result
                () -> assertEquals(List.of(), IntStream.range(0, 10_000).boxed().parallel().gather(exactly(3)).toList())
        );
    }

    @Test
    void testMonotoneSequences() {
        assertEquals(
                List.of(List.of(1, 2, 3), List.of(3, 1), List.of(1, 2, 3)),
                Stream.of(1, 2, 3, 1, 2, 3).gather(monotoneSequences()).toList()
        );
    }

    @Test
    void testMonotoneSequencesEdgeCases() {
        assertAll(
                // singleton
                () -> assertEquals(List.of(List.of(1)), Stream.of(1).gather(monotoneSequences()).toList()),
                // all-equal elements form a single sequence
                () -> assertEquals(List.of(List.of(1, 1)), Stream.of(1, 1).gather(monotoneSequences()).toList()),
                // ties within a run don't split it
                () -> assertEquals(List.of(List.of(1, 1, 2)), Stream.of(1, 1, 2).gather(monotoneSequences()).toList()),
                () -> assertEquals(List.of(), Stream.<Integer>of().gather(monotoneSequences()).toList())
        );
    }

    @Test
    void testNullsRejected() {
        // comparison-based gatherers fail fast on null elements,
        // in line with the @NullMarked contract of the module
        assertAll(
                () -> assertThrows(NullPointerException.class, () -> Stream.of(1, null, 2).gather(distinctUntilChanged()).toList()),
                () -> assertThrows(NullPointerException.class, () -> Stream.of(2, null, 1).gather(distinctUntilChanged(Comparator.naturalOrder())).toList()),
                () -> assertThrows(NullPointerException.class, () -> Stream.of((Integer) null).gather(increasing()).toList()),
                () -> assertThrows(NullPointerException.class, () -> Stream.of((Integer) null).gather(decreasing()).toList()),
                () -> assertThrows(NullPointerException.class, () -> Stream.of(1, null, 2).gather(monotoneSequences()).toList())
        );
    }

    @Test
    void testExactlyNegativeThrows() {
        assertThrows(IllegalArgumentException.class, () -> exactly(-1));
    }

    @Test
    void testSinglePassesNullThrough() {
        assertEquals(Collections.singletonList(null), Stream.of((Integer) null).gather(single()).toList());
    }

    @Test
    void testInterleaveRotatingReusable() {
        var gatherer = MoreGatherers.interleaveRotating(List.of(8, 9));
        // each stream must start the rotation afresh
        assertEquals(List.of(1, 8, 2, 9), Stream.of(1, 2).gather(gatherer).toList());
        assertEquals(List.of(1, 8, 2, 9), Stream.of(1, 2).gather(gatherer).toList());
    }

    @Test
    void testInterleaveConstant() {
        assertEquals(
                List.of(1, 7, 2, 7, 3, 7),
                Stream.of(1, 2, 3).gather(interleave(() -> 7)).toList()
        );
    }

    @Test
    void testIntersperse() {
        assertAll(
                () -> assertEquals(List.of(1, 0, 2, 0, 3), Stream.of(1, 2, 3).gather(intersperse(0)).toList()),
                () -> assertEquals(List.of(1), Stream.of(1).gather(intersperse(0)).toList()),
                () -> assertEquals(List.of(), Stream.<Integer>of().gather(intersperse(0)).toList()),
                // separator may be null
                () -> assertEquals(Arrays.asList(1, null, 2), Stream.of(1, 2).gather(intersperse(null)).toList())
        );
    }

    @Test
    void testInterleaveGenerated() {
        var rnd = ThreadLocalRandom.current();
        var result = Stream.of(1, 2, 3).gather(interleave(rnd::nextInt)).toList();
        assertAll(
                () -> assertEquals(6, result.size()),
                () -> assertEquals(1, result.get(0)),
                () -> assertEquals(2, result.get(2)),
                () -> assertEquals(3, result.get(4))
        );
    }

    @Test
    void testInterleaveRotating() {
        assertAll(
                () -> assertEquals(
                        List.of(0, 1, 1, 2, 2, 1, 3, 2, 4, 1, 5, 2, 6, 1, 7, 2, 8, 1, 9, 2),
                        IntStream.range(0, 10).boxed().gather(interleaveRotating(List.of(1, 2))).toList()
                ),
                () -> assertThrows(IllegalArgumentException.class, () -> interleaveRotating(List.of()))
        );
    }

    @Test
    void testInterleaveAvailable() {
        assertEquals(
                List.of(1, 100, 2, 1000, 3, 4),
                Stream.of(1, 2, 3, 4).gather(interleaveAvailable(List.of(100, 1000))).toList()
        );
    }

    @Test
    void testInterleaveAppendRest() {
        assertAll(
                () -> assertEquals(
                        List.of(1, 91, 2, 92, 93, 94),
                        Stream.of(1, 2).gather(interleaveAppendRest(List.of(91, 92, 93, 94))).toList()
                ),
                // source smaller than upstream: behaves like interleaveAvailable
                () -> assertEquals(
                        List.of(1, 91, 2, 3),
                        Stream.of(1, 2, 3).gather(interleaveAppendRest(List.of(91))).toList()
                )
        );
    }
}
