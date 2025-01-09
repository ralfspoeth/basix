package io.github.ralfspoeth.basix.tree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static java.util.Objects.requireNonNull;

final class LeafNode<T> extends AbstractNode<T> implements Node<T> {
    private final T data;
    LeafNode(T data) {
        this.data = requireNonNull(data);
    }
}
