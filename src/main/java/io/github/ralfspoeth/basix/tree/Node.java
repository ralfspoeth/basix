package io.github.ralfspoeth.basix.tree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static java.util.Objects.requireNonNull;

class Node<T> {
    private final T data;
    private Node<T> parent;
    private final List<Node<T>> children = new ArrayList<>();

    Node(T data) {
        this.data = requireNonNull(data);
    }

    Node<T> addChild(T data) {
        var n = new Node<T>(data);
        n.parent = this;
        children.add(n);
        return this;
    }

    boolean removeChild(T data) {
        Iterator<Node<T>> iterator = children.iterator();
        while(iterator.hasNext()) {
            if(iterator.next().data.equals(data)) {
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    Node<T> root() {
        return parent==null?this:parent.root();
    }
}
