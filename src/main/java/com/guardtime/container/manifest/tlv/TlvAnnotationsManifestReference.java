package com.guardtime.container.manifest.tlv;

import com.guardtime.container.hash.HashAlgorithmProvider;
import com.guardtime.container.manifest.AnnotationsManifest;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;
import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.tlv.TLVParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class TlvAnnotationsManifestReference extends TlvFileReference {

    public static final int ANNOTATIONS_MANIFEST_REFERENCE = 0xb02;
    private static final String ANNOTATIONS_MANIFEST_TYPE = "ksie10/annotmanifest";

    public TlvAnnotationsManifestReference(TLVElement rootElement) throws TLVParserException {
        super(rootElement);
    }

    public TlvAnnotationsManifestReference(String uri, AnnotationsManifest annotationsManifest, HashAlgorithmProvider algorithmProvider) throws TLVParserException, IOException {
        super(uri, generateHashes(annotationsManifest, algorithmProvider), ANNOTATIONS_MANIFEST_TYPE);
    }

    @Override
    public int getElementType() {
        return ANNOTATIONS_MANIFEST_REFERENCE;
    }

    private static List<DataHash> generateHashes(AnnotationsManifest manifest, HashAlgorithmProvider algorithmProvider) throws IOException {
        List<DataHash> hashList = new ArrayList<>();
        for (HashAlgorithm algorithm : algorithmProvider.getFileReferenceHashAlgorithms()) {
            hashList.add(manifest.getDataHash(algorithm));
        }
        return hashList;
    }
}
