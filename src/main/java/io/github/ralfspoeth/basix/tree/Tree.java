package io.github.ralfspoeth.basix.tree;

public class Tree<T> {

    private final ChildNode<T> root = new ChildNode<>();

    public ChildNode<T> root() {
        return root;
    }

}
