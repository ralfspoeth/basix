package io.github.ralfspoeth.basix.fn;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SignTest {

    @Test
    void testSmallerThan() {
        // given
        var data = List.of(1, 2, 3);
        // then
        assertAll(
                () -> assertEquals(List.of(1), data.stream().filter(Sign.smallerThan(2)).toList()),
                () -> assertEquals(List.of(1, 2), data.stream().filter(Sign.smallerOrEqual(2)).toList()),
                () -> assertEquals(List.of(2), data.stream().filter(Sign.equal(2)).toList()),
                () -> assertEquals(List.of(2, 3), data.stream().filter(Sign.greaterOrEqual(2)).toList()),
                () -> assertEquals(List.of(3), data.stream().filter(Sign.greaterThan(2)).toList()),
                () -> assertEquals(List.of(1, 3), data.stream().filter(Sign.smallerOrGreater(2)).toList()),
                () -> {}
        );
    }

}
