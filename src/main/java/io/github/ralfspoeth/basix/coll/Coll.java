package io.github.ralfspoeth.basix.coll;

sealed interface Coll permits Queue, Stack {
    boolean isEmpty();
}
