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

package com.guardtime.envelope;

import com.guardtime.envelope.annotation.Annotation;
import com.guardtime.envelope.annotation.AnnotationFactory;
import com.guardtime.envelope.annotation.EnvelopeAnnotationType;
import com.guardtime.envelope.document.Document;
import com.guardtime.envelope.document.DocumentFactory;
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
 * Helper for creating an envelope with the provided documents and annotations.
 */
public class EnvelopeBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnvelopeBuilder.class);

    private final List<Document> documents = new LinkedList<>();
    private final List<Annotation> annotations = new LinkedList<>();

    private final EnvelopePackagingFactory packagingFactory;
    private Envelope existingEnvelope;

    /**
     * Expects a {@link EnvelopePackagingFactory} as parameter to be used for creating the envelope.
     *
     * @param packagingFactory The factory to be used for processing builder values and constructing an {@link Envelope}.
     */
    public EnvelopeBuilder(EnvelopePackagingFactory packagingFactory) {
        notNull(packagingFactory, "Packaging factory");
        this.packagingFactory = packagingFactory;
    }

    /**
     * Expects a {@link Envelope} as parameter to be expanded by new documents and annotations.
     *
     * @param existingEnvelope the existing {@link Envelope} to be expanded.
     *
     * @return The same {@link EnvelopeBuilder}.
     */
    public EnvelopeBuilder withExistingEnvelope(Envelope existingEnvelope) {
        this.existingEnvelope = existingEnvelope;
        return this;
    }

    public EnvelopeBuilder withDocument(InputStream input, String name, String mimeType) {
        return withDocument(DocumentFactory.create(input, mimeType, name));
    }

    public EnvelopeBuilder withDocument(File file, String mimeType) {
        return withDocument(DocumentFactory.create(file, mimeType));
    }

    public EnvelopeBuilder withDocument(Document document) {
        notNull(document, "Data file ");
        checkDocumentNameExistence(document);
        documents.add(document);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Document '{}' will be added to the envelope", document);
        }
        return this;
    }

    public EnvelopeBuilder withAnnotation(String content, String domain, EnvelopeAnnotationType type) {
        return withAnnotation(AnnotationFactory.create(content, domain, type));
    }

    public EnvelopeBuilder withAnnotation(File content, String domain, EnvelopeAnnotationType type) {
        return withAnnotation(AnnotationFactory.create(content, domain, type));
    }

    public EnvelopeBuilder withAnnotation(InputStream content, String domain, EnvelopeAnnotationType type) {
        return withAnnotation(AnnotationFactory.create(content, domain, type));
    }

    public EnvelopeBuilder withAnnotation(Annotation annotation) {
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
            envelope = packagingFactory.addSignature(existingEnvelope, documents, annotations);
        }
        documents.clear();
        annotations.clear();
        existingEnvelope = null;
        return envelope;
    }

    List<Document> getDocuments() {
        return documents;
    }

    List<Annotation> getAnnotations() {
        return annotations;
    }

    private void checkDocumentNameExistence(Document document) {
        for (Document doc : getAddedDocuments()) {
            if (doc.equals(document)) {
                continue;
            }
            if (doc.getFileName().equals(document.getFileName())) {
                throw new IllegalArgumentException("Document with name '" + document.getFileName() + "' already exists!");
            }
        }
    }

    private List<Document> getAddedDocuments() {
        List<Document> documents = new LinkedList<>(this.documents);
        if (existingEnvelope != null) {
            for (SignatureContent content : existingEnvelope.getSignatureContents()) {
                documents.addAll(content.getDocuments().values());
            }
        }
        return documents;
    }
}
