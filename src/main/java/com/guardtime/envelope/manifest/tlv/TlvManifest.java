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

import com.guardtime.envelope.hash.HashAlgorithmProvider;
import com.guardtime.envelope.manifest.FileReference;
import com.guardtime.envelope.manifest.InvalidManifestException;
import com.guardtime.envelope.manifest.Manifest;
import com.guardtime.envelope.manifest.ManifestFactoryType;
import com.guardtime.envelope.util.DataHashException;
import com.guardtime.envelope.util.Pair;
import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.tlv.TLVInputStream;
import com.guardtime.ksi.tlv.TLVParserException;
import com.guardtime.ksi.tlv.TLVStructure;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static java.util.Arrays.asList;

class TlvManifest extends AbstractTlvManifestStructure implements Manifest {

    private static final byte[] MAGIC = "KSIEMFST".getBytes(StandardCharsets.UTF_8);

    private TlvDocumentsManifestReference documentsManifestReference;
    private TlvSignatureReference signatureReference;
    private TlvAnnotationsManifestReference annotationsManifestReference;

    public TlvManifest(Pair<String, TlvDocumentsManifest> documentsManifest, Pair<String, TlvAnnotationsManifest> annotationsManifest, Pair<String, String> signatureReference, HashAlgorithmProvider algorithmProvider) throws InvalidManifestException {
        super(MAGIC);
        try {
            this.documentsManifestReference = new TlvDocumentsManifestReference(documentsManifest.getRight(), documentsManifest.getLeft(), algorithmProvider);
            this.signatureReference = new TlvSignatureReference(signatureReference.getLeft(), signatureReference.getRight());
            this.annotationsManifestReference = new TlvAnnotationsManifestReference(annotationsManifest.getLeft(), annotationsManifest.getRight(), algorithmProvider);
        } catch (TLVParserException | DataHashException e) {
            throw new InvalidManifestException("Failed to generate file reference TLVElement", e);
        }
    }

    public TlvManifest(InputStream stream) throws InvalidManifestException {
        super(MAGIC, stream);
        try {
            TLVInputStream inputStream = toTlvInputStream(stream);
            read(inputStream);
        } catch (TLVParserException e) {
            throw new InvalidManifestException("Failed to parse content of InputStream", e);
        } catch (IOException e) {
            throw new InvalidManifestException("Failed to read InputStream", e);
        }
        checkMandatoryElement(documentsManifestReference, "Documents manifest reference");
        checkMandatoryElement(signatureReference, "Signature reference");
        checkMandatoryElement(annotationsManifestReference, "Annotations manifest reference");
    }

    public FileReference getDocumentsManifestReference() {
        return documentsManifestReference;
    }

    @Override
    public FileReference getAnnotationsManifestReference() {
        return annotationsManifestReference;
    }

    @Override
    public com.guardtime.envelope.manifest.SignatureReference getSignatureReference() {
        return signatureReference;
    }

    @Override
    public ManifestFactoryType getManifestFactoryType() {
        return TlvEnvelopeManifestFactory.TLV_MANIFEST_FACTORY_TYPE;
    }

    @Override
    protected List<TLVStructure> getElements() {
        return asList(documentsManifestReference, signatureReference, annotationsManifestReference);
    }

    private void read(TLVInputStream inputStream) throws IOException, TLVParserException {
        TLVElement element;
        while (inputStream.hasNextElement()) {
            element = inputStream.readElement();
            switch (element.getType()) {
                case TlvDocumentsManifestReference.DOCUMENTS_MANIFEST_REFERENCE:
                    documentsManifestReference = new TlvDocumentsManifestReference(readOnce(element));
                    break;
                case TlvSignatureReference.SIGNATURE_REFERENCE:
                    signatureReference = new TlvSignatureReference(readOnce(element));
                    break;
                case TlvAnnotationsManifestReference.ANNOTATIONS_MANIFEST_REFERENCE:
                    annotationsManifestReference = new TlvAnnotationsManifestReference(readOnce(element));
                    break;
                default:
                    verifyCriticalFlag(element);
            }
        }
    }

}
