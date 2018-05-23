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
import com.guardtime.envelope.packaging.parsing.store.ParsingStoreReference;
import com.guardtime.envelope.signature.EnvelopeSignature;
import com.guardtime.envelope.signature.SignatureFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import static com.guardtime.envelope.packaging.EnvelopeWriter.MIME_TYPE_ENTRY_NAME;


/**
 * Helper that manages different {@link ContentHandler} instances and parsed envelope contents.
 * Converts entries in ParsingStore to appropriate {@link com.guardtime.envelope.EnvelopeElement} by using different
 * {@link ContentHandler}s.
 */
class EnvelopeElementExtractor {
    private final ParsingStoreHandler parsingStoreHandler;
    private final MimeTypeHandler mimeTypeHandler;
    private final ManifestHandler manifestHandler;
    private final DocumentsManifestHandler documentsManifestHandler;
    private final AnnotationsManifestHandler annotationsManifestHandler;
    private final SingleAnnotationManifestHandler singleAnnotationManifestHandler;
    private final SignatureHandler signatureHandler;

    EnvelopeElementExtractor(EnvelopeManifestFactory manifestFactory, SignatureFactory signatureFactory,
                             ParsingStoreHandler storeHandler) {
        this.mimeTypeHandler = new MimeTypeHandler();
        this.manifestHandler = new ManifestHandler(manifestFactory);
        this.documentsManifestHandler = new DocumentsManifestHandler(manifestFactory);
        this.annotationsManifestHandler = new AnnotationsManifestHandler(manifestFactory);
        this.singleAnnotationManifestHandler = new SingleAnnotationManifestHandler(manifestFactory);
        this.signatureHandler = new SignatureHandler(signatureFactory);
        this.parsingStoreHandler = storeHandler;

    }

    public Set<String> getManifestUris() {
        Set<String> returnable = new HashSet<>();
        for (String key : parsingStoreHandler.getStoredKeys()) {
            if (manifestHandler.isSupported(key)) {
                returnable.add(key);
            }
        }
        return returnable;
    }

    public SingleAnnotationManifest getSingleAnnotationManifest(String uri) throws ContentParsingException {
        return parseAndUnstore(singleAnnotationManifestHandler, uri);
    }

    public byte[] getMimeTypeContent() throws ContentParsingException {
        return parseAndUnstore(mimeTypeHandler, MIME_TYPE_ENTRY_NAME);
    }

    public Manifest getManifest(String manifestPath) throws ContentParsingException {
        return parseAndUnstore(manifestHandler, manifestPath);
    }

    public DocumentsManifest getDocumentsManifest(String manifestPath) throws ContentParsingException {
        return parseAndUnstore(documentsManifestHandler, manifestPath);
    }

    public AnnotationsManifest getAnnotationsManifest(String manifestPath) throws ContentParsingException {
        return parseAndUnstore(annotationsManifestHandler, manifestPath);
    }

    public EnvelopeSignature getEnvelopeSignature(String path) throws ContentParsingException {
        return parseAndUnstore(signatureHandler, path);
    }

    private  <T> T parseAndUnstore(ContentHandler<T> handler, String path) throws ContentParsingException {
        ParsingStoreReference parsingStoreReference = getParsingStoreReference(path);
        try (InputStream stream = parsingStoreReference.get()) {
            return handler.parse(stream, path);
        } catch (IOException e) {
            throw new ContentParsingException("Failed to access data for '" + path + "'!");
        } finally {
            parsingStoreReference.unstore();
        }
    }

    public ParsingStoreReference getParsingStoreReference(String path) throws ContentParsingException {
        if (!parsingStoreHandler.contains(path)) {
            throw new ContentParsingException("No content stored for entry '" + path + "'!");
        }
        return parsingStoreHandler.get(path);
    }

}
