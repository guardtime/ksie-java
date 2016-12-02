package com.guardtime.container.manifest.tlv;

import com.guardtime.container.hash.HashAlgorithmProvider;
import com.guardtime.container.manifest.DocumentsManifest;
import com.guardtime.container.util.DataHashException;
import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.tlv.TLVParserException;

class TlvDocumentsManifestReference extends TlvFileReference {

    public static final int DOCUMENTS_MANIFEST_REFERENCE = 0xb01;
    private static final String DATA_FILES_MANIFEST = "ksie10/datamanifest";

    public TlvDocumentsManifestReference(TLVElement rootElement) throws TLVParserException {
        super(rootElement);
    }

    public TlvDocumentsManifestReference(DocumentsManifest documentsManifest, String uri, HashAlgorithmProvider algorithmProvider) throws TLVParserException, DataHashException {
        super(uri, generateHashes(documentsManifest, algorithmProvider.getFileReferenceHashAlgorithms()), DATA_FILES_MANIFEST);
    }

    @Override
    public int getElementType() {
        return DOCUMENTS_MANIFEST_REFERENCE;
    }

}
