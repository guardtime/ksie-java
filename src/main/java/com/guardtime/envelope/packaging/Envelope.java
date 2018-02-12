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
import com.guardtime.envelope.document.ParsedDocument;
import com.guardtime.envelope.document.SignedDocument;
import com.guardtime.envelope.document.UnknownDocument;
import com.guardtime.envelope.packaging.exception.EnvelopeMergingException;
import com.guardtime.envelope.packaging.parsing.store.ParsingStore;
import com.guardtime.envelope.packaging.parsing.store.ParsingStoreException;
import com.guardtime.envelope.util.SortedList;
import com.guardtime.envelope.util.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static com.guardtime.envelope.packaging.EnvelopeMergingVerifier.verifyNewSignatureContentIsAcceptable;
import static com.guardtime.envelope.packaging.EnvelopeMergingVerifier.verifyUniqueUnknownFiles;
import static com.guardtime.envelope.packaging.EnvelopeMergingVerifier.verifyUniqueness;

/**
 * Envelope that encompasses documents, annotations and structure elements that links the annotations to the documents
 * and signatures that validate the content of the envelope.
 */
public class Envelope implements AutoCloseable {

    private ParsingStore parsingStore;
    private List<SignatureContent> signatureContents = new SortedList<>();
    private boolean closed = false;
    private List<UnknownDocument> unknownFiles = new LinkedList<>();

    public Envelope(SignatureContent signatureContent) {
        this(Collections.singletonList(signatureContent), Collections.<UnknownDocument>emptyList(), null);
    }

    public Envelope(Collection<SignatureContent> contents, List<UnknownDocument> unknownFiles, ParsingStore store) {
        Util.notNull(contents, "Signature contents");
        Util.notNull(unknownFiles, "Unknown files");
        this.signatureContents.addAll(contents);
        this.unknownFiles.addAll(unknownFiles);
        this.parsingStore = store;
    }

    protected Envelope(Envelope original) {
        this(
                original.getSignatureContents(),
                original.getUnknownFiles(),
                original.getParsingStore()
        );
    }

    /**
     * Returns sorted list of {@link SignatureContent} contained in this envelope.
     */
    public List<SignatureContent> getSignatureContents() {
        return Collections.unmodifiableList(signatureContents);
    }

    /**
     * Returns the {@link SignatureContent} at {@param index} and removes it from this {@link Envelope}.
     */
    public boolean removeSignatureContent(SignatureContent content) {
        return signatureContents.remove(content);
    }

    /**
     * Returns List of all {@link UnknownDocument} that were not associated with any structure elements or signatures but were
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
     */
    @Override
    public void close() throws Exception {
        for (SignatureContent content : getSignatureContents()) {
            content.close();
        }
        for (UnknownDocument f : getUnknownFiles()) {
            f.close();
        }
        if (parsingStore != null) {
            this.parsingStore.close();
        }
        this.closed = true;
    }

    /**
     * Adds the {@link SignatureContent} to this {@link Envelope}. Also takes ownership of the resources associated with the
     * {@link SignatureContent} and as such any external calls to close() on those resources may lead to unexpected behaviour.
     * @throws EnvelopeMergingException when the {@link SignatureContent} can not be added into the {@link Envelope} due to
     * clashing file paths or any other reason.
     */
    public void add(SignatureContent content) throws EnvelopeMergingException {
        verifyNewSignatureContentIsAcceptable(content, signatureContents);
        verifyUniqueness(content, signatureContents);
        signatureContents.add(content);
    }

    /**
     * Adds all {@link SignatureContent}s from input {@link Envelope}. Also takes ownership of the resources associated with the
     * {@link Envelope} and as such any external calls to close() on those resources may lead to unexpected behaviour.
     * @throws EnvelopeMergingException when any {@link SignatureContent} can not be added into the {@link Envelope} due to
     * clashing file paths or any other reason.
     */
    public void add(Envelope envelope) throws EnvelopeMergingException {
        verifyUniqueUnknownFiles(envelope, this);
        List<SignatureContent> signatureContents = new ArrayList<>(envelope.getSignatureContents());
        for (SignatureContent content : signatureContents) {
            if (envelope.removeSignatureContent(content)) {
                add(content);
            }
        }

        if (parsingStore != null && envelope.getParsingStore() != null) {
            for (UnknownDocument unknownDocument : envelope.getUnknownFiles()) {
                unknownFiles.add(new ParsedDocument(
                        parsingStore,
                        unknownDocument.getFileName(),
                        unknownDocument.getMimeType(),
                        unknownDocument.getFileName()
                ));
            }
            try {
                parsingStore.transferFrom(envelope.getParsingStore());
            } catch (ParsingStoreException e) {
                throw new EnvelopeMergingException("Failed to take control of parsed data!", e);
            }
        } else if (envelope.getParsingStore() != null) {
            parsingStore = envelope.getParsingStore();
            unknownFiles = envelope.removeAllUnknownFiles();
        }
    }

    private List<UnknownDocument> removeAllUnknownFiles() {
        List<UnknownDocument> returnable = unknownFiles;
        unknownFiles = Collections.emptyList();
        return returnable;
    }

    /**
     * Adds all {@link SignatureContent}s to this {@link Envelope}. Also takes ownership of the resources associated with the
     * {@link SignatureContent}s and as such any external calls to close() on those resources may lead to unexpected behaviour.
     * @throws EnvelopeMergingException when any {@link SignatureContent} can not be added into the {@link Envelope} due to
     * clashing file paths or any other reason.
     */
    public void addAll(Collection<SignatureContent> contents) throws EnvelopeMergingException {
        List<SignatureContent> original = new LinkedList<>(signatureContents);
        try {
            for (SignatureContent content : contents) {
                add(content);
            }
        } catch (Exception e) {
            this.signatureContents = original;
            throw e;
        }
    }

    /**
     * Returns a list of all {@link Document}s that are signed.
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

    protected ParsingStore getParsingStore() {
        return parsingStore;
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
