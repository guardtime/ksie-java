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

package com.guardtime.envelope.packaging.parsing;

import com.guardtime.envelope.document.ParsedDocument;
import com.guardtime.envelope.document.UnknownDocument;
import com.guardtime.envelope.manifest.AnnotationsManifest;
import com.guardtime.envelope.manifest.DocumentsManifest;
import com.guardtime.envelope.manifest.EnvelopeManifestFactory;
import com.guardtime.envelope.manifest.Manifest;
import com.guardtime.envelope.manifest.SingleAnnotationManifest;
import com.guardtime.envelope.packaging.parsing.handler.AnnotationsManifestHandler;
import com.guardtime.envelope.packaging.parsing.handler.ContentHandler;
import com.guardtime.envelope.packaging.parsing.handler.ContentParsingException;
import com.guardtime.envelope.packaging.parsing.handler.DocumentsManifestHandler;
import com.guardtime.envelope.packaging.parsing.handler.ManifestHandler;
import com.guardtime.envelope.packaging.parsing.handler.MimeTypeHandler;
import com.guardtime.envelope.packaging.parsing.handler.SignatureHandler;
import com.guardtime.envelope.packaging.parsing.handler.SingleAnnotationManifestHandler;
import com.guardtime.envelope.packaging.parsing.store.ParsingStore;
import com.guardtime.envelope.signature.EnvelopeSignature;
import com.guardtime.envelope.signature.SignatureFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.guardtime.envelope.packaging.EnvelopeWriter.MIME_TYPE_ENTRY_NAME;


/**
 * Helper that manages different {@link ContentHandler} instances and parsed in envelope content.
 * Converts entries in ParsingStore to appropriate {@link com.guardtime.envelope.EnvelopeElement} by using different
 * {@link ContentHandler}s.
 */
class EnvelopeElementExtractor {
    private final ParsingStore parsingStore;
    private final MimeTypeHandler mimeTypeHandler;
    private final ManifestHandler manifestHandler;
    private final DocumentsManifestHandler documentsManifestHandler;
    private final AnnotationsManifestHandler annotationsManifestHandler;
    private final SingleAnnotationManifestHandler singleAnnotationManifestHandler;
    private final SignatureHandler signatureHandler;
    private final Set<String> requestedKeys = new HashSet<>();

    EnvelopeElementExtractor(EnvelopeManifestFactory manifestFactory, SignatureFactory signatureFactory, ParsingStore store) {
        this.mimeTypeHandler = new MimeTypeHandler();
        this.manifestHandler = new ManifestHandler(manifestFactory);
        this.documentsManifestHandler = new DocumentsManifestHandler(manifestFactory);
        this.annotationsManifestHandler = new AnnotationsManifestHandler(manifestFactory);
        this.singleAnnotationManifestHandler = new SingleAnnotationManifestHandler(manifestFactory);
        this.signatureHandler = new SignatureHandler(signatureFactory);
        this.parsingStore = store;

    }

    public List<UnknownDocument> getUnrequestedFiles() {
        List<UnknownDocument> returnable = new ArrayList<>();
        Set<String> keys = new HashSet<>(parsingStore.getStoredKeys());
        keys.removeAll(requestedKeys);
        for (String key : keys) {
            returnable.add(new ParsedDocument(parsingStore, key, "unknown", key));
        }
        return returnable;
    }

    public Set<String> getManifestUris() {
        Set<String> returnable = new HashSet<>();
        for (String key : parsingStore.getStoredKeys()) {
            if (manifestHandler.isSupported(key)) {
                returnable.add(key);
            }
        }
        return returnable;
    }

    public SingleAnnotationManifest getSingleAnnotationManifest(String uri) throws ContentParsingException {
        return parse(singleAnnotationManifestHandler, uri);
    }

    public byte[] getMimeTypeContent() throws ContentParsingException {
        return parse(mimeTypeHandler, MIME_TYPE_ENTRY_NAME);
    }

    public Manifest getManifest(String manifestPath) throws ContentParsingException {
        return parse(manifestHandler, manifestPath);
    }

    public DocumentsManifest getDocumentsManifest(String manifestPath) throws ContentParsingException {
        return parse(documentsManifestHandler, manifestPath);
    }

    public AnnotationsManifest getAnnotationsManifest(String manifestPath) throws ContentParsingException {
        return parse(annotationsManifestHandler, manifestPath);
    }

    public EnvelopeSignature getEnvelopeSignature(String path) throws ContentParsingException {
        return parse(signatureHandler, path);
    }

    public InputStream getDocumentStream(String path) throws ContentParsingException {
        return getInputStream(path);
    }

    public InputStream getAnnotationDataStream(String path) throws ContentParsingException {
        return getInputStream(path);
    }

    private  <T> T parse(ContentHandler<T> handler, String path) throws ContentParsingException {
        try (InputStream stream = getInputStream(path)) {
            return handler.parse(stream, path);
        } catch (IOException e) {
            throw new ContentParsingException("Failed to access data for '" + path + "'!");
        }
    }

    private InputStream getInputStream(String path) throws ContentParsingException {
        requestedKeys.add(path);
        if (!parsingStore.contains(path)) {
            throw new ContentParsingException("No content stored for entry '" + path + "'!");
        }
        return parsingStore.get(path);
    }

    /**
     * Clears parsingStore of content that has already been parsed.
     */
    public void clear() {
        for (String key : requestedKeys) {
            parsingStore.remove(key);
        }
    }
}
