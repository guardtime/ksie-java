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
import com.guardtime.ksi.hashing.HashAlgorithm;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        if (o == null || getClass() != o.getClass()) return false;

        Document that = (Document) o;

        if (getFileName() != null ? !getFileName().equals(that.getFileName()) : that.getFileName() != null) return false;
        if (getMimeType() != null ? !getMimeType().equals(that.getMimeType()) : that.getMimeType() != null) return false;
        Set<HashAlgorithm> algorithmList = dataHashMap.keySet();
        if (that instanceof EmptyDocument) {
            algorithmList.retainAll(((EmptyDocument) that).dataHashMap.keySet());
        }
        return doDataHashesMatch(that, algorithmList);
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + (mimeType != null ? mimeType.hashCode() : 0);
        result = 31 * result + (fileName != null ? fileName.hashCode() : 0);
        return result;
    }

}
