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

}
