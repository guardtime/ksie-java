package com.guardtime.container.manifest.tlv;

import com.guardtime.container.datafile.ContainerDocument;
import com.guardtime.container.manifest.DataFilesManifest;
import com.guardtime.container.manifest.reference.DataFileReference;
import com.guardtime.container.manifest.reference.tlv.TlvDataFileReference;
import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.tlv.TLVParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

public class TlvDataFilesManifest extends TlvManifestStructure implements DataFilesManifest {
    private static final byte[] MAGIC = "KSIEDAMF".getBytes();  // TODO: Verify from spec
    private List<TlvDataFileReference> documents = new LinkedList<>();

    public TlvDataFilesManifest(List<ContainerDocument> documents, String uri) throws TLVParserException {
        super(uri);
        fillMapFromContainerDocuments(documents);
    }

    public TlvDataFilesManifest(InputStream stream, String uri) throws TLVParserException {
        super(uri, stream);
        fillMapFromTLVElements(parseElementsFromStream(stream));
    }

    @Override
    protected byte[] getMagic() {
        return MAGIC;
    }

    @Override
    protected List<TLVElement> getElements() {
        List<TLVElement> returnable = new LinkedList<>();
        for (TlvDataFileReference ref : documents) {
            returnable.add(ref.getRootElement());
        }
        return returnable;
    }

    private void fillMapFromContainerDocuments(List<ContainerDocument> documents) throws TLVParserException {
        try {
            for (ContainerDocument doc : documents) {
                TlvDataFileReference ref = new TlvDataFileReference(doc);
                this.documents.add(ref);
            }
        } catch (IOException e) {
            throw new TLVParserException("Failed to generate file reference TLVElement", e);
        }
    }

    protected void fillMapFromTLVElements(List<TLVElement> tlvElements) throws TLVParserException {
        for (TLVElement element : tlvElements) {
            TlvDataFileReference ref = new TlvDataFileReference(element);
            documents.add(ref);
        }
    }

    @Override
    public List<? extends DataFileReference> getDataFileReferences() {
        return documents;
    }
}
