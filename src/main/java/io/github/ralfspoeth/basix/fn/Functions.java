package io.github.ralfspoeth.basix.fn;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Functions {

    // prevent instantiation
    private Functions() {}

    /**
     * Resembles the ternary operator {@code ?:}.
     * <p>
     * {@snippet :
     * int x = 7;
     * int y = conditional(
     * t -> t==0,
     * _ -> "NULL",
     * t -> "not null-y: " + t
     * ).apply(x); // "not null-y: 7"
     *}
     *
     * @param condition the test condition
     * @param ifTrue    function that returns a result if the test condition evaluates to true
     * @param ifFalse   function that returns a result if the test condition evaluates to false
     * @param <T>       the type to be filtered
     * @param <R>       the return type
     * @return the return value of either {@code ifTrue} or {@code ifFalse}
     */
    public static <T, R> Function<T, R> conditional(
            Predicate<? super T> condition,
            Function<? super T, ? extends R> ifTrue,
            Function<? super T, ? extends R> ifFalse
    ) {
        return t -> condition.test(t) ? ifTrue.apply(t) : ifFalse.apply(t);
    }

    /**
     * Create a {@link Function function} based on a {@link Map map}
     * and an extraction function which extracts some property
     * of some object.
     * Example:
     * {@snippet :
     * // given
     * var m = Map.of(1, "one", 2, "two", 3, "three");
     * record R(int x) {}
     * // when
     * var l = List.of(new R(2), new R(3));
     * // then
     * assert List.of("two", "three").equals(l.stream().map(of(m, R::x)).toList());
     *}
     *
     * @param m         a map
     * @param extractor a function
     * @param <T>       target type of the function
     * @param <R>       return type of the function
     * @return a function that maps the extracted key to a value
     */
    public static <T, R> Function<T, R> of(Map<?, R> m, Function<T, ?> extractor) {
        m = Map.copyOf(m);
        return extractor.andThen(m::get);
    }

    /**
     * Considers a {@link List list} to be a {@link Function function}
     * of an index {@code i}.
     *
     * @param l   the list; defensively copied using {@link List#copyOf(Collection)}
     * @param <R> the content type of the list
     * @return the function
     */
    public static <R> IntFunction<R> of(List<R> l) {
        l = List.copyOf(l);
        return l::get;
    }

    /**
     * Create a {@link Function} which turns any object into an {@link Indexed} object,
     * that is, a record composed of an index with this object.
     * The index is incremented everytime the function is called.
     * Example:
     * {@snippet :
     * // given
     * var l = List.of("a", "b", "c");
     * // when
     * var r = List.of(new Indexed<>(1, "a"), new Indexed<>(2, "b"), new Indexed<>(3, "c"));
     * // then
     * assert l.stream().map(indexed(1)).toList().equals(r);
     *
     *}
     *
     * @param startWith the first index value
     * @param <T>       the value type wrapped
     * @return a function
     */
    public static <T> Function<T, Indexed<T>> indexed(int startWith) {
        var seq = new AtomicInteger(startWith);
        return x -> new Indexed<>(seq.getAndIncrement(), x);
    }

    /**
     * Applies {@link #indexed(int)} to the given array.
     *
     * @param array     an array or some other {@link Iterable}
     * @param startWith the first index value
     * @param <T>       the type of the values of the array
     * @return a stream of {@link Indexed} values
     */
    public static <T> Stream<Indexed<T>> indexed(Iterable<T> array, int startWith) {
        return StreamSupport.stream(array.spliterator(), false).map(indexed(startWith));
    }

    /**
     * Applies {@link #indexed(int)} with a first index value of 0
     * to the given array.
     *
     * @see #indexed(Iterable, int)
     */
    public static <T> Stream<Indexed<T>> indexed(Iterable<T> array) {
        return indexed(array, 0);
    }

    /**
     * Turns a map into a stream of its values labeled with
     * the respective key.
     * Example:
     * {@snippet :
     * // given
     * var m = Map.of("one", 11, "two", 22);
     * // then
     * assert labeled(m).toList().equals(List.of(new Labeled<>("one", 11), new Labeled<>("two", 22)));
     *}
     *
     * @param map a map of key-value pairs
     * @param <K> the type of the keys in the map
     * @param <T> the type of the values in the map
     * @return a stream of labeled values
     */
    public static <K, T> Stream<Labeled<K, T>> labeled(Map<K, T> map) {
        return map.entrySet()
                .stream()
                .map(e -> new Labeled<>(e.getKey(), e.getValue()));
    }

    /**
     * Turns an iterable list of values into stream of {@link Labeled} values
     * where the label is extracted from each value by applying the given labelling function.
     * Example:
     * {@snippet :
     * // given
     * record R(String name, int x) {}
     * var rs = List.of(new R("one", 1), new R("two", 2));
     * // when
     * var result = List.of(new Labeled<>("o", new R("one", 1)), new Labeled<>("t", new R("two", 2)));
     * // then
     * assert result.equals(labeled(rs, r -> r.name().substring(0, 1)).toList());
     *}
     *
     * @param list  a list or array of values.
     * @param label a labelling function
     * @param <L>   the type of the label
     * @param <T>   the type of the value
     * @return a stream of labeled values
     */
    public static <L, T> Stream<Labeled<L, T>> labeled(Iterable<T> list, Function<T, L> label) {
        return StreamSupport
                .stream(list.spliterator(), false)
                .map(t -> new Labeled<>(label.apply(t), t));
    }


    /**
     * Creates a sequenced map by "zipping" two independent arrays whereby the order
     * of the arrays is preserved, and the shorter array determines the size of the map.
     * Example:
     * {@snippet :
     * // given
     * var keys = new int[]{1, 2, 3}; // 3 items
     * var vals = new String[]{"one", "two", "three", "four"}; // 4 items
     * // when
     * var seqMap = zipMap(keys, vals);
     * // then
     * assert 3==seqMap.size();
     * assert List.of(keys).equals(seqMap.sequencedKeySet().stream().toList());
     * assert List.of("one", "two", "three").equals(seqMap.sequencedValues().stream().toList());
     *}
     *
     * @param keys   array of keys, may contain {@code null} keys
     * @param values array of values, may contain {@code null} values
     * @param <K>    the type of the keys
     * @param <V>    the type of the values
     * @return a sequenced map where the first key/value pair is made of the first key and the first value,
     * the second key/value pair made of the second key and the second value and so forth;
     * note that the map may contain {@code} null keys and/or values
     */
    @SuppressWarnings("unchecked")
    public static <K, V> SequencedMap<K, V> zipMap(Iterable<K> keys, Iterable<V> values) {
        SequencedMap<K, V> tmp = new LinkedHashMap<>();
        for (Iterator<?> itk = keys.iterator(), itv = values.iterator(); itk.hasNext() && itv.hasNext(); ) {
            tmp.put((K) itk.next(), (V) itv.next());
        }
        return tmp;
    }

    static <T> Predicate<T> unboxedBoolean(Function<T, Boolean> f) {
        return f::apply;
    }
}
