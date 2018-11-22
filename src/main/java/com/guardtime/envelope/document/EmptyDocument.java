/*
 * Copyright 2013-2018 Guardtime, Inc.
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

package com.guardtime.envelope.document;

import com.guardtime.envelope.util.DataHashException;
import com.guardtime.envelope.util.Util;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.DataHasher;
import com.guardtime.ksi.hashing.HashAlgorithm;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a document in a envelope which doesn't store the document data in the same envelope (detached document).
 */
public class EmptyDocument extends AbstractDocument {
    protected final Map<HashAlgorithm, DataHash> dataHashMap;

    /**
     * Creates a new {@link EmptyDocument} with the given properties.
     *
     * @param fileName name of the {@link EmptyDocument}.
     * @param mimeType MIME type of the file, can be freely chosen.
     * @param hashes list of {@link DataHash} that are generated from the data associated with the document.
     */
    EmptyDocument(String fileName, String mimeType, List<DataHash> hashes) {
        super(mimeType, fileName);
        Util.notEmpty(hashes, "Data hash list");
        this.dataHashMap = mapHashes(hashes);
    }

    private Map<HashAlgorithm, DataHash> mapHashes(List<DataHash> hashList) {
        Map<HashAlgorithm, DataHash> map = new HashMap<>();
        for (DataHash hash : hashList) {
            map.put(hash.getAlgorithm(), hash);
        }
        return map;
    }

    @Override
    public InputStream getInputStream() {
        return null;
    }

    @Override
    public DataHash getDataHash(HashAlgorithm algorithm) throws DataHashException {
        if (!dataHashMap.containsKey(algorithm)) {
            throw new DataHashException("Could not find pre-generated hash for algorithm '" + algorithm.getName() + "'");
        }
        return dataHashMap.get(algorithm);
    }

    @Override
    public boolean isWritable() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof Document)) return false;
        Document that = (Document) o;

        if (getFileName() != null ? !getFileName().equals(that.getFileName()) : that.getFileName() != null) return false;
        if (getMimeType() != null ? !getMimeType().equals(that.getMimeType()) : that.getMimeType() != null) return false;

        if (that instanceof EmptyDocument) {
            return Objects.equals(dataHashMap, ((EmptyDocument) that).dataHashMap);
        } else {
            Map<HashAlgorithm, DataHash> thatHashMap = generateDataHashMap(dataHashMap.keySet(), that);
            return Objects.equals(dataHashMap, thatHashMap);
        }
    }

    private Map<HashAlgorithm, DataHash> generateDataHashMap(Set<HashAlgorithm> algorithms, Document that) {
        Map<HashAlgorithm, DataHash> returnable = new HashMap<>();
        for (HashAlgorithm algorithm : algorithms) {
            try (InputStream inputStream = that.getInputStream()) {
                returnable.put(algorithm, new DataHasher(algorithm, false).addData(inputStream).getHash());
            } catch (IOException e) {
                throw new RuntimeException("Failed to access content of Document for equality comparison", e);
            }
        }
        return returnable;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileName, mimeType, dataHashMap);
    }
}
