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

package com.guardtime.container.util;

/**
 * Represents a pair of objects.
 */
public class Pair<L, R> {

    private L left;
    private R right;

    private Pair(L left, R right) {
        Util.notNull(left, "Left value");
        Util.notNull(right, "Right value");
        this.left = left;
        this.right = right;
    }

    /**
     * Static factory method for creating a {@link Pair} instance.
     *
     * @param left  left value. Can not be null
     * @param right right value. Can not be null
     */
    public static <L, R> Pair<L, R> of(L left, R right) {
        return new Pair(left, right);
    }

    /**
     * Method for accessing the left value of the {@link Pair}
     */
    public L getLeft() {
        return left;
    }

    /**
     * Method for accessing the right value of the {@link Pair}
     */
    public R getRight() {
        return right;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Pair<?, ?> pair = (Pair<?, ?>) o;

        if (!left.equals(pair.left)) return false;
        return right.equals(pair.right);

    }

    @Override
    public int hashCode() {
        int result = left.hashCode();
        result = 31 * result + right.hashCode();
        return result;
    }
}
