package io.github.ralfspoeth.basix.fn;

/**
 * A pair of an integer index and a value, typically produced by
 * {@link Functions#indexed(int)} or {@link Functions#indexed(Iterable, int)}
 * to attach an ordinal position to elements of a stream.
 *
 * @param index the index attached to the value
 * @param value the wrapped value
 * @param <T>   the type of the wrapped value
 */
public record Indexed<T>(int index, T value) {}
