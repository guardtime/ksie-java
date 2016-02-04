package com.guardtime.container.manifest.tlv;

import com.guardtime.container.BlockChainContainerException;
import com.guardtime.container.datafile.ContainerDocument;
import com.guardtime.container.manifest.DataFilesManifest;
import com.guardtime.container.manifest.tlv.reference.DocumentReference;
import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.tlv.TLVParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TlvDataFilesManifest extends TlvManifestStructure implements DataFilesManifest {
    private static final byte[] MAGIC = "KSIEDAMF".getBytes();  // TODO: Verify from spec
    private Map<String, DocumentReference> documents = new HashMap<>();

    public TlvDataFilesManifest(List<ContainerDocument> documents, String uri) throws BlockChainContainerException {
        super(uri);
        fillMapFromContainerDocuments(documents);
    }

    public TlvDataFilesManifest(InputStream stream, String uri) throws BlockChainContainerException {
        super(uri);
        try {
            fillMapFromTLVElements(parseElementsFromStream(stream));
        } catch (TLVParserException e) {
            throw new BlockChainContainerException(e);
        }
    }

    @Override
    protected byte[] getMagic() {
        return MAGIC;
    }

    @Override
    protected List<TLVElement> getElements() {
        List<TLVElement> returnable = new LinkedList<>();
        for (DocumentReference ref : documents.values()) {
            returnable.add(ref.getRootElement());
        }
        return returnable;
    }

    private void fillMapFromContainerDocuments(List<ContainerDocument> documents) throws BlockChainContainerException {
        try {
            for (ContainerDocument doc : documents) {
                DocumentReference ref = new DocumentReference(doc);
                this.documents.put(ref.getUri(), ref);
            }
        } catch (IOException | TLVParserException e) {
            throw new BlockChainContainerException("Failed to generate TLVElement", e);
        }
    }

    protected void fillMapFromTLVElements(List<TLVElement> tlvElements) throws TLVParserException {
        for (TLVElement element : tlvElements) {
            DocumentReference ref = new DocumentReference(element);
            documents.put(ref.getUri(), ref);
        }
    }
}
