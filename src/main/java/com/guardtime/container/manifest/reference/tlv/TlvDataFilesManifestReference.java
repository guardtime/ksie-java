package com.guardtime.container.manifest.reference.tlv;

import com.guardtime.container.manifest.ContainerManifestMimeType;
import com.guardtime.container.manifest.DataFilesManifest;
import com.guardtime.container.manifest.reference.DataFilesManifestReference;
import com.guardtime.container.util.Util;
import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.tlv.TLVParserException;

import java.io.IOException;

public class TlvDataFilesManifestReference extends TlvFileReference implements DataFilesManifestReference {
    public static final int DATA_FILES_MANIFEST_REFERENCE = 0xb01;

    public TlvDataFilesManifestReference(TLVElement rootElement) throws TLVParserException {
        super(rootElement);
    }

    public TlvDataFilesManifestReference(DataFilesManifest dataManifest) throws TLVParserException, IOException {
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
