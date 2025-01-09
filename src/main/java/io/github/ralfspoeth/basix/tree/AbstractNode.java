package io.github.ralfspoeth.basix.tree;

sealed abstract class AbstractNode<T> permits LeafNode, ChildNode {
    protected Node<T> parent;

    public Node<T> parent() {
        return parent;
    }
}
