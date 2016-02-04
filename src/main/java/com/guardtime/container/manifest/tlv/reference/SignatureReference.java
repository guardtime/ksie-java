package com.guardtime.container.manifest.tlv.reference;

import com.guardtime.container.manifest.ContainerManifestMimeType;
import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.tlv.TLVParserException;

public class SignatureReference extends FileReference {
    public static final int SIGNATURE_REFERENCE = 0xb06;

    public SignatureReference(TLVElement rootElement) throws TLVParserException {
        super(rootElement);
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

    @Override
    protected Object[] getMandatoryElements() {
        return new Object[]{uri, mimeType};
    }

    @Override
    public int getElementType() {
        return SIGNATURE_REFERENCE;
    }
}
