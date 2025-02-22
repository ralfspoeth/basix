package io.github.ralfspoeth.basix.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.SequencedCollection;

final class ChildNode<T> extends AbstractNode<T> implements TreeNode<T> {
    private final List<TreeNode<T>> children = new ArrayList<>();

    @Override
    public SequencedCollection<TreeNode<T>> children() {
        return children;
    }

    LeafNode<T> addLeafNode(T data) {
        var n = new LeafNode<T>(data);
        n.parent = this;
        children.add(n);
        return n;
    }

    ChildNode<T> addChildNode() {
        var n = new ChildNode<T>();
        n.parent = this;
        children.add(n);
        return n;
    }

    boolean remove(TreeNode<?> n) {
        return children.remove(n);
    }
}
