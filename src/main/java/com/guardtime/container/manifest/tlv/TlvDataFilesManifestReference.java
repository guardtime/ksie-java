package com.guardtime.container.manifest.tlv;

import com.guardtime.container.manifest.DataFilesManifest;
import com.guardtime.container.manifest.tlv.TlvFileReference;
import com.guardtime.container.util.Util;
import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.tlv.TLVParserException;

import java.io.IOException;

import static com.guardtime.container.manifest.ContainerManifestMimeType.DATA_MANIFEST;

class TlvDataFilesManifestReference extends TlvFileReference {

    public static final int DATA_FILES_MANIFEST_REFERENCE = 0xb01;

    public TlvDataFilesManifestReference(TLVElement rootElement) throws TLVParserException {
        super(rootElement);
    }

    public TlvDataFilesManifestReference(DataFilesManifest dataManifest, String uri) throws TLVParserException, IOException {
        super(uri, Util.hash(dataManifest.getInputStream(), DEFAULT_HASH_ALGORITHM), DATA_MANIFEST.getType());
    }

    @Override
    public int getElementType() {
        return DATA_FILES_MANIFEST_REFERENCE;
    }

}
