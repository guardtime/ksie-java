package com.guardtime.container.manifest.tlv;

import com.guardtime.container.annotation.ContainerAnnotationType;
import com.guardtime.container.hash.HashAlgorithmProvider;
import com.guardtime.container.util.DataHashException;
import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.tlv.TLVParserException;

class TlvSingleAnnotationManifestReference extends TlvFileReference {

    public static final int ANNOTATION_INFO_REFERENCE = 0xb04;

    public TlvSingleAnnotationManifestReference(TLVElement rootElement) throws TLVParserException {
        super(rootElement);
    }

    public TlvSingleAnnotationManifestReference(String uri, TlvSingleAnnotationManifest singleAnnotationManifest, ContainerAnnotationType annotationType, HashAlgorithmProvider algorithmProvider) throws TLVParserException, DataHashException {
        super(uri, generateHashes(singleAnnotationManifest, algorithmProvider.getFileReferenceHashAlgorithms()), annotationType.getContent());
    }

    @Override
    public int getElementType() {
        return ANNOTATION_INFO_REFERENCE;
    }

}
