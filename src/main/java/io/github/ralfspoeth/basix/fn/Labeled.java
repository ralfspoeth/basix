package io.github.ralfspoeth.basix.fn;

/**
 * A pair of a label and a value, typically produced by
 * {@link Functions#labeled(java.util.Map)} or
 * {@link Functions#labeled(Iterable, java.util.function.Function)}.
 * <p>
 * Unlike {@link java.util.Map.Entry}, the label is not required to be
 * unique within a stream; it serves as a tag rather than as an identifier.
 *
 * @param label the label attached to the value
 * @param value the wrapped value
 * @param <L>   the type of the label
 * @param <T>   the type of the wrapped value
 */
public record Labeled<L, T>(L label, T value) {}
