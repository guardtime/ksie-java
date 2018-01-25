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

import static com.guardtime.envelope.util.Util.notNull;
import static com.guardtime.ksi.util.Util.toByteArray;

public abstract class AbstractAnnotation implements Annotation {
    protected static final Logger logger = LoggerFactory.getLogger(Annotation.class);

    protected final String domain;
    protected final EnvelopeAnnotationType type;
    private DataHash dataHash;
    private String path;

    public AbstractAnnotation(String domain, EnvelopeAnnotationType type) {
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
        return this.getClass().toString() +
                " {type=\'" + type.getContent() + "\'" +
                ", domain=\'" + domain + "\'" +
                ", content=\'" + getContent() + "\'}";
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
}
