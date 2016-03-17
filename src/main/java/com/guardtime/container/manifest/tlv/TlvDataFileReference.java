package com.guardtime.container.manifest.tlv;

import com.guardtime.container.datafile.ContainerDocument;
import com.guardtime.container.manifest.tlv.TlvFileReference;
import com.guardtime.container.util.DataHashException;
import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.tlv.TLVParserException;

import java.io.IOException;

class TlvDataFileReference extends TlvFileReference {

    private static final int DATA_FILE_REFERENCE = 0xb03;

    public TlvDataFileReference(TLVElement root) throws TLVParserException {
        super(root);
    }

    public TlvDataFileReference(ContainerDocument document) throws TLVParserException, IOException, DataHashException {
        super(document.getFileName(), document.getDataHash(DEFAULT_HASH_ALGORITHM), document.getMimeType());
    }

    @Override
    public int getElementType() {
        return DATA_FILE_REFERENCE;
    }

}
