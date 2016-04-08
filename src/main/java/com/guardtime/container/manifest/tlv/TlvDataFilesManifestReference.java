package com.guardtime.container.manifest.tlv;

import com.guardtime.container.manifest.DataFilesManifest;
import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.tlv.TLVParserException;

import java.io.IOException;

class TlvDataFilesManifestReference extends TlvFileReference {

    public static final int DATA_FILES_MANIFEST_REFERENCE = 0xb01;
    private static final String DATA_MANIFEST = "ksie10/datamanifest";

    public TlvDataFilesManifestReference(TLVElement rootElement) throws TLVParserException {
        super(rootElement);
    }

    public TlvDataFilesManifestReference(DataFilesManifest dataManifest, String uri) throws TLVParserException, IOException {
        super(uri, dataManifest.getDataHash(DEFAULT_HASH_ALGORITHM), DATA_MANIFEST);
    }

    @Override
    public int getElementType() {
        return DATA_FILES_MANIFEST_REFERENCE;
    }

}
