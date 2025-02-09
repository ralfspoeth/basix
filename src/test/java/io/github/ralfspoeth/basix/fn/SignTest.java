package io.github.ralfspoeth.basix.fn;

import org.junit.jupiter.api.Test;

import java.util.List;

import static io.github.ralfspoeth.basix.fn.Predicates.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SignTest {

    @Test
    void testComparisons() {
        // given
        var data = List.of(1, 2, 3);
        // then
        assertAll(
                () -> assertEquals(List.of(1), data.stream().filter(smallerThan(2)).toList()),
                () -> assertEquals(List.of(1, 2), data.stream().filter(smallerOrEqual(2)).toList()),
                () -> assertEquals(List.of(2), data.stream().filter(equal(2)).toList()),
                () -> assertEquals(List.of(2, 3), data.stream().filter(greaterOrEqual(2)).toList()),
                () -> assertEquals(List.of(3), data.stream().filter(greaterThan(2)).toList()),
                () -> assertEquals(List.of(1, 3), data.stream().filter(smallerOrGreater(2)).toList()),
                () -> {}
        );
    }

}
