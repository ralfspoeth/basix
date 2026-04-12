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
        // given: some constants, the sequenceNumber, and the concurrent queue
        final int parallel = Runtime.getRuntime().availableProcessors() * 4;
        final int numPerProcessor = 1_024<<4;
        final var sequenceNumber = new AtomicInteger(0);
        final var queue = new ConcurrentQueue<@NonNull Integer>();
        // when we got as many executors as we got processors...
        // each of them shall add elements to the queue concurrently
        try (var es = Executors.newFixedThreadPool(parallel)) {
            for (int i = 0; i < parallel; i++) {
                es.submit(() -> {
                    int start = sequenceNumber.getAndIncrement() * numPerProcessor;
                    for (int j = start; j < start + numPerProcessor; j++) {
                        queue.add(j);
                    }
                });
            }
        }
        // then
        int expectedElems = numPerProcessor * parallel;
        // will collect the distinct set of elements
        var s = HashSet.<Integer>newHashSet(expectedElems);
        // count the additions to the queue
        // by moving them into a set
        int calls = 0;
        while (!queue.isEmpty()) {
            s.add(queue.remove());
            calls++;
        }
        final int c = calls; // necessary with assertAll
        // both the number of calls and the elements in the distinct set must equal #procs X
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
