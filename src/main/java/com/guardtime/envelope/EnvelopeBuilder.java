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

package com.guardtime.envelope;

import com.guardtime.envelope.annotation.EnvelopeAnnotation;
import com.guardtime.envelope.document.EnvelopeDocument;
import com.guardtime.envelope.document.FileEnvelopeDocument;
import com.guardtime.envelope.document.StreamEnvelopeDocument;
import com.guardtime.envelope.packaging.Envelope;
import com.guardtime.envelope.packaging.EnvelopePackagingFactory;
import com.guardtime.envelope.packaging.SignatureContent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import static com.guardtime.envelope.util.Util.notNull;

/**
 * Helper for creating a envelope with the provided documents and annotations.
 */
public class EnvelopeBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnvelopeBuilder.class);

    private final List<EnvelopeDocument> documents = new LinkedList<>();
    private final List<EnvelopeAnnotation> annotations = new LinkedList<>();

    private final EnvelopePackagingFactory packagingFactory;
    private Envelope existingEnvelope;

    /**
     * Expects a {@link EnvelopePackagingFactory} as parameter to be used for creating the envelope.
     */
    public EnvelopeBuilder(EnvelopePackagingFactory packagingFactory) {
        notNull(packagingFactory, "Packaging factory");
        this.packagingFactory = packagingFactory;
    }

    /**
     * Expects a {@link Envelope} as parameter to be expanded by new documents and annotations.
     */
    public EnvelopeBuilder withExistingEnvelope(Envelope existingEnvelope) {
        this.existingEnvelope = existingEnvelope;
        return this;
    }

    public EnvelopeBuilder withDocument(InputStream input, String name, String mimeType) {
        return withDocument(new StreamEnvelopeDocument(input, mimeType, name));
    }

    public EnvelopeBuilder withDocument(File file, String mimeType) {
        return withDocument(new FileEnvelopeDocument(file, mimeType));
    }

    public EnvelopeBuilder withDocument(EnvelopeDocument document) {
        notNull(document, "Data file ");
        checkDocumentNameExistence(document);
        documents.add(document);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Document '{}' will be added to the envelope", document);
        }
        return this;
    }

    public EnvelopeBuilder withAnnotation(EnvelopeAnnotation annotation) {
        notNull(annotations, "Annotation");
        annotations.add(annotation);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Annotation '{}' will be added to the envelope", annotation);
        }
        return this;
    }

    public Envelope build() throws EnvelopeException {
        Envelope envelope;
        if (existingEnvelope == null) {
            envelope = packagingFactory.create(documents, annotations);
        } else {
            packagingFactory.addSignature(existingEnvelope, documents, annotations);
            envelope = existingEnvelope;
        }
        documents.clear();
        annotations.clear();
        existingEnvelope = null;
        return envelope;
    }

    List<EnvelopeDocument> getDocuments() {
        return documents;
    }

    List<EnvelopeAnnotation> getAnnotations() {
        return annotations;
    }

    private void checkDocumentNameExistence(EnvelopeDocument document) {
        for (EnvelopeDocument doc : getAddedDocuments()) {
            if (doc.equals(document)) {
                continue;
            }
            if (doc.getFileName().equals(document.getFileName())) {
                throw new IllegalArgumentException("Document with name '" + document.getFileName() + "' already exists!");
            }
        }
    }

    private List<EnvelopeDocument> getAddedDocuments() {
        List<EnvelopeDocument> documents = new LinkedList<>(this.documents);
        if (existingEnvelope != null) {
            for (SignatureContent content : existingEnvelope.getSignatureContents()) {
                documents.addAll(content.getDocuments().values());
            }
        }
        return documents;
    }
}
