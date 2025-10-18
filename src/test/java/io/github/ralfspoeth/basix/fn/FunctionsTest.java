package io.github.ralfspoeth.basix.fn;

import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
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
                () -> assertEquals("one", zipmap.get(1))
        );
    }


    /**
     * demo code
     */
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
                        .filter(e -> smaller ? e.getKey() <= limit : e.getKey() >= limit)
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
                if (sellOrders.isEmpty() || purchaseOrders.isEmpty()) return;
                if (sellOrders.firstKey() <= purchaseOrders.lastKey()) {
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


    @Test
    void conditionalTrue() {
        Function<Integer, String> f = conditional(
                i -> i > 0,
                i -> "positive",
                i -> "not positive"
        );
        assertEquals("positive", f.apply(5));
    }

    @Test
    void conditionalFalse() {
        Function<Integer, String> f = conditional(
                i -> i > 0,
                i -> "positive",
                i -> "not positive"
        );
        assertEquals("not positive", f.apply(-5));
        assertEquals("not positive", f.apply(0));
    }

    @Test
    void ofMap() {
        var m = Map.of(1, "one", 2, "two", 3, "three");
        record R(int x) {}
        var l = List.of(new R(2), new R(3));
        var expected = List.of("two", "three");
        assertEquals(expected, l.stream().map(of(m, R::x)).toList());
    }

    @Test
    void ofMapWithMissingKey() {
        var m = Map.of(1, "one");
        record R(int x) {}
        var f = of(m, R::x);
        assertNull(f.apply(new R(2)));
    }

    @Test
    void ofMapWithEmptyMap() {
        Map<Integer, String> m = Collections.emptyMap();
        record R(int x) {}
        var f = of(m, R::x);
        assertNull(f.apply(new R(1)));
    }

    @Test
    void ofList() {
        var list = List.of("a", "b", "c");
        var f = Functions.of(list);
        assertEquals("a", f.apply(0));
        assertEquals("b", f.apply(1));
        assertEquals("c", f.apply(2));
    }

    @Test
    void ofListIsDefensive() {
        var originalList = new ArrayList<>(List.of("a", "b"));
        var f = Functions.of(originalList);
        originalList.set(0, "z");
        assertEquals("a", f.apply(0));
    }

    @Test
    void ofListThrowsForOutOfBounds() {
        var list = List.of("a", "b", "c");
        var f = Functions.of(list);
        assertThrows(IndexOutOfBoundsException.class, () -> f.apply(3));
        assertThrows(IndexOutOfBoundsException.class, () -> f.apply(-1));
    }

    @Test
    void indexedWithStart() {
        var l = List.of("a", "b", "c");
        var f = indexed(1);
        var result = l.stream().map(f).toList();
        var expected = List.of(new Indexed<>(1, "a"), new Indexed<>(2, "b"), new Indexed<>(3, "c"));
        assertEquals(expected, result);
    }

    @Test
    void indexedIterableWithStart() {
        var l = List.of("a", "b", "c");
        var result = indexed(l, 5).toList();
        var expected = List.of(new Indexed<>(5, "a"), new Indexed<>(6, "b"), new Indexed<>(7, "c"));
        assertEquals(expected, result);
    }

    @Test
    void indexedIterableDefaultStart() {
        var l = List.of("a", "b", "c");
        var result = indexed(l).toList();
        var expected = List.of(new Indexed<>(0, "a"), new Indexed<>(1, "b"), new Indexed<>(2, "c"));
        assertEquals(expected, result);
    }

    @Test
    void indexedWithEmptyIterable() {
        var l = Collections.<String>emptyList();
        var result = indexed(l).toList();
        assertTrue(result.isEmpty());
    }

    @Test
    void labeledFromMap() {
        // Use LinkedHashMap to have a predictable iteration order
        var m = new LinkedHashMap<String, Integer>();
        m.put("one", 11);
        m.put("two", 22);
        var result = labeled(m).toList();
        var expected = List.of(new Labeled<>("one", 11), new Labeled<>("two", 22));
        assertEquals(expected, result);
    }

    @Test
    void labeledFromMapUnordered() {
        var m = Map.of("one", 11, "two", 22);
        var result = labeled(m).collect(Collectors.toSet());
        var expected = Set.of(new Labeled<>("one", 11), new Labeled<>("two", 22));
        assertEquals(expected, result);
    }

    @Test
    void labeledFromEmptyMap() {
        var m = Collections.<String, Integer>emptyMap();
        var result = labeled(m).toList();
        assertTrue(result.isEmpty());
    }

    @Test
    void labeledFromIterable() {
        record R(String name, int x) {}
        var rs = List.of(new R("one", 1), new R("two", 2));
        var result = labeled(rs, r -> r.name().substring(0, 1)).toList();
        var expected = List.of(new Labeled<>("o", new R("one", 1)), new Labeled<>("t", new R("two", 2)));
        assertEquals(expected, result);
    }

    @Test
    void labeledFromEmptyIterable() {
        var l = Collections.<String>emptyList();
        var result = labeled(l, String::toUpperCase).toList();
        assertTrue(result.isEmpty());
    }

    @Test
    void zipMapEqualSize() {
        var keys = List.of(1, 2, 3);
        var values = List.of("one", "two", "three");
        var map = zipMap(keys, values);

        assertEquals(3, map.size());
        assertEquals("one", map.get(1));
        assertEquals("two", map.get(2));
        assertEquals("three", map.get(3));
        assertEquals(keys, new ArrayList<>(map.keySet()));
        assertEquals(values, new ArrayList<>(map.values()));
    }

    @Test
    void zipMapKeysShorter() {
        var keys = List.of(1, 2);
        var values = List.of("one", "two", "three");
        var map = zipMap(keys, values);

        assertEquals(2, map.size());
        assertEquals(keys, new ArrayList<>(map.keySet()));
        assertEquals(List.of("one", "two"), new ArrayList<>(map.values()));
    }

    @Test
    void zipMapValuesShorter() {
        var keys = List.of(1, 2, 3);
        var values = List.of("one", "two");
        var map = zipMap(keys, values);

        assertEquals(2, map.size());
        assertEquals(List.of(1, 2), new ArrayList<>(map.keySet()));
        assertEquals(values, new ArrayList<>(map.values()));
    }

    @Test
    void zipMapWithEmptyKeys() {
        var keys = Collections.<Integer>emptyList();
        var values = List.of("one", "two", "three");
        var map = zipMap(keys, values);
        assertTrue(map.isEmpty());
    }

    @Test
    void zipMapWithEmptyValues() {
        var keys = List.of(1, 2, 3);
        var values = Collections.<String>emptyList();
        var map = zipMap(keys, values);
        assertTrue(map.isEmpty());
    }

    @Test
    void zipMapWithNulls() {
        var keys = Arrays.asList(1, null, 3);
        var values = Arrays.asList("one", "two", null);
        var map = zipMap(keys, values);

        assertEquals(3, map.size());
        assertEquals("one", map.get(1));
        assertEquals("two", map.get(null));
        assertNull(map.get(3));
        assertEquals(keys, new ArrayList<>(map.keySet()));
        assertEquals(values, new ArrayList<>(map.values()));
    }

}