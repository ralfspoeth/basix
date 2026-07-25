package io.github.ralfspoeth.basix.fn;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A {@link Function} composed of an ordered list of guarded {@link Case}s
 * plus a default function, mimicking a {@code switch} expression with
 * {@code when} clauses.
 * <p>
 * When {@link #apply(Object) applied}, the cases are evaluated in order and the
 * function of the first case whose predicate matches is applied to the argument;
 * if no case matches, the default function is applied instead. Example:
 * {@snippet :
 * var f = Switch.<Integer, String>builder()
 *         .when(i -> i == 0).then(_ -> "none")
 *         .when(i -> i == 1).then(_ -> "one")
 *         .otherwise(_ -> "many");
 * f.apply(0); // "none"
 * f.apply(7); // "many"
 * }
 * In target-typed positions such as {@link java.util.stream.Stream#map(Function) Stream.map}
 * the {@linkplain #Switch(List, Function) constructor} may be preferable
 * because the diamond operator infers the type arguments which the
 * fluent style above needs spelled out:
 * {@snippet :
 * import java.util.stream.Stream;
 * var words = Stream.of(0, 1, 7).map(new Switch<>(List.of(
 *         Case.of(i -> i == 0, _ -> "none"),
 *         Case.of(i -> i == 1, _ -> "one")
 * ), _ -> "many")).toList(); // ["none", "one", "many"]
 * }
 * Instances are immutable and thread-safe provided the given predicates
 * and functions are.
 *
 * @param <T> the type of the input to the function
 * @param <R> the type of the result of the function
 */
public class Switch<T, R> implements Function<T, R> {

    /**
     * A single case: a guarding predicate and the function to apply if it matches.
     *
     * @param when the guarding predicate; must not be {@code null}
     * @param then the function applied when {@code when} matches; must not be {@code null}
     * @param <T>  the type of the input
     * @param <R>  the type of the result
     */
    public record Case<T, R>(Predicate<T> when, Function<T, R> then) {

        /**
         * Canonical constructor rejecting {@code null} components.
         *
         * @throws NullPointerException if {@code when} or {@code then} is {@code null}
         */
        public Case {
            Objects.requireNonNull(when);
            Objects.requireNonNull(then);
        }

        /**
         * Factory method which - in contrast to the canonical constructor -
         * allows for contravariant predicates and functions.
         *
         * @param when the guarding predicate; must not be {@code null}
         * @param then the function applied when {@code when} matches; must not be {@code null}
         * @param <T>  the type of the input
         * @param <R>  the type of the result
         * @return a new {@link Case}
         */
        public static <T, R> Case<T, R> of(Predicate<? super T> when,
                                           Function<? super T, ? extends R> then) {
            return new Case<>(when::test, then::apply);
        }
    }

    private final List<Case<T, R>> cases;
    private final Function<T, R> defaultFunction;

    /**
     * Creates a switching function from the given cases and default function.
     * The list of cases is copied; later modifications to it have no effect.
     *
     * @param cases           the cases, evaluated in the given order; must not be
     *                        {@code null} nor contain {@code null} elements, may be empty
     * @param defaultFunction the function applied when no case matches; must not be {@code null}
     * @throws NullPointerException if any argument or element is {@code null}
     */
    public Switch(List<Case<T, R>> cases,
                  Function<? super T, ? extends R> defaultFunction)
    {
        this.cases = List.copyOf(cases);
        this.defaultFunction = defaultFunction::apply;
    }

    /**
     * Starts building a {@link Switch} in source order — cases first,
     * default last, each case a {@code when}/{@code then} pair:
     * {@snippet :
     * var f = Switch.<Integer, String>when(i -> i < 0).then(_ -> "negative")
     *         .when(i -> i > 0).then(_ -> "positive")
     *         .otherwise(_ -> "zero");
     * }
     * Neither the returned {@link Builder.Stub} nor the {@link Builder} is
     * a function; only {@link Builder#otherwise(Function)} produces one,
     * just as a {@code switch} expression is complete only with its
     * {@code default} arm.
     * <p>
     * Note that when the type arguments must be spelled out — as in the
     * example above — starting with {@link #builder()} usually reads
     * better than wedging the witnesses into the first {@code when}.
     *
     * @param when the guarding predicate of the first case; must not be {@code null}
     * @param <T>  the type of the input
     * @param <R>  the type of the result
     * @return the first case under construction, to be completed with
     *         {@link Builder.Stub#then(Function)}
     * @throws NullPointerException if {@code when} is {@code null}
     */
    public static <T, R> Builder<T, R>.Stub<T> when(Predicate<? super T> when) {
        return new Builder<T, R>().when(when);
    }

    /**
     * Compact form of {@link #when(Predicate)} followed by
     * {@link Builder.Stub#then(Function)}; the two are equivalent:
     * {@snippet :
     * var f = Switch.<Integer, String>when(i -> i < 0, _ -> "negative")
     *         .when(i -> i > 0).then(_ -> "positive")
     *         .otherwise(_ -> "zero");
     * }
     *
     * @param when the guarding predicate of the first case; must not be {@code null}
     * @param then the function applied when {@code when} matches; must not be {@code null}
     * @param <T>  the type of the input
     * @param <R>  the type of the result
     * @return a builder holding the first case
     * @throws NullPointerException if {@code when} or {@code then} is {@code null}
     */
    public static <T, R> Builder<T, R> when(Predicate<? super T> when,
                                            Function<? super T, ? extends R> then)
    {
        return Switch.<T, R>when(when).then(then);
    }

    /**
     * Creates an empty {@link Builder}; the preferred entry point whenever
     * the type arguments must be spelled out — which, since inference does
     * not flow backwards through a method chain, is nearly always the case
     * for the fluent style. The witnesses read better on {@code builder()}
     * than wedged into the first {@code when}:
     * {@snippet :
     * var f = Switch.<Object, String>builder()
     *         .when(Integer.class).then(i -> "int:" + i)   // i is an Integer
     *         .when(String.class).then(s -> "str:" + s.length())
     *         .otherwise(x -> "other:" + x);
     * }
     * It is also the entry point for type-guarded cases, where the instance
     * method {@link Builder#when(Class)} infers the case type from its
     * argument.
     *
     * @param <T> the type of the input
     * @param <R> the type of the result
     * @return a fresh, empty builder
     */
    public static <T, R> Builder<T, R> builder() {
        return new Builder<>();
    }

    /**
     * Accumulates {@link Case}s in source order until
     * {@link #otherwise(Function)} completes them with the default function
     * to a {@link Switch}. Obtained via {@link Switch#when(Predicate)}.
     * <p>
     * The builder may be reused after calling {@code otherwise}; the
     * {@link Switch} instances snapshot the cases accumulated so far.
     *
     * @param <T> the type of the input
     * @param <R> the type of the result
     */
    public static final class Builder<T, R> {

        private final List<Case<T, R>> cases = new ArrayList<>();

        // instantiated through Switch.when only
        private Builder() {
        }

        /**
         * Starts a case with its guarding predicate; cases are evaluated
         * in the order they are added.
         *
         * @param when the guarding predicate; must not be {@code null}
         * @return a fresh {@link Stub} holding the predicate, to be
         *         completed with {@link Stub#then(Function)}
         * @throws NullPointerException if {@code when} is {@code null}
         */
        public Stub<T> when(Predicate<? super T> when) {
            return new Stub<>(when, x -> x);
        }

        /**
         * Compact form of {@link #when(Predicate)} followed by
         * {@link Stub#then(Function)}; the two are equivalent.
         *
         * @param when the guarding predicate; must not be {@code null}
         * @param then the function applied when {@code when} matches; must not be {@code null}
         * @return {@code this}
         * @throws NullPointerException if {@code when} or {@code then} is {@code null}
         */
        public Builder<T, R> when(Predicate<? super T> when,
                                  Function<? super T, ? extends R> then)
        {
            return when(when).then(then);
        }

        /**
         * Starts a type-guarded case: it matches when the argument is an
         * instance of the given type, and — in contrast to the
         * predicate-guarded {@link #when(Predicate)} — narrows the argument,
         * so that the function passed to {@link Stub#then(Function)}
         * receives the already-cast value, mirroring a
         * {@code case Integer i -> ...} type pattern:
         * {@snippet :
         * var f = Switch.<Object, String>builder()
         *         .when(Integer.class).then(i -> "int:" + i)   // i is an Integer
         *         .when(String.class).then(s -> "str:" + s.length())
         *         .otherwise(x -> "other:" + x);
         * }
         * Note that {@code null} never matches, since
         * {@link Class#isInstance(Object)} is {@code false} for {@code null}.
         *
         * @param type the type to test for and to cast to; must not be {@code null}
         * @param <U>  the type of the case
         * @return a fresh {@link Stub} holding the type, to be
         *         completed with {@link Stub#then(Function)}
         * @throws NullPointerException if {@code type} is {@code null}
         */
        public <U> Stub<U> when(Class<U> type) {
            return new Stub<>(type::isInstance, type::cast);
        }

        /**
         * Completes the cases with the default function applied when
         * no case matches.
         *
         * @param defaultFunction the default function; must not be {@code null}
         * @return a new {@link Switch}
         * @throws NullPointerException if {@code defaultFunction} is {@code null}
         */
        public Switch<T, R> otherwise(Function<? super T, ? extends R> defaultFunction) {
            return new Switch<>(cases, defaultFunction);
        }

        /**
         * Same as {@link #otherwise(Function)} with a constant result;
         * {@code otherwiseValue(r)} is equivalent to {@code otherwise(_ -> r)}.
         *
         * @param value the result when no case matches
         * @return a new {@link Switch}
         */
        public Switch<T, R> otherwiseValue(R value) {
            return otherwise(_ -> value);
        }

        /**
         * A case under construction: the guard is fixed, along with a
         * narrowing step applied to the argument before it is passed to
         * the matching function. For predicate-guarded cases
         * ({@link Builder#when(Predicate)}) the narrowing step is the
         * identity and {@code U} equals {@code T}; for type-guarded cases
         * ({@link Builder#when(Class)}) it is the cast to the case type
         * {@code U}, so the matching function receives the already-cast
         * value, mirroring a {@code case Integer i -> ...} type pattern.
         * <p>
         * {@link #then(Function)} completes the {@link Case}, adds it to
         * the enclosing builder, and returns that builder.
         *
         * @param <U> the narrowed argument type of the case
         */
        public final class Stub<U> {

            private final Predicate<? super T> guard;
            private final Function<? super T, ? extends U> narrow;

            private Stub(Predicate<? super T> guard, Function<? super T, ? extends U> narrow) {
                this.guard = Objects.requireNonNull(guard);
                this.narrow = Objects.requireNonNull(narrow);
            }

            /**
             * Completes the case with the function applied — to the
             * narrowed argument — when the guard matches.
             *
             * @param then the matching function; must not be {@code null}
             * @return the enclosing builder, for the next case or
             *         {@link Builder#otherwise(Function)}
             * @throws NullPointerException if {@code then} is {@code null}
             */
            public Builder<T, R> then(Function<? super U, ? extends R> then) {
                Objects.requireNonNull(then);
                cases.add(new Case<>(guard::test, x -> then.apply(narrow.apply(x))));
                return Builder.this;
            }

            /**
             * Same as {@link #then(Function)} with a constant result;
             * {@code thenValue(r)} is equivalent to {@code then(_ -> r)}.
             *
             * @param value the result when the guard matches
             * @return the enclosing builder, for the next case or
             *         {@link Builder#otherwise(Function)}
             */
            public Builder<T, R> thenValue(R value) {
                return then(_ -> value);
            }
        }
    }

    /**
     * Applies the function of the first case whose predicate matches {@code e},
     * or the default function if none matches.
     *
     * @param e the function argument
     * @return the result of the selected function
     */
    @Override
    public final R apply(T e) {
        for(var c : cases) {
            if(c.when.test(e)) {
                return c.then.apply(e);
            }
        }
        return defaultFunction.apply(e);
    }
}
