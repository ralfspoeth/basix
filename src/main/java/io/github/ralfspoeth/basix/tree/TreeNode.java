package io.github.ralfspoeth.basix.tree;

import java.util.List;
import java.util.SequencedCollection;

public sealed interface TreeNode<T> permits LeafNode, ChildNode {
    default TreeNode<T> root() {
        return parent()==null?this:parent().root();
    }

    default SequencedCollection<TreeNode<T>> children() {
        return List.of();
    }

    TreeNode<T> parent();

    default T data() {
        return null;
    }
}
