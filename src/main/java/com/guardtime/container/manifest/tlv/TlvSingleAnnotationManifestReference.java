package com.guardtime.container.manifest.tlv;

import com.guardtime.container.annotation.ContainerAnnotationType;
import com.guardtime.container.hash.HashAlgorithmProvider;
import com.guardtime.container.manifest.SingleAnnotationManifest;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;
import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.tlv.TLVParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class TlvSingleAnnotationManifestReference extends TlvFileReference {

    public static final int ANNOTATION_INFO_REFERENCE = 0xb04;

    public TlvSingleAnnotationManifestReference(TLVElement rootElement) throws TLVParserException {
        super(rootElement);
    }

    public TlvSingleAnnotationManifestReference(String uri, TlvSingleAnnotationManifest singleAnnotationManifest, ContainerAnnotationType annotationType, HashAlgorithmProvider algorithmProvider) throws TLVParserException, IOException {
        super(uri, generateHashes(singleAnnotationManifest, algorithmProvider), annotationType.getContent());
    }

    @Override
    public int getElementType() {
        return ANNOTATION_INFO_REFERENCE;
    }

    private static List<DataHash> generateHashes(SingleAnnotationManifest manifest, HashAlgorithmProvider algorithmProvider) throws IOException {
        List<DataHash> hashList = new ArrayList<>();
        for (HashAlgorithm algorithm : algorithmProvider.getFileReferenceHashAlgorithms()) {
            hashList.add(manifest.getDataHash(algorithm));
        }
        return hashList;
    }
}
