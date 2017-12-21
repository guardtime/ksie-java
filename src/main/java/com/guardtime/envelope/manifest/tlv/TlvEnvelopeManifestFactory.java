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

package com.guardtime.envelope.manifest.tlv;

import com.guardtime.envelope.annotation.Annotation;
import com.guardtime.envelope.document.Document;
import com.guardtime.envelope.hash.HashAlgorithmProvider;
import com.guardtime.envelope.hash.SingleHashAlgorithmProvider;
import com.guardtime.envelope.manifest.AnnotationsManifest;
import com.guardtime.envelope.manifest.DocumentsManifest;
import com.guardtime.envelope.manifest.EnvelopeManifestFactory;
import com.guardtime.envelope.manifest.InvalidManifestException;
import com.guardtime.envelope.signature.SignatureFactoryType;
import com.guardtime.envelope.util.Util;
import com.guardtime.ksi.hashing.HashAlgorithm;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Creates and parses manifests with TLV (Type Length Value) structure.
 */
public class TlvEnvelopeManifestFactory implements
        EnvelopeManifestFactory<TlvManifest, TlvDocumentsManifest, TlvAnnotationsManifest, TlvSingleAnnotationManifest> {

    private static final HashAlgorithm DEFAULT_HASH_ALGORITHM = HashAlgorithm.SHA2_256;
    protected static final TlvManifestFactoryType TLV_MANIFEST_FACTORY_TYPE = new TlvManifestFactoryType(
            "TLV manifest factory",
            "tlv"
    );

    private final HashAlgorithmProvider algorithmProvider;

    public TlvEnvelopeManifestFactory() {
        this(new SingleHashAlgorithmProvider(DEFAULT_HASH_ALGORITHM));
    }

    /**
     * @param algorithmProvider
     *         defines what algorithm(s) will be used to generate {@link com.guardtime.ksi.hashing.DataHash}es used in
     *         {@link com.guardtime.envelope.manifest.FileReference}es
     */
    public TlvEnvelopeManifestFactory(HashAlgorithmProvider algorithmProvider) {
        Util.notNull(algorithmProvider, "Hash algorithm provider");
        this.algorithmProvider = algorithmProvider;
    }

    @Override
    public HashAlgorithmProvider getHashAlgorithmProvider() {
        return this.algorithmProvider;
    }

    @Override
    public TlvManifest createManifest(DocumentsManifest documentsManifest, AnnotationsManifest annotationManifest,
                                      SignatureFactoryType factoryType, String signatureName, String manifestName) throws InvalidManifestException {
        Util.notNull(documentsManifest, "Documents manifest");
        Util.notNull(annotationManifest, "Annotations manifest");
        return new TlvManifest(
                documentsManifest,
                annotationManifest,
                signatureName,
                factoryType,
                algorithmProvider,
                manifestName
        );
    }

    @Override
    public TlvDocumentsManifest createDocumentsManifest(List<Document> files, String documentsManifestName)
            throws InvalidManifestException {
        Util.notNull(files, "Document files list");
        Util.notEmpty(files, "Document files list");
        return new TlvDocumentsManifest(files, algorithmProvider, documentsManifestName);
    }

    @Override
    public TlvAnnotationsManifest createAnnotationsManifest(Map<Annotation, TlvSingleAnnotationManifest> annotationManifests,
                                                            String annotationsManifestName)
            throws InvalidManifestException {
        return new TlvAnnotationsManifest(annotationManifests, algorithmProvider, annotationsManifestName);
    }

    @Override
    public TlvSingleAnnotationManifest createSingleAnnotationManifest(DocumentsManifest documentsManifest,
                                                                      Annotation annotation, String singleAnnotationManifestName)
            throws InvalidManifestException {
        Util.notNull(documentsManifest, "Documents manifest");
        Util.notNull(annotation, "Annotation");
        return new TlvSingleAnnotationManifest(
                annotation,
                documentsManifest,
                algorithmProvider,
                singleAnnotationManifestName
        );
    }

    @Override
    public TlvManifest readManifest(InputStream input, String path) throws InvalidManifestException {
        Util.notNull(input, "Input stream");
        return new TlvManifest(input, path);
    }

    @Override
    public TlvDocumentsManifest readDocumentsManifest(InputStream input, String path) throws InvalidManifestException {
        Util.notNull(input, "Input stream");
        return new TlvDocumentsManifest(input, path);
    }

    @Override
    public TlvAnnotationsManifest readAnnotationsManifest(InputStream input, String path) throws InvalidManifestException {
        Util.notNull(input, "Input stream");
        return new TlvAnnotationsManifest(input, path);
    }

    @Override
    public TlvSingleAnnotationManifest readSingleAnnotationManifest(InputStream input, String path)
            throws InvalidManifestException {
        Util.notNull(input, "Input stream");
        return new TlvSingleAnnotationManifest(input, path);
    }

    @Override
    public TlvManifestFactoryType getManifestFactoryType() {
        return TLV_MANIFEST_FACTORY_TYPE;
    }

}
