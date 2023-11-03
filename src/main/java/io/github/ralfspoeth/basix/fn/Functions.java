package io.github.ralfspoeth.basix.fn;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class Functions {

    private Functions() {
        // prevent instantiation
    }


    public static <T, R> Function<T, R> of(Map<T, R> m) {
        m = Map.copyOf(m);
        return m::get;
    }

    /**
     * Create a {@link Function function} based on a {@link Map map}
     * and an extraction function which extracts some property
     * of some object.
     *
     * @param m a map
     * @param extr a function
     * @return a function that maps the extracted key to a value
     * @param <T> target type of the function
     * @param <R> return type of the function
     */
    public static <T, R> Function<T, R> of(Map<?, R> m, Function<T, ?> extr) {
        m = Map.copyOf(m);
        return extr.andThen(m::get);
    }

    /**
     * Considers a {@link List list} to be a {@link Function function}
     * of an index {@code i}.
     *
     * @param l the list; defensively copied using {@link List#copyOf(Collection)}
     * @return the function
     * @param <R> the content type of the list
     */
    public static <R> IntFunction<R> of(List<R> l) {
        l = List.copyOf(l);
        return l::get;
    }

    public static <T> Function<T, Indexed<T>> indexed(int startWith) {
        var seq = new AtomicInteger(startWith);
        return x -> new Indexed<>(seq.getAndIncrement(), x);
    }

    public static <T> Stream<Indexed<T>> indexed(Iterable<T> array, int offset) {
        return StreamSupport.stream(array.spliterator(), false).map(indexed(offset));
    }

    public static <T> Stream<Indexed<T>> indexed(Iterable<T> array) {
        return indexed(array, 0);
    }

    /**
     * Used to perform filtering for and casting to a given type
     * in a single call.
     * <p>
     * The standard idiom for filtering and casting is this:
     * {@snippet :
     * Stream<?> str; // ... something
     * Class<?> clz;  // ... given
     * str.filter(clz::isInstance).map(clz::cast);
     * }
     *
     * It's easy to get this wrong like so:
     * {@snippet :
     * str.filter(e -> e instanceof Number).map(Double.class::cast)
     * }
     * while the other way around things are at least not ill-behaved
     * {@snippet :
     * str.filter(e -> e instanceof Double).map(Number.class::cast)
     * }
     *
     * The function instantiated here returns a stream of the given input
     * value which is either a singleton or an empty stream.
     * The intended usage is therefore with a {@link Stream#flatMap(Function) flatMap}
     * mapping operation:
     * {@snippet :
     * str.flatMap(filterAndCast(Double.class))
     * }
     *
     * @param c the class to filter for and to cast the elements of the stream to
     * @return a function the wraps the given object into a singleton stream if it mathces
     * @param <T> the target type
     */
    public static <T> Function<Object, Stream<T>> filterAndCast(Class<T> c) {
        return obj -> c.isInstance(obj)
                ? Stream.of(obj).map(c::cast)
                : Stream.empty();
    }
}
