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

import com.guardtime.envelope.annotation.Annotation;
import com.guardtime.envelope.packaging.SignatureContent;
import com.guardtime.envelope.signature.EnvelopeSignature;
import com.guardtime.envelope.util.DataHashException;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper for a {@link Document} which creates a backward link to the {@link SignatureContent} that contains
 * the Document and that provides the {@link EnvelopeSignature} for the document.
 * Helper class to allow for simpler association between a {@link Document} and {@link EnvelopeSignature} and
 * any related {@link Annotation}s.
 */

public class SignedDocument implements Document {
    private final Document delegated;
    private final SignatureContent content;

    /**
     * Creates {@link Document} wrapper from provided original {@link Document} and provided {@link SignatureContent}.
     * @param original the wrapped {@link Document}.
     * @param content  the {@link SignatureContent} that contains and signs this {@link Document}.
     */
    public SignedDocument(Document original, SignatureContent content) {
        this.delegated = original;
        this.content = content;
    }

    @Override
    public String getFileName() {
        return delegated.getFileName();
    }

    @Override
    public String getMimeType() {
        return delegated.getMimeType();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return delegated.getInputStream();
    }

    @Override
    public List<DataHash> getDataHashList(List<HashAlgorithm> algorithmList) throws DataHashException {
        return delegated.getDataHashList(algorithmList);
    }

    @Override
    public boolean isWritable() {
        return delegated.isWritable();
    }

    @Override
    public String getPath() {
        return delegated.getPath();
    }

    @Override
    public DataHash getDataHash(HashAlgorithm algorithm) throws DataHashException {
        return delegated.getDataHash(algorithm);
    }

    @Override
    public void close() throws Exception {
        delegated.close();
    }

    /**
     * @return The {@link EnvelopeSignature} that is associated with, e.g. signs this {@link Document}.
     */
    public EnvelopeSignature getSignature() {
        return content.getEnvelopeSignature();
    }

    /**
     * @return List of {@link Annotation}s that are associated with this {@link Document}.
     */
    public List<Annotation> getAnnotations() {
        return new ArrayList<>(content.getAnnotations().values());
    }
}
