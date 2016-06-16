package com.guardtime.container.util;

import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.DataHasher;
import com.guardtime.ksi.hashing.HashAlgorithm;
import com.guardtime.ksi.hashing.HashException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

/**
 * Helper class containing utility functionality used throughout the code.
 */
public final class Util {

    /**
     * Checks that the given input is not null.
     * @throws NullPointerException when the input is null.
     */
    public static void notNull(Object o, String name) {
        if (o == null) {
            throw new NullPointerException(name + " must be present");
        }
    }

    /**
     * Checks that the given input is not null and is not empty.
     * @throws NullPointerException when the input is null.
     * @throws IllegalArgumentException when the input is empty.
     */
    public static void notEmpty(Collection<?> o, String name) {
        notNull(o, name);
        if (o.isEmpty()) {
            throw new IllegalArgumentException(name + " must not be empty");
        }
    }

    /**
     * Hashes the inputStream with the given algorithm and returns the produced {@link DataHash}
     */
    public static DataHash hash(InputStream inputStream, HashAlgorithm algorithm) {
        try {
            DataHasher hasher = new DataHasher(algorithm);
            hasher.addData(inputStream);
            return hasher.getHash();
        } catch (HashException e) {
            throw new IllegalArgumentException("Hash calculation failed", e);
        }
    }

    /**
     * Creates a temporary file with given prefix and suffix that will be deleted when the program exits.
     * @throws IOException when the file can't be created.
     */
    public static File createTempFile(String prefix, String suffix) throws IOException {
        File tempFile = File.createTempFile(prefix, suffix);
        tempFile.deleteOnExit();
        return tempFile;
    }

    /**
     * Extracts integer value from a string containing numbers. E.g. input string "town125" will return integer 125.
     */
    public static Integer extractIntegerFrom(String str) {
        return Integer.parseInt(str.replaceAll("[^0-9]", ""));
    }

    private Util() {
    }
}
