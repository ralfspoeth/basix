package io.github.ralfspoeth.basix.tree;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TreeTest {

    @Test
    void testNew() {
        var tree = new Tree<Integer>();
        assertAll(
                () -> assertEquals(0, tree.root().children().size()),
                () -> assertEquals(tree.root(), tree.root().root()),
                () -> assertNull(tree.root().parent())
        );
    }

}