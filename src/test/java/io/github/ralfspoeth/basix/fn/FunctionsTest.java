package io.github.ralfspoeth.basix.fn;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.github.ralfspoeth.basix.fn.Functions.*;
import static org.junit.jupiter.api.Assertions.*;

class FunctionsTest {

    @Test
    void testConditional() {
        var l = List.of(1, 2, 3);
        var evenSquared = l.stream().map(conditional(i -> i%2==0, i -> i*i)).toList();
        assertEquals(List.of(1, 4, 3), evenSquared);
    }

    @Test
    void testOfMapAndExtrFunc() {
        var m = Map.of(1, "one", 2, "two");
        record Int(int x) {
        }
        var x = new Int(1);
        var y = new Int(3);
        var f = Functions.of(m, Int::x);
        assertAll(
                () -> assertEquals("one", f.apply(x)),
                () -> assertNull(f.apply(y))
        );
    }

    @Test
    void testOfInt() {
        var l = List.of("one", "two", "three");
        var f = Functions.of(l);
        assertAll(
                () -> assertEquals("three", f.apply(2)),
                () -> assertThrows(Exception.class, ()->f.apply(-1)),
                () -> assertThrows(Exception.class, ()->f.apply(3))
        );
    }

    @Test
    void testLabeled() {
        record Comp(String name, int age) {}
        var compList = List.of(new Comp("Ada", 50), new Comp("Lisp", 90), new Comp("Java", 30));
        System.out.println(compList);
        var labeledCompList = labeled(compList, Comp::name)
                .map(lv -> lv.modLabel(String::toUpperCase)) // modify label to upper-case
                .map(lv -> lv.modValue(comp -> new Comp(comp.name, comp.age-10))) // modify value subtracting 10 from the age
                .toList();
        System.out.println(labeledCompList);
        var llList = labeledCompList.stream().map(label(lsc -> lsc.label() + "_" + lsc.label())).toList();
        System.out.println(llList);
        var orig = llList.stream()
                .map(llv -> llv.value().value())
                .map(comp -> new Comp(comp.name, comp.age+10))
                .toList();
        System.out.println(orig);
        assertEquals(compList, orig);
    }
}