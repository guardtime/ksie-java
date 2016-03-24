package com.guardtime.container.manifest.tlv;

import com.guardtime.container.annotation.ContainerAnnotationType;
import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.tlv.TLVParserException;

import java.io.IOException;

class TlvAnnotationInfoManifestReference extends TlvFileReference {

    private static final int ANNOTATION_INFO_REFERENCE = 0xb04;

    public TlvAnnotationInfoManifestReference(TLVElement rootElement) throws TLVParserException {
        super(rootElement);
    }

    public TlvAnnotationInfoManifestReference(String uri, TlvAnnotationInfoManifest manifest, ContainerAnnotationType annotationType) throws TLVParserException, IOException {
        super(uri, manifest.getDataHash(DEFAULT_HASH_ALGORITHM), annotationType.getContent());
    }

    @Override
    public int getElementType() {
        return ANNOTATION_INFO_REFERENCE;
    }

}
