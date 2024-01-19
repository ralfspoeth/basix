package io.github.ralfspoeth.basix.fn;

import java.util.function.Function;

public record Labeled<L, T>(L label, T value) {

    public <U> Labeled<L, U> modValue(Function<T, U> modification) {
        return new Labeled<>(label, modification.apply(value));
    }

    public <M> Labeled<M, T> modLabel(Function<L, M> modification) {
        return new Labeled<>(modification.apply(label), value);
    }
}
