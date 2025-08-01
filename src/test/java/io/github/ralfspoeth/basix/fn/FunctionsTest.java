package io.github.ralfspoeth.basix.fn;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.*;
import java.util.stream.IntStream;

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
                    sellOrders.compute(order.limit, (ignored, l) -> {
                        if (l == null) l = new ArrayList<>();
                        l.add(order);
                        return l;
                    });
                } else {
                    purchaseOrders.compute(order.limit, (ignored, l) -> {
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