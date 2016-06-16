package com.guardtime.container.manifest.tlv;

import com.guardtime.container.document.ContainerDocument;
import com.guardtime.container.manifest.DocumentsManifest;
import com.guardtime.container.manifest.InvalidManifestException;
import com.guardtime.container.util.DataHashException;
import com.guardtime.container.util.Util;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;
import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.tlv.TLVInputStream;
import com.guardtime.ksi.tlv.TLVParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

class TlvDocumentsManifest extends AbstractTlvManifestStructure implements DocumentsManifest {

    private static final byte[] MAGIC = "KSIEDAMF".getBytes();

    private List<TlvDocumentReference> documents = new LinkedList<>();

    public TlvDocumentsManifest(List<ContainerDocument> documents, HashAlgorithm algorithm) throws InvalidManifestException {
        super(MAGIC);
        try {
            for (ContainerDocument doc : documents) {
                this.documents.add(new TlvDocumentReference(doc, algorithm));
            }
        } catch (DataHashException | TLVParserException | IOException e) {
            throw new InvalidManifestException("Failed to generate TlvDocumentsManifest", e);
        }
    }

    public TlvDocumentsManifest(InputStream stream) throws InvalidManifestException {
        super(MAGIC, stream);
        try {
            read(stream);
        } catch (TLVParserException e) {
            throw new InvalidManifestException("Failed to parse TlvDocumentsManifest from InputStream", e);
        } catch (IOException e) {
            throw new InvalidManifestException("Failed to read InputStream", e);
        }
    }

    @Override
    public List<TlvDocumentReference> getDocumentReferences() {
        return documents;
    }

    @Override
    protected List<TlvDocumentReference> getElements() {
        return documents;
    }

    private void read(InputStream stream) throws TLVParserException, IOException {
        TLVInputStream input = toTlvInputStream(stream);
        TLVElement element;
        while (input.hasNextElement()) {
            element = input.readElement();
            //TODO unknown elements must be handled correctly
            documents.add(new TlvDocumentReference(element));
        }
    }

    @Override
    public DataHash getDataHash(HashAlgorithm algorithm) throws IOException {
        return Util.hash(getInputStream(), algorithm);
    }

}
