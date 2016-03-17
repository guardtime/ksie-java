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
