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

import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.DataHasher;
import com.guardtime.ksi.hashing.HashAlgorithm;
import com.guardtime.ksi.hashing.HashException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

/**
 * Helper class containing utility functionality used throughout the code.
 */
public final class Util {

    public static final String TEMP_FILE_PREFIX = "ksie-";
    public static final String TEMP_FILE_SUFFIX = ".tmp";
    public static final String TEMP_DIR_PREFIX = "KSIE_";

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
    public static File createTempFile() throws IOException {
        File tempFile = File.createTempFile(TEMP_FILE_PREFIX, TEMP_FILE_SUFFIX);
        tempFile.deleteOnExit();
        return tempFile;
    }

    public static File createTempFile(Path directory) throws IOException {
        File file = Files.createTempFile(directory, TEMP_FILE_PREFIX, TEMP_FILE_SUFFIX).toFile();
        file.deleteOnExit();
        return file;
    }

    public static void copyToTempFile(InputStream inputStream, File temp) throws IOException {
        try (FileOutputStream tempFileOutStream = new FileOutputStream(temp)) {
            com.guardtime.ksi.util.Util.copyData(inputStream, tempFileOutStream);
        }
    }

    public static Path getTempDirectory() throws IOException {
        File tempDirectory = Files.createTempDirectory(TEMP_DIR_PREFIX).toFile();
        tempDirectory.deleteOnExit();
        return tempDirectory.toPath();
    }

    public static void deleteFileOrDirectory(Path path) throws IOException {
        if (path != null) {
            File[] contents = path.toFile().listFiles();
            if (contents != null) {
                for (File f : contents) {
                    deleteFileOrDirectory(f.toPath());
                }
            }
            Files.deleteIfExists(path);
        }
    }

    private Util() {
    }
}
