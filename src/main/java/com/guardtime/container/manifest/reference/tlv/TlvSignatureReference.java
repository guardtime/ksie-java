package com.guardtime.container.manifest.reference.tlv;

import com.guardtime.container.manifest.ContainerManifestMimeType;
import com.guardtime.container.manifest.reference.SignatureReference;
import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.tlv.TLVParserException;
import com.guardtime.ksi.tlv.TLVStructure;

public class TlvSignatureReference extends TLVStructure implements SignatureReference {
    public static final int SIGNATURE_REFERENCE = 0xb06;

    private String uri;

    public TlvSignatureReference(TLVElement rootElement) throws TLVParserException {
        super(rootElement);
        for (TLVElement element : rootElement.getChildElements()) {
            switch (element.getType()) {
                case TlvReferenceBuilder.URI_TYPE:
                    this.uri = readOnce(element).getDecodedString();
                    break;
                case TlvReferenceBuilder.MIME_TYPE:
                    String mimeType = readOnce(element).getDecodedString();
                    if (!mimeType.equals(ContainerManifestMimeType.SIGNATURE.getType())) {
                        throw new TLVParserException("Invalid MIME type for signature");
                    }
                    break;
                default:
                    verifyCriticalFlag(element);
            }
        }
    }

    public TlvSignatureReference(String uri) throws TLVParserException {
        this(
                new TlvReferenceBuilder().
                        withType(SIGNATURE_REFERENCE).
                        withUriElement(uri).
                        withMimeTypeElement(ContainerManifestMimeType.SIGNATURE.getType()).
                        build()

        );
    }

    public String getUri() {
        return uri;
    }

    @Override
    public int getElementType() {
        return SIGNATURE_REFERENCE;
    }
}
