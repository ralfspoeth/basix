package io.github.ralfspoeth.basix.fn;

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
 * var f = SwitchingFunction.<Integer, String>of(
 *         i -> "many",                          // default
 *         Case.of(i -> i == 0, i -> "none"),
 *         Case.of(i -> i == 1, i -> "one")
 * );
 * f.apply(0); // "none"
 * f.apply(7); // "many"
 * }
 * Instances are immutable and thread-safe provided the given predicates
 * and functions are.
 *
 * @param <T> the type of the input to the function
 * @param <R> the type of the result of the function
 */
public class SwitchingFunction<T, R> implements Function<T, R> {

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
    public SwitchingFunction(List<Case<T, R>> cases,
                             Function<? super T, ? extends R> defaultFunction)
    {
        this.cases = List.copyOf(cases);
        this.defaultFunction = defaultFunction::apply;
    }

    /**
     * Varargs factory method; equivalent to
     * {@link #SwitchingFunction(List, Function) new SwitchingFunction(List.of(cases), defaultFunction)}.
     *
     * @param defaultFunction the function applied when no case matches; must not be {@code null}
     * @param cases           the cases, evaluated in the given order; must not contain {@code null}
     * @param <T>             the type of the input
     * @param <R>             the type of the result
     * @return a new {@link SwitchingFunction}
     * @throws NullPointerException if any argument or element is {@code null}
     */
    @SafeVarargs
    public static <T, R> SwitchingFunction<T, R> of(Function<? super T, ? extends R> defaultFunction,
                                                    Case<T, R>... cases)
    {
        return new SwitchingFunction<>(List.of(cases), defaultFunction);
    }

    /**
     * Applies the function of the first case whose predicate matches {@code e},
     * or the default function if none matches.
     *
     * @param e the function argument
     * @return the result of the selected function
     */
    @Override
    public R apply(T e) {
        return cases.stream()
                .filter(c -> c.when().test(e))
                .findFirst()
                .map(Case::then)
                .orElse(defaultFunction)
                .apply(e);
    }
}
