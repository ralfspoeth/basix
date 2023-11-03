package io.github.ralfspoeth.basix.fn;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class IndexedTest {

    @Test
    void testIndexedWithOffset() {
        var s = Stream.of(1, 2, 3);
        s.map(Functions.indexed(1)).forEach(i -> assertEquals(i.index(),i.value()));
    }

    @Test
    void testIndexedWithoutOffset() {
        var s = Stream.of(1, 2, 3);
        s.map(Functions.indexed(0)).forEach(i -> assertEquals(i.index()+1, i.value()));
    }

    @Test
    void testIndexedStreamWithOffset() {
        var data = Arrays.asList(1, 2, 3);
        Functions.indexed(data, 5).forEach(i -> assertEquals(i.index() - 4, i.value()));
    }

    @Test
    void testIndexedStreamWithoutOffset() {
        var data = List.of(7);
        var indexedSet = Functions.indexed(data).collect(Collectors.toSet());
        var indexedOffSet = Functions.indexed(data, 7).collect(Collectors.toSet());
        assertAll(
                () -> assertEquals(1, indexedSet.size()),
                () -> assertTrue(indexedSet.contains(new Indexed<>(0, 7))),
                () -> assertTrue(indexedOffSet.contains(new Indexed<>(7, 7)))
        );
    }
}
