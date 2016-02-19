package com.guardtime.container.manifest.reference.tlv;

import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.tlv.TLVHeader;
import com.guardtime.ksi.tlv.TLVParserException;

import java.util.LinkedList;
import java.util.List;

class TlvReferenceBuilder {
    public static final int URI_TYPE = 0x1;
    public static final int HASH_TYPE = 0x2;
    public static final int MIME_TYPE = 0x3;
    public static final int DOMAIN_TYPE = 0x4;

    private int type;
    private List<TLVElement> elements = new LinkedList<>();

    public TlvReferenceBuilder() {
    }

    public TlvReferenceBuilder withType(int type) {
        this.type = type;
        return this;
    }

    public TlvReferenceBuilder withUriElement(String uri) throws TLVParserException {
        TLVElement element = new TLVElement(new TLVHeader(false, false, URI_TYPE));
        element.setStringContent(uri);
        this.elements.add(element);
        return this;
    }

    public TlvReferenceBuilder withHashElement(DataHash hash) throws TLVParserException {
        TLVElement element = new TLVElement(new TLVHeader(false, false, HASH_TYPE));
        element.setDataHashContent(hash);
        this.elements.add(element);
        return this;
    }

    public TlvReferenceBuilder withMimeTypeElement(String mimeType) throws TLVParserException {
        TLVElement element = new TLVElement(new TLVHeader(false, false, MIME_TYPE));
        element.setStringContent(mimeType);
        this.elements.add(element);
        return this;
    }

    public TlvReferenceBuilder withDomainElement(String domain) throws TLVParserException {
        TLVElement element = new TLVElement(new TLVHeader(false, false, DOMAIN_TYPE));
        element.setStringContent(domain);
        this.elements.add(element);
        return this;
    }

    public TLVElement build() throws TLVParserException {
        TLVElement element = new TLVElement(new TLVHeader(false, false, type));
        for (TLVElement elem : elements) {
            element.addChildElement(elem);
        }
        return element;
    }
}
