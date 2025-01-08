package io.github.ralfspoeth.basix.tree;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Tree<T> {

    private Node<T> root;

    private final Map<T, Node<T>> index = new HashMap<>();

    public Tree<T> addNode(T data) {
        var newNode = new Node<>(data);
        index.put(data, newNode);
        return this;
    }

    public Set<T> data() {
        return index.keySet();
    }


}
