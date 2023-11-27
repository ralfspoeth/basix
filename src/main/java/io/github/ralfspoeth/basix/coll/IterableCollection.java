package io.github.ralfspoeth.basix.coll;

import java.util.Iterator;

public interface IterableCollection<T> extends Iterable<T>{
    boolean isEmpty();
    Iterator<T> iterator();
}
