package com.guardtime.container.manifest.tlv;

import com.guardtime.container.manifest.AnnotationsManifest;
import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.tlv.TLVParserException;

import java.io.IOException;

class TlvAnnotationsManifestReference extends TlvFileReference {

    public static final int ANNOTATIONS_MANIFEST_REFERENCE = 0xb02;
    private static final String ANNOTATIONS_MANIFEST_TYPE = "ksie10/annotmanifest";

    public TlvAnnotationsManifestReference(TLVElement rootElement) throws TLVParserException {
        super(rootElement);
    }

    public TlvAnnotationsManifestReference(String uri, AnnotationsManifest annotationsManifest) throws TLVParserException, IOException {
        super(uri, annotationsManifest.getDataHash(DEFAULT_HASH_ALGORITHM), ANNOTATIONS_MANIFEST_TYPE);
    }

    @Override
    public int getElementType() {
        return ANNOTATIONS_MANIFEST_REFERENCE;
    }
}
