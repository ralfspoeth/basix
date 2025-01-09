package io.github.ralfspoeth.basix.tree;

sealed abstract class AbstractNode<T> permits LeafNode, ChildNode {
    protected TreeNode<T> parent;

    public TreeNode<T> parent() {
        return parent;
    }
}
