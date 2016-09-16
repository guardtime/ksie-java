package com.guardtime.container.manifest.tlv;

import com.guardtime.container.document.ContainerDocument;
import com.guardtime.container.hash.HashAlgorithmProvider;
import com.guardtime.container.util.DataHashException;
import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.tlv.TLVParserException;

import java.io.IOException;

class TlvDocumentReference extends TlvFileReference {

    public static final int DATA_FILE_REFERENCE = 0xb03;

    public TlvDocumentReference(TLVElement root) throws TLVParserException {
        super(root);
    }

    public TlvDocumentReference(ContainerDocument document, HashAlgorithmProvider algorithmProvider) throws TLVParserException, IOException, DataHashException {
        super(document.getFileName(), document.getDataHashList(algorithmProvider), document.getMimeType());
    }

    @Override
    public int getElementType() {
        return DATA_FILE_REFERENCE;
    }

}
