package io.github.ralfspoeth.basix.fn;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static io.github.ralfspoeth.basix.fn.Functions.*;
import static java.util.Comparator.comparing;
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
        record Int(int x) {}
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
        // given
        var l = List.of("one", "two", "three");
        // when
        var f = Functions.of(l);
        // then
        assertAll(
                () -> assertEquals("one", f.apply(0)),
                () -> assertEquals("two", f.apply(1)),
                () -> assertEquals("three", f.apply(2)),
                () -> assertThrows(IndexOutOfBoundsException.class, () -> f.apply(-1)),
                () -> assertThrows(IndexOutOfBoundsException.class, () -> f.apply(3))
        );
    }

    @Test
    void testLabeled() {
        // given
        record Comp(String name, int age) {}
        var compList = List.of(new Comp("Ada", 50), new Comp("Lisp", 90), new Comp("Java", 30));
        // when
        var labeledList = compList.stream().map(l -> new Labeled<>(l.name, l.age)).toList();
        // then
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
                () -> assertEquals(List.of(4L), input.stream().gather(Gatherers.filterAndCast(Long.class)).toList()),
                () -> assertEquals(List.of(2d), input.stream().gather(Gatherers.filterAndCast(Double.class)).toList()),
                () -> assertEquals(List.of(1, 1), input.stream().gather(Gatherers.filterAndCast(Integer.class)).toList())
        );
    }

    @Test
    void testAlternatingEquals() {
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
                () -> assertEquals(List.of(), Stream.of().gather(Gatherers.alternatingEquality()).toList()),
                () -> assertEquals(List.of(1), Stream.of(1).gather(Gatherers.alternatingEquality()).toList()),
                () -> assertEquals(List.of(1), Stream.of(1, 1, 1).gather(Gatherers.alternatingEquality()).toList()),
                () -> assertEquals(List.of(1, 2), Stream.of(1, 2, 2).gather(Gatherers.alternatingEquality()).toList()),
                () -> assertEquals(List.of(1, 2, 1), Stream.of(1, 2, 1).gather(Gatherers.alternatingEquality()).toList()),
                () -> assertEquals(List.of(1, 2, 3, 2, 4, 3, 2, 3, 1), input.stream().gather(Gatherers.alternatingEquality()).toList())
        );
    }

    @Test
    void testAlternating() {
        // given
        record T(long millis, long K) {}
        // when
        var input = List.of(
                new T(1, 100), new T(2, 100), new T(3, 100),
                new T(4, 101),
                new T(5, 100)
        );
        var allAlt = input.stream().gather(Gatherers.alternating(comparing(T::K))).toList();
        var first4Alt = input.stream().limit(4).gather(Gatherers.alternating(comparing(T::K))).toList();
        // then
        assertAll(
                () -> assertEquals(List.of(new T(1, 100), new T(4, 101), new T(5, 100)), allAlt),
                () -> assertEquals(List.of(new T(1, 100), new T(4, 101)), first4Alt)
        );
    }



    @Test
    void testMonotone123() {
        // given
        var input = List.of(1, 2, 3);
        // then
        assertAll(
                () -> assertEquals(input, input.stream().gather(Gatherers.increasing()).toList()),
                () -> assertEquals(List.of(1), input.stream().gather(Gatherers.decreasing()).toList())
        );
    }

    @Test
    void testMonotone120() {
        // given
        var input = List.of(1, 2, 0);
        // then
        assertAll(
                () -> assertEquals(List.of(1, 2), input.stream().gather(Gatherers.increasing()).toList()),
                () -> assertEquals(List.of(1, 0), input.stream().gather(Gatherers.decreasing()).toList())
        );
    }

    @Test
    void testMonotone1213() {
        // given
        var input = List.of(1, 2, 1, 3);
        // then
        assertAll(
                () -> assertEquals(List.of(1, 2, 3), input.stream().gather(Gatherers.increasing()).toList()),
                () -> assertEquals(List.of(3, 1), input.reversed().stream().gather(Gatherers.decreasing()).toList())
        );
    }

    @Test
    void testReverse() {
        // given
        var input = List.of(1, 2, 3);
        // then
        assertAll(
                () -> assertEquals(List.of(3, 2, 1), input.stream().gather(Gatherers.reverse()).toList()),
                () -> assertEquals(input, input.stream().gather(Gatherers.reverse()).gather(Gatherers.reverse()).toList()),
                () -> assertEquals(input, input.stream().gather(Gatherers.reverse()).gather(Gatherers.alternatingEquality()).gather(Gatherers.reverse()).toList()),
                () -> assertEquals(input, input.stream().gather(Gatherers.reverse().andThen(Gatherers.reverse()).andThen(Gatherers.alternatingEquality())).toList())
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
                () -> assertTrue(input.stream().filter(filterNone).gather(Gatherers.single()).findFirst().isEmpty()),
                () -> assertEquals(3, input.stream().filter(filterOne).gather(Gatherers.single()).findFirst().orElseThrow()),
                () -> assertTrue(input.stream().filter(filterAll).gather(Gatherers.single()).findFirst().isEmpty()),
                () -> assertTrue(input.stream().filter(filterSome).gather(Gatherers.single()).findFirst().isEmpty())
        );
    }

    @Test
    void testSingleInParallel() {
        // given
        var input = IntStream.generate(() -> 1).limit(1_000_000L).boxed().parallel();
        // then
        assertTrue(input.gather(Gatherers.single()).findFirst().isEmpty());
    }

    @Test
    void testExactlyN() {
        // given
        var input = List.of(1, 2, 3, 4, 5);
        // when
        var empty = List.of();
        var first3 = List.of(1, 2, 3);
        // then
        assertAll(
                () -> assertEquals(first3, input.stream().limit(3).gather(Gatherers.exactly(3)).toList()),
                () -> assertEquals(empty, input.stream().gather(Gatherers.exactly(3)).toList()),
                () -> assertEquals(empty, input.stream().limit(2).gather(Gatherers.exactly(3)).toList()),
                () -> assertEquals(empty, input.stream().limit(4).gather(Gatherers.exactly(3)).toList())
        );
    }

    @Test
    void testMonoSeq() {
        // given
        var monotoneSequencesGatherer = Gatherers.<Integer>monotoneSequences();
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
        // given
        var input = List.of(1, 2, 2, 3, 3, 3, 4, 4, 3, 2, -1, -1, -1);
        // when
        var mono = List.of(List.of(1, 2, 3, 4), List.of(4, 3, 2, -1));
        var alt = List.of(1, 2, 3, 4, 3, 2, -1);
        var inc = List.of(1, 2, 3, 4);
        var dec = List.of(1, -1);
        // then
        assertAll(
                () -> assertEquals(alt, input.stream().gather(Gatherers.alternating()).toList()),
                () -> assertEquals(mono, input.stream().gather(Gatherers.alternating()).gather(Gatherers.monotoneSequences()).toList()),
                () -> assertEquals(inc, input.stream().gather(Gatherers.increasing()).toList()),
                () -> assertEquals(dec, input.stream().gather(Gatherers.decreasing()).toList())
        );
    }

    @Test
    void testZipMap() {
        // given
        var keys = List.of(1, 2, 3);
        var vals = List.of("one", "two", "three");
        // when
        var zipmap = zipMap(keys, vals);
        // then
        assertEquals(Map.of(1, "one", 2, "two", 3, "three"), zipmap);
    }

    @Test
    void testZipMapInv() {
        // given
        var keys = List.of(1, 2);
        var vals = List.of("one");
        // when
        var zipmap = zipMap(keys, vals);
        // then
        assertAll(
                () -> assertEquals(1, zipmap.size()),
                () -> assertEquals(1, zipmap.firstEntry().getKey()),
                () -> assertEquals("one", zipmap.firstEntry().getValue())
        );
    }

    @Test
    void testInterleave() {
        // given
        var input = IntStream.range(0, 100).boxed();
        // when
        var first = new AtomicInteger(100);
        var ret = input.gather(Gatherers.interleave(first::getAndIncrement)).limit(7).toList();
        // then
        System.out.println(ret);
    }

    @Test
    void testInterleaveList() {
        // given
        var data = List.of(1, 3, 5);
        var intersperse = List.of(2, 4, 6);
        // when
        var result = data.stream().gather(Gatherers.interleave(intersperse.iterator()::next)).toList();
        // then
        assertEquals(List.of(1, 2, 3, 4, 5, 6), result);
    }

    @Test
    void testInterleaveRot() {
        // given
        var data = List.of(1, 2, 3, 4);
        var inter = List.of(-1, -2);
        // when
        var result = data.stream().gather(Gatherers.interleaveRotating(inter)).toList();
        // then
        assertEquals(List.of(1, -1, 2, -2, 3, -1, 4, -2), result);
    }

    @Test
    void testInterleaveAvail() {
        // given
        var data = List.of(1, 2, 3);
        var inter = List.of(-1, -2);
        // when
        var result = data.stream().gather(Gatherers.interleaveAvailable(inter)).toList();
        assertEquals(List.of(1, -1, 2, -2, 3), result);
    }

    @Test
    void testConst() {
        // given
        var input = Stream.of(1, 2, 3);
        // when
        var result = input.gather(Gatherers.interleave(()->0)).toList();
        // then
        assertEquals(List.of(1, 0, 2, 0, 3, 0), result);
    }

    @Test
    void testRnd() {
        // given
        var input = Stream.of(1, 2, 3);
        var rnd = ThreadLocalRandom.current();
        // when
        var result = input.gather(Gatherers.interleave(rnd::nextInt)).toList();
        // then
        assertAll(
                () -> assertEquals(6, result.size()),
                () -> assertEquals(1, result.getFirst()),
                () -> assertEquals(2, result.get(2)),
                () -> assertEquals(3, result.get(4))
        );
    }



    @Test
    void orderBook() {
        // given
        record Order(long amount, int limit) {
            Order {
                if (limit < 0) throw new IllegalArgumentException("limit cannot be negative");
                if (amount == 0) throw new IllegalArgumentException("amount cannot be zero");
            }
        }
        class OrderBook {
            final SortedMap<Integer, List<Order>> purchaseOrders = new TreeMap<>();
            final SortedMap<Integer, List<Order>> sellOrders = new TreeMap<>();

            long sellAmountAt(int limit) {
                return amountAt(limit, true);
            }

            long purchaseAmountAt(int limit) {
                return amountAt(limit, false);
            }

            long amountAt(int limit, boolean smaller) {
                return sellOrders.entrySet()
                        .stream()
                        .filter(e -> smaller?e.getKey()<=limit:e.getKey()>=limit)
                        .map(Map.Entry::getValue)
                        .flatMap(List::stream)
                        .mapToLong(Order::amount)
                        .map(Math::abs)
                        .sum();
            }

            void postOrder(Order order) {
                if (order.amount() < 0) {
                    sellOrders.compute(order.limit, (_, l) -> {
                        if (l == null) l = new ArrayList<>();
                        l.add(order);
                        return l;
                    });
                } else {
                    purchaseOrders.compute(order.limit, (_, l) -> {
                        if (l == null) l = new ArrayList<>();
                        l.add(order);
                        return l;
                    });
                }
                tryProcessOrders();
            }
            void tryProcessOrders() {
                if(sellOrders.isEmpty() || purchaseOrders.isEmpty()) return;
                if(sellOrders.firstKey() <= purchaseOrders.lastKey()) {
                    System.out.println(this);
                }
            }

            @Override
            public String toString() {
                return purchaseOrders + "//" + sellOrders;
            }

            void printOrdersAtLimit(int limit) {
                System.out.printf("Limit: %3d, Purchase amount: %6d, Sell amount: %6d%n",
                        limit, purchaseAmountAt(limit), sellAmountAt(limit)
                );
            }
        }
        // when
        var book = new OrderBook();
        book.postOrder(new Order(100, 11));
        book.postOrder(new Order(100, 12));
        book.postOrder(new Order(100, 13));
        book.postOrder(new Order(-100, 10));
        book.postOrder(new Order(-80, 11));
        book.printOrdersAtLimit(8);
        book.printOrdersAtLimit(9);
        book.printOrdersAtLimit(10);
        book.printOrdersAtLimit(11);
        book.printOrdersAtLimit(12);
        book.printOrdersAtLimit(13);
        book.printOrdersAtLimit(14);
    }
}