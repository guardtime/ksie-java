package com.guardtime.container.manifest.tlv;

import com.guardtime.container.BlockChainContainerException;
import com.guardtime.container.datafile.ContainerDocument;
import com.guardtime.container.manifest.DataFilesManifest;
import com.guardtime.ksi.tlv.TLVParserException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class TlvDataFilesManifest implements DataFilesManifest {
    private static final byte[] MAGIC = "KSIEDAMF".getBytes(); // TODO: Replace with bytes according to spec
    private List<ContainerDocument> documents;

    public TlvDataFilesManifest(List<ContainerDocument> documents) {
        this.documents = documents;
    }

    @Override
    public String getUri() {
        return null;
    }

    @Override
    public InputStream getInputStream() {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bos.write(MAGIC);
            for (ContainerDocument document : documents) {
                TlvReferenceElementFactory.createDocumentReferenceTlvElement(document).writeTo(bos);
            }
            return new ByteArrayInputStream(bos.toByteArray());
        } catch (IOException | TLVParserException e) {
            throw new BlockChainContainerException(e);
        }
    }
}
