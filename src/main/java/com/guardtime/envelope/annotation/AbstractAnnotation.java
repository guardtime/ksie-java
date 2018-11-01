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

package com.guardtime.envelope.annotation;

import com.guardtime.envelope.util.DataHashException;
import com.guardtime.envelope.util.Util;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import static com.guardtime.envelope.manifest.Manifest.DEFAULT_HASH_ALGORITHM;
import static com.guardtime.envelope.util.Util.notNull;
import static com.guardtime.ksi.util.Util.toByteArray;

/**
 * Generic implementation for {@link Annotation} that is lacking {@link Annotation#getInputStream()} implementation.
 */
abstract class AbstractAnnotation implements Annotation {
    protected static final Logger logger = LoggerFactory.getLogger(Annotation.class);

    protected final String domain;
    protected final EnvelopeAnnotationType type;
    private DataHash dataHash;
    private String path;

    /**
     * Creates {@link Annotation} with provided type and domain.
     * @param domain The key of the annotation key-value pair. To prevent key conflicts, the prefix x.y.z. is reserved to the
     *               entity controlling the Internet domain name z.y.x.
     * @param type annotation type, indicating the persistence of the annotation, see {@link EnvelopeAnnotationType} for details.
     */
    protected AbstractAnnotation(String domain, EnvelopeAnnotationType type) {
        notNull(domain, "Domain");
        notNull(type, "Annotation type");
        this.type = type;
        this.domain = domain;
    }


    @Override
    public EnvelopeAnnotationType getAnnotationType() {
        return type;
    }

    @Override
    public String getDomain() {
        return domain;
    }

    @Override
    public DataHash getDataHash(HashAlgorithm algorithm) throws DataHashException {
        if (dataHash == null || !dataHash.getAlgorithm().equals(algorithm)) {
            try (InputStream inputStream = getInputStream()) {
                dataHash = Util.hash(inputStream, algorithm);
            } catch (IOException e) {
                throw new DataHashException("Failed to access data to generate hash.", e);
            }
        }
        return dataHash;
    }

    @Override
    public void close() throws Exception {
        // Nothing to close
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() +
                " {type= \'" + type.getContent() + '\'' +
                ", domain= \'" + getDomain() + '\'' +
                ", content= \'" + getContent() + "\'}";
    }

    private String getContent() {
        try (InputStream inputStream = getInputStream()) {
            byte[] bytes = toByteArray(inputStream);
            return new String(bytes);
        } catch (IOException e) {
            logger.warn("Failed to get content of annotation.", e);
            return "";
        }
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractAnnotation)) return false;
        AbstractAnnotation that = (AbstractAnnotation) o;
        try {
            return getDomain().equals(that.getDomain()) &&
                    getAnnotationType().equals(that.getAnnotationType()) &&
                    Objects.equals(getPath(), that.getPath()) &&
                    getDataHash(DEFAULT_HASH_ALGORITHM).equals(that.getDataHash(DEFAULT_HASH_ALGORITHM));
        } catch (DataHashException e) {
            throw new RuntimeException("Data hash calculation for equality check failed!", e);
        }
    }

    @Override
    public int hashCode() {
        DataHash dataHash = null;
        try {
            dataHash = getDataHash(DEFAULT_HASH_ALGORITHM);
        } catch (DataHashException e) {
            throw new RuntimeException("Object hash calculation failed!", e);
        }
        return Objects.hash(getDomain(), getAnnotationType(), dataHash, getPath());
    }

}
