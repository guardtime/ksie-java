package com.guardtime.container.manifest.tlv;

import com.guardtime.container.manifest.DocumentsManifest;
import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.tlv.TLVParserException;

import java.io.IOException;

class TlvDocumentsManifestReference extends TlvFileReference {

    public static final int DOCUMENTS_MANIFEST_REFERENCE = 0xb01;
    private static final String DATA_FILES_MANIFEST = "ksie10/datamanifest";

    public TlvDocumentsManifestReference(TLVElement rootElement) throws TLVParserException {
        super(rootElement);
    }

    public TlvDocumentsManifestReference(DocumentsManifest documentsManifest, String uri) throws TLVParserException, IOException {
        super(uri, documentsManifest.getDataHash(DEFAULT_HASH_ALGORITHM), DATA_FILES_MANIFEST);
    }

    @Override
    public int getElementType() {
        return DOCUMENTS_MANIFEST_REFERENCE;
    }

}
