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

import com.guardtime.envelope.annotation.Annotation;
import com.guardtime.envelope.hash.HashAlgorithmProvider;
import com.guardtime.envelope.manifest.AnnotationDataReference;
import com.guardtime.envelope.util.DataHashException;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.tlv.TLVParserException;
import com.guardtime.ksi.tlv.TLVStructure;


class TlvAnnotationDataReference extends TLVStructure implements AnnotationDataReference {

    public static final int ANNOTATION_REFERENCE = 0xb05;

    private String uri;
    private DataHash hash;
    private String domain;

    TlvAnnotationDataReference(TLVElement rootElement) throws TLVParserException {
        super(rootElement);
        for (TLVElement element : rootElement.getChildElements()) {
            switch (element.getType()) {
                case TlvReferenceBuilder.URI_TYPE:
                    this.uri = readOnce(element).getDecodedString();
                    break;
                case TlvReferenceBuilder.HASH_TYPE:
                    this.hash = readOnce(element).getDecodedDataHash();
                    break;
                case TlvReferenceBuilder.DOMAIN_TYPE:
                    this.domain = readOnce(element).getDecodedString();
                    break;
                default:
                    verifyCriticalFlag(element);
            }
        }
    }

    TlvAnnotationDataReference(Annotation annotation, HashAlgorithmProvider algorithmProvider)
            throws TLVParserException, DataHashException {
        this.uri = annotation.getPath();
        this.hash = annotation.getDataHash(algorithmProvider.getAnnotationDataReferenceHashAlgorithm());
        this.domain = annotation.getDomain();
        this.rootElement = new TlvReferenceBuilder()
                .withType(ANNOTATION_REFERENCE)
                .withUriElement(uri)
                .withHashElement(hash)
                .withDomainElement(domain)
                .build();
    }

    @Override
    public int getElementType() {
        return ANNOTATION_REFERENCE;
    }

    @Override
    public String getUri() {
        return uri;
    }

    @Override
    public String getDomain() {
        return domain;
    }

    @Override
    public DataHash getHash() {
        return hash;
    }
}
