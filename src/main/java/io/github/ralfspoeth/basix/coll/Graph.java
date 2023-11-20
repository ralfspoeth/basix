package io.github.ralfspoeth.basix.coll;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public class Graph<T> {

    private final Set<T> nodes;
    private final Set<T> sources;
    private final Set<T> sinks;

    private Graph(Set<T> nodes, Set<T> sources, Set<T> sinks) {
        this.nodes = nodes;
        this.sources = sources;
        this.sinks = sinks;
    }

    public static <T>  Graph<T> of(Collection<T> elements, Function<T, Collection<T>> successors) {
        var nodes = Set.copyOf(elements);
        var sources = new HashSet<>(nodes);
        var sinks = new HashSet<>(nodes);
        return new Graph<>(nodes, sources, sinks);
    }

}
