package io.github.ralfspoeth.basix.coll;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ConcurrentStackTest {

    @Test
    void testParallelInserts() {
        final int parallel = Runtime.getRuntime().availableProcessors();
        final int num = 1_024;
        final var startCounter = new AtomicInteger(0);
        var stack = new ConcurrentStack<Integer>();
        try (var es = Executors.newFixedThreadPool(parallel)) {
            for (int i = 0; i < parallel; i++) {
                es.submit(() -> {
                    int start = startCounter.getAndIncrement() * num;
                    for (int j = start; j < start + num; j++) {
                        stack.push(j);
                    }
                });
            }
        }
        int expectedElems = num * parallel;
        int calls = 0;
        var result = new ArrayList<Integer>();
        while (!stack.isEmpty()) {
            result.add(stack.pop());
            calls++;
        }
        int c = calls;
        assertAll(
                () -> assertEquals(expectedElems, c),
                () -> assertEquals(expectedElems, result.size())
        );
    }

    @Test
    void testParallelInsertsParallelRemovals() {
        final int parallel = Runtime.getRuntime().availableProcessors();
        var stack = new ConcurrentStack<Integer>();
        var recover = new ConcurrentLinkedQueue<Integer>();
        int num = 17_234;
        try (var es = Executors.newFixedThreadPool(parallel * 2)) {
            for (int i = 0; i < parallel; i++) {
                es.submit(() -> {
                    while (!stack.isEmpty()) stack.popIfNotEmpty().ifPresent(recover::add);
                });
                es.submit(() -> {
                    for (int j = 0; j < num; j++) {
                        stack.push(ThreadLocalRandom.current().nextInt());
                    }
                });
            }
        }
        int cntLeft = 0;
        while (!stack.isEmpty()) {
            cntLeft++;
            stack.pop();
        }
        int count = cntLeft;
        //System.out.printf("left %d, reclaimed %d%n", cntLeft, recover.size());
        assertAll(
                () -> assertTrue(count >= 0),
                () -> assertTrue(recover.size() <= num * parallel)
        );
    }
}
