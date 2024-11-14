package io.github.ralfspoeth.basix.fn;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.github.ralfspoeth.basix.fn.Functions.labeled;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class LabeledTest {

    @Test
    void testLabeledMap(){
        // given
        var m = Map.of("hello", 1, "world", 2);
        // when
        var l = labeled(m).toList();
        // then
        assertAll(
                () -> assertEquals(1, l.stream().filter(li -> li.label().equals("hello")).findAny().orElseThrow().value()),
                () -> assertEquals(2, l.stream().filter(li -> li.label().equals("world")).findAny().orElseThrow().value()),
                () -> assertEquals(2, l.size())
        );
    }

    @Test
    void testLabeledList(){
        // given
        var l = List.of("hello", "world");
        // when
        var r = labeled(l, s -> s.substring(0, 1)).toList();
        // then
        assertAll(
                () -> assertEquals("h", r.getFirst().label()),
                () -> assertEquals("hello", r.getFirst().value()),
                () -> assertEquals("w", r.getLast().label()),
                () -> assertEquals("world", r.getLast().value())
        );
    }

}
