package com.guardtime.container.util;

import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.DataHasher;
import com.guardtime.ksi.hashing.HashAlgorithm;
import com.guardtime.ksi.hashing.HashException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

public final class Util {

    public static void notNull(Object o, String name) {
        if (o == null) {
            throw new IllegalArgumentException(name + " must be present");
        }
    }

    public static void notEmpty(Collection<?> o, String name) {
        notNull(o, name);
        if (o.isEmpty()) {
            throw new IllegalArgumentException(name + " must not be empty");
        }
    }

    public static DataHash hash(InputStream inputStream, HashAlgorithm algorithm) {
        try {
            DataHasher hasher = new DataHasher(algorithm);
            hasher.addData(inputStream);
            return hasher.getHash();
        } catch (HashException e) {
            throw new IllegalArgumentException("Hash calculation failed", e);
        }
    }

    public static File createTempFile(String prefix, String suffix) throws IOException {
        File tempFile = File.createTempFile(prefix, suffix);
        tempFile.deleteOnExit();
        return tempFile;
    }

    public static Integer extractIntegerFrom(String str) {
        return Integer.parseInt(str.replaceAll("[^0-9]", ""));
    }

    private Util() {
    }
}
