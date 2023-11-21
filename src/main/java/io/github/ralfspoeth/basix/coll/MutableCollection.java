package io.github.ralfspoeth.basix.coll;

import java.util.Collection;

public interface MutableCollection<T> {

    /**
     * Add a new item to the collection.
     *
     * @param item the item to be added
     * @return this
     */
    MutableCollection<T> add(T item);

    /**
     * Remove an item and return this
     * collection minus that item.
     *
     * If the item is not in this collection,
     * this collection is returned unchanged.
     *
     * @param item the item to be removed
     * @return this
     */
    MutableCollection<T> remove(T item);

    /**
     * Remove an item if it exists; returning
     * {@code null} if not.
     *
     * @param item the item to be removed
     * @return the item if part of the collection; {@code null} if not.
     */
    T removeIfExists(T item);

    default MutableCollection<T> addAll(Collection<? extends T> items) {
        items.forEach(this::add);
        return this;
    }

    default MutableCollection<T> removeAll(Collection<? extends T> items) {
        items.forEach(this::remove);
        return this;
    }

    MutableCollection<T> retainAll(Collection<? extends T> items);

    /**
     * Removes all items.
     *
     * @return this, being empty.
     */
    MutableCollection<T> clear();
}
