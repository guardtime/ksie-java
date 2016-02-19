package com.guardtime.container.manifest.reference.tlv;

import com.guardtime.container.datafile.ContainerDocument;
import com.guardtime.container.manifest.reference.DataFileReference;
import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.tlv.TLVParserException;

import java.io.IOException;

public class TlvDataFileReference extends TlvFileReference implements DataFileReference {
    public static final int DATA_FILE_REFERENCE = 0xb03;

    public TlvDataFileReference(TLVElement root) throws TLVParserException {
        super(root);
    }

    public TlvDataFileReference(ContainerDocument document) throws TLVParserException, IOException {
        this(
                new TlvReferenceBuilder().
                        withType(DATA_FILE_REFERENCE).
                        withUriElement(document.getFileName()).
                        withHashElement(document.getDataHash(DEFAULT_HASH_ALGORITHM)).
                        withMimeTypeElement(document.getMimeType()).
                        build()
        );
    }

    @Override
    public int getElementType() {
        return DATA_FILE_REFERENCE;
    }
}
