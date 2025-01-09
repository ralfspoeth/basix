package io.github.ralfspoeth.basix.tree;

import java.util.ArrayList;
import java.util.List;

final class ChildNode<T> extends AbstractNode<T> implements TreeNode<T> {
    private final List<TreeNode<T>> children = new ArrayList<>();

    LeafNode<T> addLeadNode(T data) {
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
