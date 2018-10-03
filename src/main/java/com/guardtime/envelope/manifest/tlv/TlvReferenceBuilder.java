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

import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.tlv.TLVParserException;

import java.util.LinkedList;
import java.util.List;

class TlvReferenceBuilder {

    public static final int URI_TYPE = 0x1;
    public static final int HASH_TYPE = 0x2;
    public static final int MIME_TYPE = 0x3;
    public static final int DOMAIN_TYPE = 0x4;

    private final List<TLVElement> elements = new LinkedList<>();
    private int type;

    public TlvReferenceBuilder withType(int type) {
        this.type = type;
        return this;
    }

    public TlvReferenceBuilder withUriElement(String uri) throws TLVParserException {
        return withElement(TLVElement.create(URI_TYPE, uri));
    }

    public TlvReferenceBuilder withHashElement(DataHash hash) throws TLVParserException {
        return withElement(TLVElement.create(HASH_TYPE, hash));
    }

    public TlvReferenceBuilder withMimeTypeElement(String mimeType) throws TLVParserException {
        return withElement(TLVElement.create(MIME_TYPE, mimeType));
    }

    public TlvReferenceBuilder withDomainElement(String domain) throws TLVParserException {
        return withElement(TLVElement.create(DOMAIN_TYPE, domain));
    }

    public TLVElement build() throws TLVParserException {
        TLVElement element = new TLVElement(false, false, type);
        for (TLVElement elem : elements) {
            element.addChildElement(elem);
        }
        return element;
    }

    private TlvReferenceBuilder withElement(TLVElement element) throws TLVParserException {
        this.elements.add(element);
        return this;
    }
}
