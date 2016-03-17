package com.guardtime.container.manifest.tlv;

import com.guardtime.container.datafile.ContainerDocument;
import com.guardtime.container.manifest.DataFilesManifest;
import com.guardtime.container.manifest.InvalidManifestException;
import com.guardtime.container.util.DataHashException;
import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.tlv.TLVInputStream;
import com.guardtime.ksi.tlv.TLVParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

class TlvDataFilesManifest extends AbstractTlvManifestStructure implements DataFilesManifest {

    private static final byte[] MAGIC = "KSIEDAMF".getBytes();

    private List<TlvDataFileReference> documents = new LinkedList<>();

    public TlvDataFilesManifest(List<ContainerDocument> documents) throws InvalidManifestException {
        super(MAGIC);
        try {
            for (ContainerDocument doc : documents) {
                this.documents.add(new TlvDataFileReference(doc));
            }
        } catch (DataHashException | TLVParserException | IOException e) {
            throw new InvalidManifestException("Failed to generate TlvDataFilesManifest", e);
        }
    }

    public TlvDataFilesManifest(InputStream stream) throws InvalidManifestException {
        super(MAGIC, stream);
        try {
            read(stream);
        } catch (TLVParserException e) {
            throw new InvalidManifestException("Failed to parse TlvDataFilesManifest from InputStream", e);
        } catch (IOException e) {
            throw new InvalidManifestException("Failed to read InputStream", e);
        }
    }

    @Override
    public List<TlvDataFileReference> getDataFileReferences() {
        return documents;
    }

    @Override
    protected List<TlvDataFileReference> getElements() {
        return documents;
    }

    private void read(InputStream stream) throws TLVParserException, IOException {
        TLVInputStream input = toTlvInputStream(stream);
        TLVElement element;
        while (input.hasNextElement()) {
            element = input.readElement();
            //TODO unknown elements must be handled correctly
            documents.add(new TlvDataFileReference(element));
        }
    }

}
