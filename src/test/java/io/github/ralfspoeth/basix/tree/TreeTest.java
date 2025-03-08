package io.github.ralfspoeth.basix.tree;

import org.junit.jupiter.api.Test;

import java.util.List;

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

    @Test
    void test1() {
        var tree = new Tree<Integer>();
        var one = tree.root().addLeafNode(1);
        assertAll(
                () -> assertEquals(1, one.data()),
                () -> assertEquals(1, tree.root().children().size()),
                () -> assertEquals(tree.root(), one.root()),
                () -> assertEquals(tree.root(), one.parent())
        );
    }

    @Test
    void testAddChildNode() {
        var tree = new Tree<Integer>();
        var child = tree.root().addChildNode();
        assertAll(
                () -> assertEquals(1, tree.root().children().size()),
                () -> assertEquals(tree.root(), child.parent()),
                () -> assertEquals(child, tree.root().children().getFirst())
        );
    }

    @Test
    void testRemoveLeaf() {
        var tree = new Tree<Integer>();
        var one = tree.root().addLeafNode(1);
        assertTrue(tree.root().remove(one));
        assertEquals(0, tree.root().children().size());
        assertFalse(tree.root().remove(one));
    }

    @Test
    void testRemoveChild() {
        var tree = new Tree<Integer>();
        var child = tree.root().addChildNode();
        assertTrue(tree.root().remove(child));
        assertEquals(0, tree.root().children().size());
        assertFalse(tree.root().remove(child));
    }

    @Test
    void testTreeStructure() {
        var tree = new Tree<String>();
        var root = tree.root();
        var child1 = root.addChildNode();
        var child2 = root.addChildNode();
        var leaf1 = child1.addLeafNode("leaf1");
        var leaf2 = child1.addLeafNode("leaf2");
        var leaf3 = child2.addLeafNode("leaf3");

        assertAll(
                () -> assertEquals(2, root.children().size()),
                () -> assertEquals(2, child1.children().size()),
                () -> assertEquals(1, child2.children().size()),
                () -> assertEquals("leaf1", leaf1.data()),
                () -> assertEquals("leaf2", leaf2.data()),
                () -> assertEquals("leaf3", leaf3.data()),
                () -> assertEquals(root, leaf1.root()),
                () -> assertEquals(child1, leaf1.parent()),
                () -> assertEquals(root, leaf3.root()),
                () -> assertEquals(child2, leaf3.parent())
        );
    }

    @Test
    void testRootOnChild(){
        var tree = new Tree<Integer>();
        var root = tree.root();
        var child1 = root.addChildNode();
        var leaf = child1.addLeafNode(1);

        assertAll(
                ()->assertEquals(root, child1.root()),
                ()->assertEquals(root, leaf.root())
        );

    }

    @Test
    void testComplex(){
        var tree = new Tree<String>();
        var root = tree.root();
        var child1 = root.addChildNode();
        var child2 = root.addChildNode();
        var child11 = child1.addChildNode();
        var leaf1 = child11.addLeafNode("L1");
        var leaf2 = child1.addLeafNode("L2");
        var child21 = child2.addChildNode();
        var leaf3 = child21.addLeafNode("L3");
        var leaf4 = child21.addLeafNode("L4");
        var child22 = child2.addChildNode();
        var leaf5 = child22.addLeafNode("L5");

        assertAll(
                ()->assertEquals(2, root.children().size()),
                ()->assertEquals(2, child1.children().size()),
                ()->assertEquals(2, child2.children().size()),
                ()->assertEquals(List.of(child11, leaf2), child1.children().stream().toList()),
                ()->assertEquals(List.of(child21, child22), child2.children().stream().toList()),
                ()->assertEquals(root, leaf1.root()),
                ()->assertEquals(child11, leaf1.parent()),
                ()->assertEquals(child1, leaf2.parent()),
                ()->assertEquals(root, leaf2.root()),
                ()->assertEquals(child2, child21.parent()),
                ()->assertEquals(child21, leaf3.parent()),
                ()->assertEquals(root, leaf3.root())
        );

    }

}