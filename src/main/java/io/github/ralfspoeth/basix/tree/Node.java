package io.github.ralfspoeth.basix.tree;

import java.util.Collection;
import java.util.List;

public sealed interface Node<T> permits LeafNode, ChildNode {
    Node<T> parent();
    default Node<T> root() {
        return parent()==null?this:parent().root();
    }
    default Collection<Node<T>> children() {
        return List.of();
    }
}
