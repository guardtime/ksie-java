package com.guardtime.container.manifest.tlv;

import com.guardtime.container.hash.HashAlgorithmProvider;
import com.guardtime.container.manifest.DocumentsManifest;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;
import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.tlv.TLVParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class TlvDocumentsManifestReference extends TlvFileReference {

    public static final int DOCUMENTS_MANIFEST_REFERENCE = 0xb01;
    private static final String DATA_FILES_MANIFEST = "ksie10/datamanifest";

    public TlvDocumentsManifestReference(TLVElement rootElement) throws TLVParserException {
        super(rootElement);
    }

    public TlvDocumentsManifestReference(DocumentsManifest documentsManifest, String uri, HashAlgorithmProvider algorithmProvider) throws TLVParserException, IOException {
        super(uri, generateHashes(documentsManifest, algorithmProvider), DATA_FILES_MANIFEST);
    }

    @Override
    public int getElementType() {
        return DOCUMENTS_MANIFEST_REFERENCE;
    }

    private static List<DataHash> generateHashes(DocumentsManifest manifest, HashAlgorithmProvider algorithmProvider) throws IOException {
        List<DataHash> hashList = new ArrayList<>();
        for (HashAlgorithm algorithm : algorithmProvider.getFileReferenceHashAlgorithms()) {
            hashList.add(manifest.getDataHash(algorithm));
        }
        return hashList;
    }
}
