package com.guardtime.container.manifest.tlv;

import com.guardtime.container.hash.HashAlgorithmProvider;
import com.guardtime.container.manifest.AnnotationsManifest;
import com.guardtime.container.util.DataHashException;
import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.tlv.TLVParserException;

class TlvAnnotationsManifestReference extends TlvFileReference {

    public static final int ANNOTATIONS_MANIFEST_REFERENCE = 0xb02;
    private static final String ANNOTATIONS_MANIFEST_TYPE = "ksie10/annotmanifest";

    public TlvAnnotationsManifestReference(TLVElement rootElement) throws TLVParserException {
        super(rootElement);
    }

    public TlvAnnotationsManifestReference(String uri, AnnotationsManifest annotationsManifest, HashAlgorithmProvider algorithmProvider) throws TLVParserException, DataHashException {
        super(uri, generateHashes(annotationsManifest, algorithmProvider.getFileReferenceHashAlgorithms()), ANNOTATIONS_MANIFEST_TYPE);
    }

    @Override
    public int getElementType() {
        return ANNOTATIONS_MANIFEST_REFERENCE;
    }

}
