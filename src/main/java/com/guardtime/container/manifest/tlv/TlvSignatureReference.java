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

import com.guardtime.container.manifest.SignatureReference;
import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.tlv.TLVParserException;
import com.guardtime.ksi.tlv.TLVStructure;

class TlvSignatureReference extends TLVStructure implements SignatureReference {

    public static final int SIGNATURE_REFERENCE = 0xb06;

    private String uri;
    private String type;

    public TlvSignatureReference(TLVElement rootElement) throws TLVParserException {
        super(rootElement);
        for (TLVElement element : rootElement.getChildElements()) {
            switch (element.getType()) {
                case TlvReferenceBuilder.URI_TYPE:
                    this.uri = readOnce(element).getDecodedString();
                    break;
                case TlvReferenceBuilder.MIME_TYPE:
                    this.type = readOnce(element).getDecodedString();
                    break;
                default:
                    verifyCriticalFlag(element);
            }
        }
    }

    public TlvSignatureReference(String uri, String type) throws TLVParserException {
        this.uri = uri;
        this.type = type;
        this.rootElement = new TlvReferenceBuilder().
                withType(SIGNATURE_REFERENCE).
                withUriElement(uri).
                withMimeTypeElement(type).
                build();
    }

    public String getUri() {
        return uri;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public int getElementType() {
        return SIGNATURE_REFERENCE;
    }

}
