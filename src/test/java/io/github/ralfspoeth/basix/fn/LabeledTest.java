package io.github.ralfspoeth.basix.fn;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.github.ralfspoeth.basix.fn.Functions.labeled;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class LabeledTest {

    @Test
    void testLabeled(){
        var m = Map.of("hello", 1, "world", 2);
        assertAll(
                () -> assertEquals(1, labeled(m).filter(l -> l.label().equals("hello")).findAny().orElseThrow().value()),
                () -> assertEquals(2, labeled(m).filter(l -> l.label().equals("world")).findAny().orElseThrow().value()),
                () -> assertEquals(2, labeled(m).count())
        );
    }
}
