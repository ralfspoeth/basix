package io.github.ralfspoeth.basix.tree;

import static java.util.Objects.requireNonNull;

final class LeafNode<T> extends AbstractNode<T> implements TreeNode<T> {
    private final T data;
    LeafNode(T data) {
        this.data = requireNonNull(data);
    }
}
