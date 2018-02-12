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

package com.guardtime.envelope.manifest.tlv;

import com.guardtime.envelope.EnvelopeElement;
import com.guardtime.envelope.manifest.FileReference;
import com.guardtime.envelope.util.DataHashException;
import com.guardtime.envelope.util.Util;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;
import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.tlv.TLVParserException;
import com.guardtime.ksi.tlv.TLVStructure;

import java.util.ArrayList;
import java.util.List;

abstract class TlvFileReference extends TLVStructure implements FileReference {

    private String uri;
    private List<DataHash> hashList = new ArrayList<>();
    private String mimeType;

    TlvFileReference(TLVElement rootElement) throws TLVParserException {
        super(rootElement);
        for (TLVElement element : rootElement.getChildElements()) {
            switch (element.getType()) {
                case TlvReferenceBuilder.URI_TYPE:
                    this.uri = readOnce(element).getDecodedString();
                    break;
                case TlvReferenceBuilder.HASH_TYPE:
                    this.hashList.add(element.getDecodedDataHash());
                    break;
                case TlvReferenceBuilder.MIME_TYPE:
                    this.mimeType = readOnce(element).getDecodedString();
                    break;
                default:
                    verifyCriticalFlag(element);
            }
        }
    }

    TlvFileReference(String uri, List<DataHash> dataHashList, String mimeType) throws TLVParserException {
        Util.notEmpty(dataHashList, "Data hashes");
        this.uri = uri;
        this.hashList.addAll(dataHashList);
        this.mimeType = mimeType;
        TlvReferenceBuilder tlvReferenceBuilder = new TlvReferenceBuilder()
                .withType(getElementType())
                .withUriElement(uri);
        for (DataHash dataHash : dataHashList) {
            tlvReferenceBuilder.withHashElement(dataHash);
        }
        this.rootElement = tlvReferenceBuilder
                .withMimeTypeElement(mimeType)
                .build();
    }

    protected static List<DataHash> generateHashes(EnvelopeElement envelopeElement, List<HashAlgorithm> hashAlgorithms)
            throws DataHashException {
        Util.notNull(hashAlgorithms, "Hash algorithm list");
        List<DataHash> hashList = new ArrayList<>();
        for (HashAlgorithm algorithm : hashAlgorithms) {
            hashList.add(envelopeElement.getDataHash(algorithm));
        }
        return hashList;
    }

    public String getUri() {
        return uri;
    }

    public String getMimeType() {
        return mimeType;
    }

    public List<DataHash> getHashList() {
        return hashList;
    }
}
