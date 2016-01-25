package com.guardtime.container.manifest.tlv;

import com.guardtime.container.BlockChainContainerException;
import com.guardtime.container.datafile.ContainerDocument;
import com.guardtime.container.manifest.DataFilesManifest;
import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.tlv.TLVParserException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

public class TlvDataFilesManifest implements DataFilesManifest {
    private static final byte[] MAGIC = "KSIEDAMF".getBytes(); // TODO: Replace with bytes according to spec
    private List<ContainerDocument> documents;
    private String manifestUri;

    public TlvDataFilesManifest(List<ContainerDocument> documents, String manifestUri) {
        this.documents = documents;
        this.manifestUri = manifestUri;
    }

    @Override
    public String getUri() {
        return manifestUri;
    }

    @Override
    public InputStream getInputStream() throws BlockChainContainerException {
        LinkedList<TLVElement> elements = new LinkedList<>(); // TODO: Possibly a better solution,  revisit
        for (ContainerDocument document : documents) {
            elements.add(TlvReferenceElementFactory.createDocumentReferenceTlvElement(document));
        }
        return TlvUtil.generateInputStream(MAGIC, elements.toArray(new TLVElement[elements.size()]));
    }
}
