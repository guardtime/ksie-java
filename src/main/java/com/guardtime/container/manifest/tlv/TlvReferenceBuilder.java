package com.guardtime.container.manifest.tlv;

import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.tlv.TLVHeader;
import com.guardtime.ksi.tlv.TLVParserException;

import java.util.LinkedList;
import java.util.List;

class TlvReferenceBuilder {
    private int type;
    private List<TLVElement> elements = new LinkedList<>();

    public TlvReferenceBuilder() {
    }

    public TlvReferenceBuilder withType(int type) {
        this.type = type;
        return this;
    }

    public TlvReferenceBuilder withType(TlvTypes type) {
        this.type = type.getType();
        return this;
    }

    public TlvReferenceBuilder withUriElement(String uri) throws TLVParserException {
        TLVElement element = new TLVElement(new TLVHeader(false, false, 0x1));
        element.setStringContent(uri);
        this.elements.add(element);
        return this;
    }

    public TlvReferenceBuilder withHashElement(DataHash hash) throws TLVParserException {
        TLVElement element = new TLVElement(new TLVHeader(false, false, 0x2));
        element.setDataHashContent(hash);
        this.elements.add(element);
        return this;
    }

    public TlvReferenceBuilder withMimeTypeElement(String mimeType) throws TLVParserException {
        TLVElement element = new TLVElement(new TLVHeader(false, false, 0x3));
        element.setStringContent(mimeType);
        this.elements.add(element);
        return this;
    }

    public TlvReferenceBuilder withDomainElement(String domain) throws TLVParserException {
        TLVElement element = new TLVElement(new TLVHeader(false, false, 0x4));
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
