package net.nemezanevem.gregtech.api.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;


public class SizedArrayList<E> extends ArrayList<E> {

    private final int maxSize;

    public SizedArrayList(int maxSize) {
        super(maxSize);
        this.maxSize = maxSize;
    }

    public SizedArrayList(int maxSize, E... fill) {
        super(fill.length > maxSize ? Arrays.stream(fill).limit(maxSize).toList() : Arrays.asList(fill));
        this.maxSize = maxSize;
    }

    public int getMaxSize() {
        return maxSize;
    }

    /**
     * Appends the specified element to the end of this list.
     *
     * @param e element to be appended to this list
     * @return {@code true} (as specified by {@link Collection#add})
     */
    public boolean add(E e) {
        if(this.size() > this.getMaxSize()) return false;
        return super.add(e);
    }

    /**
     * Inserts the specified element at the specified position in this
     * list. Shifts the element currently at that position (if any) and
     * any subsequent elements to the right (adds one to their indices).
     *
     * @param index index at which the specified element is to be inserted
     * @param element element to be inserted
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public void add(int index, E element) {
        if(index >= getMaxSize()) return;
        super.add(index, element);
    }

    public void fill(E element) {
        for (int i = 0; i < this.getMaxSize(); ++i) {
            this.add(i, element);
        }
    }
}
