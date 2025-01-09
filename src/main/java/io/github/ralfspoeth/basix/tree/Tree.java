package io.github.ralfspoeth.basix.tree;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Tree<T> {

    private final ChildNode<T> root = new ChildNode<>();

    public Node<T> root() {
        return root;
    }

}
