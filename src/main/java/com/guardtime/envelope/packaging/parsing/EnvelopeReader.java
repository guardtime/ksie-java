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

package com.guardtime.envelope.packaging.parsing;

import com.guardtime.envelope.document.UnknownDocument;
import com.guardtime.envelope.manifest.EnvelopeManifestFactory;
import com.guardtime.envelope.packaging.Envelope;
import com.guardtime.envelope.packaging.EnvelopePackagingFactory;
import com.guardtime.envelope.packaging.SignatureContent;
import com.guardtime.envelope.packaging.exception.EnvelopeReadingException;
import com.guardtime.envelope.packaging.exception.InvalidEnvelopeException;
import com.guardtime.envelope.packaging.parsing.handler.ContentParsingException;
import com.guardtime.envelope.packaging.parsing.store.ParsingStore;
import com.guardtime.envelope.signature.SignatureFactory;
import com.guardtime.envelope.util.Pair;
import com.guardtime.envelope.util.Util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Provides stream parsing logic for {@link EnvelopePackagingFactory}.
 * Derivatives of this class must be stateless and reusable.
 */
public abstract class EnvelopeReader {

    protected static final Logger LOGGER = LoggerFactory.getLogger(EnvelopeReader.class);

    private final EnvelopeManifestFactory manifestFactory;
    private final SignatureFactory signatureFactory;
    private final ParsingStore parsingStore;

    public EnvelopeReader(EnvelopeManifestFactory manifestFactory, SignatureFactory signatureFactory,
                          ParsingStore store) {
        Util.notNull(manifestFactory, "Manifest factory");
        Util.notNull(signatureFactory, "Signature factory");
        Util.notNull(store, "Parsing store");
        this.manifestFactory = manifestFactory;
        this.signatureFactory = signatureFactory;
        this.parsingStore = store;
    }

    /**
     *Parses an {@link InputStream} to produce an {@link Envelope}.
     *
     * @param input    an {@link InputStream} that contains a valid/parsable {@link Envelope}. This InputStream will be
     *                       closed after reading.
     * @return An instance of {@link Envelope} based on the data from {@link InputStream}. Does not verify
     *         the envelope/signature(s).
     * @throws InvalidEnvelopeException when the {@link InputStream} does not contain a parsable {@link Envelope}.
     * @throws EnvelopeReadingException when there were issues parsing some elements of the {@link Envelope}. The parsed
     *         envelope and all encountered exceptions can be retrieved from this exception.
     * @throws IOException              when errors occur accessing data in provided {@link InputStream}.
     */
    public Envelope read(InputStream input) throws IOException, InvalidEnvelopeException {
        EnvelopeReadingException readingException = new EnvelopeReadingException("Reading envelope encountered errors!");
        ParsingStoreHandler parsingStoreHandler = new ParsingStoreHandler(parsingStore);
        parseInputStream(input, parsingStoreHandler, readingException);
        EnvelopeElementExtractor envelopeElementExtractor =
                new EnvelopeElementExtractor(manifestFactory, signatureFactory, parsingStoreHandler);

        List<SignatureContent> contents = buildSignatures(envelopeElementExtractor, readingException, parsingStoreHandler);
        validateMimeType(envelopeElementExtractor);
        List<UnknownDocument> unknownFiles = parsingStoreHandler.getUnrequestedFiles();
        parsingStoreHandler.clear();
        Envelope envelope = new Envelope(contents, unknownFiles);
        readingException.setEnvelope(envelope);

        if (!containsValidContents(contents)) {
            readingException.addException(new InvalidEnvelopeException("Parsed envelope was not valid"));
        }

        if (!readingException.getExceptions().isEmpty()) {
            throw readingException;
        }
        return envelope;
    }

    /**
     * Processes input stream containing envelope and stores each entry in envelope to parsing store.
     * @param input            {@link InputStream} containing {@link Envelope}.
     * @param storeHandler     stores all parsed entries. Implementation must add each entry to store (key, stream) method.
     * @param readingException holds all expectable exceptions if any occurs.
     * @throws IOException     when error occurs during accessing of InputStream.
     */
    protected abstract void parseInputStream(InputStream input, ParsingStoreHandler storeHandler,
                                             EnvelopeReadingException readingException)
            throws IOException;

    private boolean containsValidContents(List<SignatureContent> signatureContents) {
        for (SignatureContent content : signatureContents) {
            if (containsManifest(content) ||
                    containsOrContainedDocuments(content) ||
                    containsOrContainedAnnotations(content)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsOrContainedDocuments(SignatureContent content) {
        return content.getDocuments().size() > 0 || content.getDocumentsManifest().getDocumentReferences().size() > 0;
    }

    private boolean containsOrContainedAnnotations(SignatureContent content) {
        return content.getAnnotations().size() > 0 ||
                content.getAnnotationsManifest().getSingleAnnotationManifestReferences().size() > 0;
    }

    private boolean containsManifest(SignatureContent content) {
        return content.getManifest() != null ||
                content.getDocumentsManifest() != null ||
                content.getAnnotationsManifest() != null;
    }

    private void validateMimeType(EnvelopeElementExtractor envelopeElementExtractor) throws InvalidEnvelopeException {
        try {
            byte[] content = envelopeElementExtractor.getMimeTypeContent();
            String parsedMimeType = new String(content);
            if (!parsedMimeType.equals(getMimeType())) {
                throw new InvalidEnvelopeException("Parsed Envelope has invalid MIME type. Can't process it!");
            }
        } catch (ContentParsingException e) {
            LOGGER.debug("Failed to parse MIME type. Reason: '{}", e.getMessage());
            throw new InvalidEnvelopeException("No parsable MIME type.", e);
        }
    }

    protected abstract String getMimeType();

    private List<SignatureContent> buildSignatures(EnvelopeElementExtractor envelopeElementExtractor,
                                                   EnvelopeReadingException readingException,
                                                   ParsingStoreHandler parsingStoreHandler) {
        Set<String> parsedManifestUriSet = envelopeElementExtractor.getManifestUris();
        SignatureContentComposer signatureContentComposer = new SignatureContentComposer(envelopeElementExtractor);
        List<SignatureContent> signatures = new LinkedList<>();
        for (String manifestUri : parsedManifestUriSet) {
            try {
                Pair<SignatureContent, List<Throwable>> signatureContentVectorPair =
                        signatureContentComposer.compose(manifestUri, parsingStoreHandler);
                signatures.add(signatureContentVectorPair.getLeft());
                readingException.addExceptions(signatureContentVectorPair.getRight());
            } catch (ContentParsingException e) {
                LOGGER.debug("Parsing SignatureContent failed for '{}'. Reason: '{}'", manifestUri, e.getMessage());
                readingException.addException(e);
            }
        }
        return signatures;
    }

}
