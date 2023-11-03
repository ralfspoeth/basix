package io.github.ralfspoeth.basix.coll;

sealed interface Coll permits Queue, Ring, Stack {
    boolean isEmpty();
}
