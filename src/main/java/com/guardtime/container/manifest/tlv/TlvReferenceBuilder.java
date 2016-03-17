package com.guardtime.container.manifest.tlv;

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
        return withStringElementWithType(uri, URI_TYPE);
    }

    public TlvReferenceBuilder withHashElement(DataHash hash) throws TLVParserException {
        TLVElement element = new TLVElement(false, false, HASH_TYPE);
        element.setDataHashContent(hash);
        this.elements.add(element);
        return this;
    }

    public TlvReferenceBuilder withMimeTypeElement(String mimeType) throws TLVParserException {
        return withStringElementWithType(mimeType, MIME_TYPE);
    }

    public TlvReferenceBuilder withDomainElement(String domain) throws TLVParserException {
        return withStringElementWithType(domain, DOMAIN_TYPE);
    }

    public TLVElement build() throws TLVParserException {
        TLVElement element = new TLVElement(false, false, type);
        for (TLVElement elem : elements) {
            element.addChildElement(elem);
        }
        return element;
    }

    private TlvReferenceBuilder withStringElementWithType(String uri, int type) throws TLVParserException {
        TLVElement element = new TLVElement(false, false, type);
        element.setStringContent(uri);
        this.elements.add(element);
        return this;
    }
}
