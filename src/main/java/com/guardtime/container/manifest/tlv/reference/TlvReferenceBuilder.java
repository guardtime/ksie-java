package com.guardtime.container.manifest.tlv.reference;

import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.tlv.TLVHeader;
import com.guardtime.ksi.tlv.TLVParserException;

import java.util.LinkedList;
import java.util.List;

import static com.guardtime.container.manifest.tlv.reference.FileReference.*;

public class TlvReferenceBuilder {

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
