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

package com.guardtime.container.manifest.tlv;

import com.guardtime.container.document.ContainerDocument;
import com.guardtime.container.hash.HashAlgorithmProvider;
import com.guardtime.container.manifest.DocumentsManifest;
import com.guardtime.container.manifest.InvalidManifestException;
import com.guardtime.container.util.DataHashException;
import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.tlv.TLVInputStream;
import com.guardtime.ksi.tlv.TLVParserException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

class TlvDocumentsManifest extends AbstractTlvManifestStructure implements DocumentsManifest {

    private static final byte[] MAGIC = "KSIEDAMF".getBytes(StandardCharsets.UTF_8);

    private List<TlvDocumentReference> documents = new LinkedList<>();

    public TlvDocumentsManifest(List<ContainerDocument> documents, HashAlgorithmProvider algorithmProvider) throws InvalidManifestException {
        super(MAGIC);
        try {
            for (ContainerDocument doc : documents) {
                this.documents.add(new TlvDocumentReference(doc, algorithmProvider));
            }
        } catch (DataHashException | TLVParserException | IOException e) {
            throw new InvalidManifestException("Failed to generate TlvDocumentsManifest", e);
        }
    }

    public TlvDocumentsManifest(InputStream stream) throws InvalidManifestException {
        super(MAGIC, stream);
        try {
            read(stream);
        } catch (TLVParserException e) {
            throw new InvalidManifestException("Failed to parse TlvDocumentsManifest from InputStream", e);
        } catch (IOException e) {
            throw new InvalidManifestException("Failed to read InputStream", e);
        }
    }

    @Override
    public List<TlvDocumentReference> getDocumentReferences() {
        return documents;
    }

    @Override
    protected List<TlvDocumentReference> getElements() {
        return documents;
    }

    private void read(InputStream stream) throws TLVParserException, IOException {
        TLVInputStream input = toTlvInputStream(stream);
        TLVElement element;
        while (input.hasNextElement()) {
            element = input.readElement();
            switch (element.getType()) {
                case TlvDocumentReference.DATA_FILE_REFERENCE:
                    documents.add(new TlvDocumentReference(element));
                    break;
                default:
                    verifyCriticalFlag(element);
            }
        }
    }

}
