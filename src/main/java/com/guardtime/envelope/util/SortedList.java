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
public class SortedList<E extends Comparable<? super E>> extends ArrayList<E> {

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
        int index = Collections.binarySearch(this, e);
        if (index >= 0) {
            super.add(index, e);
        } else {
            super.add(-index - 1, e);
        }
        return true;
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
