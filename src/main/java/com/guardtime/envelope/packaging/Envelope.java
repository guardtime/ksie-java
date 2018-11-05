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

package com.guardtime.envelope.packaging;

import com.guardtime.envelope.annotation.Annotation;
import com.guardtime.envelope.document.Document;
import com.guardtime.envelope.document.DocumentFactory;
import com.guardtime.envelope.document.SignedDocument;
import com.guardtime.envelope.document.UnknownDocument;
import com.guardtime.envelope.packaging.exception.EnvelopeClosingException;
import com.guardtime.envelope.packaging.exception.EnvelopeMergingException;
import com.guardtime.envelope.packaging.parsing.store.ParsingStore;
import com.guardtime.envelope.util.SortedList;
import com.guardtime.envelope.util.Util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static com.guardtime.envelope.packaging.EnvelopeMergingVerifier.verifyNewSignatureContentIsAcceptable;
import static com.guardtime.envelope.packaging.EnvelopeMergingVerifier.verifyUniqueness;

/**
 * Envelope that encompasses documents, annotations and structure elements that link the annotations to the documents
 * and signatures that validate the content of the envelope.
 */
public class Envelope implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(Envelope.class);

    private List<SignatureContent> signatureContents = new SortedList<>();
    private boolean closed = false;
    private List<UnknownDocument> unknownFiles = new LinkedList<>();

    public Envelope(SignatureContent signatureContent) {
        this(Collections.singletonList(signatureContent), Collections.<UnknownDocument>emptyList());
    }

    public Envelope(Collection<SignatureContent> contents, List<UnknownDocument> unknownFiles) {
        Util.notNull(contents, "Signature contents");
        Util.notNull(unknownFiles, "Unknown files");
        this.signatureContents.addAll(contents);
        this.unknownFiles.addAll(unknownFiles);
    }

    public Envelope(Envelope original, ParsingStore store) {
        this(
                copySignatureContents(original.getSignatureContents(), store),
                copyUnknownFiles(original.getUnknownFiles(), store)
        );
    }

    static List<UnknownDocument> copyUnknownFiles(List<UnknownDocument> originals, ParsingStore store) {
        DocumentFactory documentFactory = new DocumentFactory(store);
        List<UnknownDocument> copies = new ArrayList<>();
        for (UnknownDocument doc : originals) {
            copies.add((UnknownDocument) documentFactory.create(doc));
        }
        return copies;
    }

    static List<SignatureContent> copySignatureContents(List<SignatureContent> originals, ParsingStore store) {
        List<SignatureContent> copies = new ArrayList<>();
        for (SignatureContent signatureContent : originals) {
            copies.add(new SignatureContent(signatureContent, store));
        }
        return copies;
    }

    /**
     * @return Sorted list of {@link SignatureContent} contained in this envelope.
     */
    public List<SignatureContent> getSignatureContents() {
        return Collections.unmodifiableList(signatureContents);
    }

    /**
     * @return List of all {@link UnknownDocument} that were not associated with any structure elements or signatures but were
     * contained in the {@link Envelope}
     */
    public List<UnknownDocument> getUnknownFiles() {
        return Collections.unmodifiableList(unknownFiles);
    }

    /**
     * Closes the envelope and all {@link Document}s and
     * {@link Annotation}s in the envelope.
     * NB! This will close {@link Document}s and
     * {@link Annotation}s added during creation as well.
     *
     * @throws EnvelopeClosingException when some resources fail to close. All resources are attempted to be closed and all
     * exceptions encountered will be added to thrown exception and can be accessed by
     * {@link EnvelopeClosingException#getExceptions()}.
     */
    @Override
    public void close() throws Exception {
        List<Exception> exceptions = new ArrayList<>();
        for (SignatureContent content : getSignatureContents()) {
            try {
                content.close();
            } catch (Exception e) {
                exceptions.add(e);
                logger.debug("Failed to close SignatureContent!", e);
            }
        }

        for (UnknownDocument f : getUnknownFiles()) {
            try {
                f.close();
            } catch (Exception e) {
                exceptions.add(e);
                logger.debug("Failed to close UnknownDocument!", e);
            }
        }
        if (!exceptions.isEmpty()) {
            throw new EnvelopeClosingException(
                    String.format("Failed to close all Envelope resources! Encountered %d exceptions!", exceptions.size()),
                    exceptions
            );
        }

        this.closed = true;
    }

    /**
     * Adds a copy of {@link SignatureContent} to this {@link Envelope}.
     *
     * @param content the content to be added.
     * @param parsingStore instance of {@link ParsingStore} to be used for deep copying {@link SignatureContent}.
     * @throws EnvelopeMergingException when the {@link SignatureContent} can not be added into the {@link Envelope}.
     */
    public void add(SignatureContent content, ParsingStore parsingStore) throws EnvelopeMergingException {
        verifyNewSignatureContentIsAcceptable(content, signatureContents);
        verifyUniqueness(content, signatureContents, unknownFiles);
        // TODO: If exists, ignore
        if (!signatureContents.contains(content)) {
            signatureContents.add(new SignatureContent(content, parsingStore));
        }
    }

    /**
     * Adds copies of all {@link SignatureContent}s to this {@link Envelope}.
     *
     * @param contents the content to be added.
     * @param parsingStore instance of {@link ParsingStore} to be used for deep copying {@link SignatureContent}.
     * @throws EnvelopeMergingException when any {@link SignatureContent} can not be added into the {@link Envelope}.
     */
    public void addAll(Collection<SignatureContent> contents, ParsingStore parsingStore) throws EnvelopeMergingException {
        List<SignatureContent> original = new LinkedList<>(signatureContents);
        List<SignatureContent> copies = new LinkedList<>();
        SignatureContent copiedContent = null;
        try {
            for (SignatureContent content : contents) {
                copiedContent = new SignatureContent(content, parsingStore);
                verifyNewSignatureContentIsAcceptable(copiedContent, signatureContents);
                verifyUniqueness(copiedContent, signatureContents, unknownFiles);
                copies.add(copiedContent);
            }
            signatureContents.addAll(copies);
        } catch (Exception e) {
            if (copiedContent != null) {
                try {
                    copiedContent.close();
                } catch (Exception signatureContentCloseException) {
                    // We are already throwing exception with more important information about why merging failed.
                    // Log down the exception for closing and continue to close other SignatureContent
                    logger.error("Failed to close copied SignatureContent!", e);
                }
            }
            for (SignatureContent content : copies) {
                try {
                    content.close();
                } catch (Exception signatureContentCloseException) {
                    // We are already throwing exception with more important information about why merging failed.
                    // Log down the exception for closing and continue to close other SignatureContent
                    logger.error("Failed to close copied SignatureContent!", e);
                }
            }
            this.signatureContents = original;
            throw e;
        }
    }

    /**
     * @return The list of all {@link Document}s that are signed.
     */
    public List<SignedDocument> getSignedDocuments() {
        List<SignedDocument> signedDocuments = new ArrayList<>();
        for (SignatureContent content : signatureContents) {
            for (Document doc : content.getDocuments().values()) {
                signedDocuments.add(new SignedDocument(doc, content));
            }
        }
        return signedDocuments;
    }

    public boolean isClosed() {
        return closed;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " {" +
                "signatureContents= " + signatureContents +
                ", closed= " + closed +
                ", unknownFiles= " + unknownFiles +
                '}';
    }

}
