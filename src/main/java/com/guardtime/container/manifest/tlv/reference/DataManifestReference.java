package com.guardtime.container.manifest.tlv.reference;

import com.guardtime.container.manifest.ContainerManifestMimeType;
import com.guardtime.container.manifest.DataFilesManifest;
import com.guardtime.container.util.Util;
import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.tlv.TLVParserException;

import java.io.IOException;

public class DataManifestReference extends FileReference {
    public static final int DATA_FILES_MANIFEST_REFERENCE = 0xb01;

    public DataManifestReference(TLVElement rootElement) throws TLVParserException {
        super(rootElement);
    }

    public DataManifestReference(DataFilesManifest dataManifest) throws TLVParserException, IOException {
        this(
                new TlvReferenceBuilder().
                        withType(DATA_FILES_MANIFEST_REFERENCE).
                        withUriElement(dataManifest.getUri()).
                        withHashElement(Util.hash(dataManifest.getInputStream(), DEFAULT_HASH_ALGORITHM)).
                        withMimeTypeElement(ContainerManifestMimeType.DATA_MANIFEST.getType()).
                        build()
        );
    }

    @Override
    public int getElementType() {
        return DATA_FILES_MANIFEST_REFERENCE;
    }
}
