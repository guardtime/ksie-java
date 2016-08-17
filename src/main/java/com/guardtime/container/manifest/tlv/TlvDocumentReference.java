package com.guardtime.container.manifest.tlv;

import com.guardtime.container.document.ContainerDocument;
import com.guardtime.container.hash.HashAlgorithmProvider;
import com.guardtime.container.util.DataHashException;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;
import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.tlv.TLVParserException;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

class TlvDocumentReference extends TlvFileReference {

    public static final int DATA_FILE_REFERENCE = 0xb03;

    public TlvDocumentReference(TLVElement root) throws TLVParserException {
        super(root);
    }

    public TlvDocumentReference(ContainerDocument document, HashAlgorithmProvider algorithmProvider) throws TLVParserException, IOException, DataHashException {
        super(document.getFileName(), getDataHashList(document, algorithmProvider), document.getMimeType());
    }

    private static List<DataHash> getDataHashList(ContainerDocument document, HashAlgorithmProvider algorithmProvider) throws IOException, DataHashException {
        List<DataHash> dataHashList = new LinkedList<>();
        for(HashAlgorithm algorithm : algorithmProvider.getHashAlgorithms()) {
            dataHashList.add(document.getDataHash(algorithm));
        }
        return dataHashList;
    }

    @Override
    public int getElementType() {
        return DATA_FILE_REFERENCE;
    }

}
