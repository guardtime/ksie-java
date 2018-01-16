/*
 * Copyright 2013-2017 Guardtime, Inc.
 *
 * This file is part of the Guardtime client SDK.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES, CONDITIONS, OR OTHER LICENSES OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 * "Guardtime" and "KSI" are trademarks or registered trademarks of
 * Guardtime, Inc., and no license to trademarks is granted; Guardtime
 * reserves and retains all trademark rights.
 */

package com.guardtime.envelope.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Keeps all records in list sorted. Performs sorting at every insert.
 * Ignores duplicates.
 * @param <E>  List entry type
 *
 * NB! Does not allow inserting at specified index!
 */
public class SortedList<E extends Comparable<? super E>> implements List<E> {
    private final List<E> delegate;

    public SortedList(Collection<? extends E> collection) {
        this.delegate = new ArrayList<>(collection);
        sort();
    }

    public SortedList() {
        this.delegate = new ArrayList<>();
    }

    @Override
    public E set(int i, E e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return delegate.contains(o);
    }

    @Override
    public Iterator<E> iterator() {
        return delegate.iterator();
    }

    @Override
    public Object[] toArray() {
        return delegate.toArray();
    }

    @Override
    public <T> T[] toArray(T[] ts) {
        return delegate.toArray(ts);
    }

    @Override
    public boolean add(E e) {
        if (contains(e)) {
            return false;
        }
        int index = Collections.binarySearch(delegate, e);
        if (index >= 0) {
            delegate.add(index, e);
        } else {
            delegate.add(-index - 1, e);
        }
        return true;
    }

    @Override
    public boolean remove(Object o) {
        return delegate.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        return delegate.containsAll(collection);
    }

    @Override
    public void add(int i, E e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public E remove(int i) {
        return delegate.remove(i);
    }

    @Override
    public int indexOf(Object o) {
        return delegate.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return delegate.lastIndexOf(o);
    }

    @Override
    public ListIterator<E> listIterator() {
        return delegate.listIterator();
    }

    @Override
    public ListIterator<E> listIterator(int i) {
        return delegate.listIterator(i);
    }

    @Override
    public List<E> subList(int i, int i1) {
        return delegate.subList(i, i1);
    }

    @Override
    public boolean addAll(Collection<? extends E> collection) {
        for (E e : collection) {
            if (contains(e)) {
                throw new IllegalArgumentException("Duplicates are not allowed!");
            }
        }
        if (delegate.addAll(collection)) {
            sort();
            return true;
        }
        return false;
    }

    @Override
    public boolean addAll(int i, Collection<? extends E> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        return delegate.removeAll(collection);
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        return delegate.retainAll(collection);
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public E get(int i) {
        return delegate.get(i);
    }

    private void sort() {
        Collections.sort(delegate);
    }
}
