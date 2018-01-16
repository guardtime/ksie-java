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
import com.guardtime.envelope.hash.HashAlgorithmProvider;
import com.guardtime.envelope.manifest.AnnotationDataReference;
import com.guardtime.envelope.manifest.DocumentsManifest;
import com.guardtime.envelope.manifest.FileReference;
import com.guardtime.envelope.manifest.InvalidManifestException;
import com.guardtime.envelope.manifest.SingleAnnotationManifest;
import com.guardtime.envelope.util.DataHashException;
import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.tlv.TLVInputStream;
import com.guardtime.ksi.tlv.TLVParserException;
import com.guardtime.ksi.tlv.TLVStructure;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static java.util.Arrays.asList;

class TlvSingleAnnotationManifest extends AbstractTlvManifestStructure implements SingleAnnotationManifest {

    private static final byte[] MAGIC = "KSIEANNT".getBytes(StandardCharsets.UTF_8);

    private TlvAnnotationDataReference annotationReference;
    private TlvDocumentsManifestReference documentsManifestReference;
    private String path;

    TlvSingleAnnotationManifest(Annotation annotation, DocumentsManifest documentsManifest,
                                       HashAlgorithmProvider algorithmProvider, String path) throws InvalidManifestException {
        super(MAGIC);
        this.path = path;
        try {
            this.annotationReference = new TlvAnnotationDataReference(annotation, algorithmProvider);
            this.documentsManifestReference = new TlvDocumentsManifestReference(documentsManifest, algorithmProvider);
        } catch (TLVParserException | DataHashException e) {
            throw new InvalidManifestException("Failed to generate file reference TLVElement", e);
        }
    }

    TlvSingleAnnotationManifest(InputStream stream, String path) throws InvalidManifestException {
        super(MAGIC, stream);
        this.path = path;
        try {
            read(stream);
        } catch (TLVParserException e) {
            throw new InvalidManifestException("Failed to parse TlvSingleAnnotationManifest from InputStream", e);
        } catch (IOException e) {
            throw new InvalidManifestException("Failed to read InputStream", e);
        }
        checkMandatoryElement(documentsManifestReference, "Data manifest reference");
        checkMandatoryElement(annotationReference, "Annotation reference");
    }

    private void read(InputStream stream) throws TLVParserException, IOException {
        TLVInputStream input = toTlvInputStream(stream);
        TLVElement element;
        while (input.hasNextElement()) {
            element = input.readElement();
            switch (element.getType()) {
                case TlvDocumentsManifestReference.DOCUMENTS_MANIFEST_REFERENCE:
                    documentsManifestReference = new TlvDocumentsManifestReference(readOnce(element));
                    break;
                case TlvAnnotationDataReference.ANNOTATION_REFERENCE:
                    annotationReference = new TlvAnnotationDataReference(readOnce(element));
                    break;
                default:
                    verifyCriticalFlag(element);
            }
        }
    }

    @Override
    protected List<TLVStructure> getElements() {
        return asList(documentsManifestReference, annotationReference);
    }

    @Override
    public AnnotationDataReference getAnnotationReference() {
        return annotationReference;
    }

    public FileReference getDocumentsManifestReference() {
        return documentsManifestReference;
    }

    @Override
    public String getPath() {
        return path;
    }
}
