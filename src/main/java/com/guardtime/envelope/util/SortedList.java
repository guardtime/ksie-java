package com.guardtime.envelope.util;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Keeps all records in list sorted. Performs sorting at every insert.
 * Ignores duplicates.
 * @param <E>  List entry type
 *
 * NB! Does not allow inserting at specified index!
 */
public class SortedList<E extends Comparable> extends ArrayList<E> {

    public SortedList(int i) {
        super(i);
    }

    public SortedList() {
        super();
    }

    public SortedList(Collection<? extends E> collection) {
        super(collection);
        sort();
    }

    @Override
    public E set(int i, E e) {
        throw new NotImplementedException();
    }

    @Override
    public boolean add(E e) {
        if(contains(e)) {
            return false;
        }
        if (super.add(e)) {
            sort();
            return true;
        }
        return false;
    }

    @Override
    public void add(int i, E e) {
        throw new NotImplementedException();
    }

    @Override
    public boolean addAll(Collection<? extends E> collection) {
        Collection<E> toAdd = new ArrayList<>();
        for (E e : collection) {
            if (!contains(e)) {
                toAdd.add(e);
            }
        }
        if (super.addAll(toAdd)) {
            sort();
            return true;
        }
        return false;
    }

    @Override
    public boolean addAll(int i, Collection<? extends E> collection) {
        throw new NotImplementedException();
    }

    private void sort() {
        Collections.sort(this);
    }
}
