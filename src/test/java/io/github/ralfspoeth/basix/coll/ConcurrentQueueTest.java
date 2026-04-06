package io.github.ralfspoeth.basix.coll;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ConcurrentQueueTest {

    @Test
    void testParallelInsertsThenRemovals() {
        final int parallel = Runtime.getRuntime().availableProcessors();
        final int num = 1_024;
        var cnt = new AtomicInteger(0);
        var q = new ConcurrentQueue<@NonNull Integer>();
        try (var es = Executors.newFixedThreadPool(parallel)) {
            for (int i = 0; i < parallel * 4; i++) {
                es.submit(() -> {
                    int start = cnt.getAndIncrement() * num;
                    for (int j = start; j < start + num; j++) {
                        q.add(j);
                    }
                });
            }
        }
        int expectedElems = num * cnt.get();
        var s = HashSet.<Integer>newHashSet(expectedElems);
        int calls = 0;
        while (!q.isEmpty()) {
            s.add(q.remove());
            calls++;
        }
        int c = calls;
        assertAll(
                () -> assertEquals(expectedElems, c),
                () -> assertEquals(expectedElems, s.size())
        );
    }

    @Test
    void someBasics() {
        var q = new ConcurrentQueue<Integer>();
        assertTrue(q.isEmpty());
        q.add(1);
        assertFalse(q.isEmpty());
        assertEquals(1, q.head().orElseThrow());
        assertEquals(1, q.tail().orElseThrow());
        q.add(2);
        assertEquals(1, q.head().orElseThrow());
        assertEquals(2, q.tail().orElseThrow());
        q.add(3);
        assertEquals(1, q.head().orElseThrow());
        assertEquals(3, q.tail().orElseThrow());
        // removeing things...
        assertEquals(1, q.remove());
        assertEquals(2, q.remove());
        assertEquals(3, q.remove());
        // should then be empty
        assertTrue(q.isEmpty());
    }

    @Test
    void someBasicsParallel() {
        var q = new ConcurrentQueue<Integer>();
        assertTrue(q.isEmpty());
        AtomicInteger cnt = new AtomicInteger();
        try(var es = Executors.newCachedThreadPool()) {
            es.submit(() -> {
                cnt.getAndIncrement();
                q.add(ThreadLocalRandom.current().nextInt());
            });
        }
        int k = 0;
        while(!q.isEmpty()) {k++;q.remove();}
        assertEquals(cnt.get(), k);
    }
}
