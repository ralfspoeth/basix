package io.github.ralfspoeth.basix.tree;

public class Tree<T> {

    private final ChildNode<T> root = new ChildNode<>();

    public TreeNode<T> root() {
        return root;
    }

}
