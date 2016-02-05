package com.guardtime.container.manifest.tlv.reference;

import com.guardtime.container.manifest.ContainerManifestMimeType;
import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.tlv.TLVParserException;
import com.guardtime.ksi.tlv.TLVStructure;

import static com.guardtime.container.manifest.tlv.reference.TlvReferenceBuilder.MIME_TYPE;
import static com.guardtime.container.manifest.tlv.reference.TlvReferenceBuilder.URI_TYPE;

public class SignatureReference extends TLVStructure {
    public static final int SIGNATURE_REFERENCE = 0xb06;

    private String uri;

    public SignatureReference(TLVElement rootElement) throws TLVParserException {
        super(rootElement);
        for (TLVElement element : rootElement.getChildElements()) {
            switch (element.getType()) {
                case URI_TYPE:
                    this.uri = readOnce(element).getDecodedString();
                    break;
                case MIME_TYPE:
                    String mimetype = readOnce(element).getDecodedString();
                    if (!mimetype.equals(ContainerManifestMimeType.SIGNATURE.getType())) {
                        throw new TLVParserException("Invalid MIME type for signature");
                    }
                    break;
                default:
                    verifyCriticalFlag(element);
            }
        }
    }

    public SignatureReference(String uri) throws TLVParserException {
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
