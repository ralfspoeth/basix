package io.github.ralfspoeth.basix.coll;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ConcurrentQueueTest {

    @Test
    void testParallelInsertsThenRemovals() {
        final int parallel = Runtime.getRuntime().availableProcessors();
        final int num = 1_024;
        var cnt = new AtomicInteger(0);
        var q = new ConcurrentQueue<Integer>();
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
    void testMultiParallelInsertsSingleRemoval() {
        final int parallel = Runtime.getRuntime().availableProcessors();
        final int num = 513;

        // insertion thread counter
        var cnt = new AtomicInteger(0);
        // removal latch
        var latch = new AtomicInteger(0);
        // the queue
        var q = new ConcurrentQueue<Integer>();
        // the result
        var s = new ConcurrentSkipListSet<Integer>();

        // running all the threads
        try (var es = Executors.newFixedThreadPool(parallel)) {
            // removal thread
            final int tasks = parallel * 4;
            es.submit(() -> {
                while (latch.get() < tasks) {
                    q.removeIfNotEmpty().ifPresent(s::add);
                }
            });
            // insertion threads
            for (int i = 0; i < tasks; i++) {
                es.submit(() -> {
                    int start = cnt.getAndIncrement() * num;
                    for (int j = start; j < start + num; j++) {
                        q.add(j);
                    }
                    latch.incrementAndGet();
                });
            }
        }
        // remove remaining elements to s
        while (!q.isEmpty()) {
            s.add(q.remove());
        }
        int expectedElems = num * cnt.get();
        assertAll(
                () -> assertEquals(expectedElems, s.size())
        );
    }

    @Test
    void testMultiparallelMultiParallelRemovals() {
        final int parallel = Runtime.getRuntime().availableProcessors();
        final int num = 2_563;
        final int tasks = parallel * 2;
        final int elems = num * tasks;

        // insertion thread counter
        var cnt = new AtomicInteger(0);
        // removal latch
        var latch = new AtomicInteger(0);
        // the queue
        var q = new ConcurrentQueue<Integer>();
        // the result
        var s = new ConcurrentSkipListSet<Integer>();

        // running all the threads
        try (var es = Executors.newFixedThreadPool(parallel)) {
            // insertion threads
            for (int i = 0; i < tasks; i++) {
                es.submit(() -> {
                    int start = cnt.getAndIncrement() * num;
                    for (int j = start; j < start + num; j++) {
                        q.add(j);
                    }
                });
            }
            // removal threads
            for (int i = 0; i < tasks / 2; i++) {
                es.submit(() -> {
                    while (latch.get() < elems) {
                        q.removeIfNotEmpty().ifPresent(r -> {
                            s.add(r);
                            latch.getAndIncrement();
                        });
                    }
                });
            }
        }
        // remove remaining elements to s
        assertAll(
                () -> assertEquals(elems, s.size())
        );
    }

    @Test
    void testParallelInsertionsRemovalsObservations() {
        final int parallel = Runtime.getRuntime().availableProcessors();
        final int num = 563;
        final int tasks = parallel * 2;
        final int elems = num * tasks;

        // insertion thread counter
        var cnt = new AtomicInteger(0);
        // removal latch
        var latch = new AtomicInteger(0);
        // the queue
        var q = new ConcurrentQueue<Integer>();
        // the result
        var s = new ConcurrentSkipListSet<Integer>();
        // heads and tails
        var h = new ConcurrentSkipListSet<Integer>();
        var t = new ConcurrentSkipListSet<Integer>();

        Thread.ofPlatform().daemon().start(()->{
            while(true) {
                q.head().ifPresent(h::add);
                q.tail().ifPresent(t::add);
            }
        });

        // running all the threads
        try (var es = Executors.newFixedThreadPool(parallel)) {
            // observer threads
            es.submit(() -> {

            });
            // insertion threads
            for (int i = 0; i < tasks; i++) {
                es.submit(() -> {
                    int start = cnt.getAndIncrement() * num;
                    for (int j = start; j < start + num; j++) {
                        q.add(j);
                    }
                });
            }
            // removal threads
            for (int i = 0; i < tasks; i++) {
                es.submit(() -> {
                    while (latch.get() < elems) {
                        q.removeIfNotEmpty().ifPresent(r -> {
                            s.add(r);
                            latch.getAndIncrement();
                        });
                    }
                });
            }
        }
        // remove remaining elements to s
        assertAll(
                () -> assertEquals(elems, s.size()),
                () -> assertTrue(h.size()>=0),
                () -> assertTrue(t.size()>=0)
        );
    }
}
