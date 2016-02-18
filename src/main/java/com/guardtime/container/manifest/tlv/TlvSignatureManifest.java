package com.guardtime.container.manifest.tlv;

import com.guardtime.container.BlockChainContainerException;
import com.guardtime.container.manifest.AnnotationsManifest;
import com.guardtime.container.manifest.DataFilesManifest;
import com.guardtime.container.manifest.SignatureManifest;
import com.guardtime.container.manifest.reference.AnnotationsManifestReference;
import com.guardtime.container.manifest.reference.DataFilesManifestReference;
import com.guardtime.container.manifest.reference.tlv.TlvAnnotationsManifestReference;
import com.guardtime.container.manifest.reference.tlv.TlvDataFilesManifestReference;
import com.guardtime.container.manifest.reference.tlv.TlvSignatureReference;
import com.guardtime.container.util.Util;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;
import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.tlv.TLVParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

public class TlvSignatureManifest extends TlvManifestStructure implements SignatureManifest {
    private static final byte[] MAGIC = "KSIEMFST".getBytes();  // TODO: Verify from spec
    private TlvDataFilesManifestReference dataFilesManifestReference;
    private TlvSignatureReference signatureReference;
    private TlvAnnotationsManifestReference annotationsManifestReference;

    public TlvSignatureManifest(DataFilesManifest dataFilesManifest, AnnotationsManifest annotationsManifest, String signaturePath, String uri) throws TLVParserException {
        super(uri);
        try {
            this.dataFilesManifestReference = new TlvDataFilesManifestReference(dataFilesManifest);
            this.signatureReference = new TlvSignatureReference(signaturePath);
            this.annotationsManifestReference = new TlvAnnotationsManifestReference(annotationsManifest);
        } catch (IOException e) {
            throw new TLVParserException("Failed to generate file reference TLVElement", e);
        }
    }

    public TlvSignatureManifest(InputStream stream, String uri) throws TLVParserException {
        super(uri);
        setReferencesFromTLVElements(
                parseElementsFromStream(stream)
        );
    }

    @Override
    public DataHash getDataHash(HashAlgorithm algorithm) throws BlockChainContainerException {
        try {
            return Util.hash(getInputStream(), algorithm);
        } catch (IOException e) {
            throw new BlockChainContainerException(e);
        }
    }

    @Override
    public DataFilesManifestReference getDataFilesManifestReference() {
        return dataFilesManifestReference;
    }

    @Override
    public AnnotationsManifestReference getAnnotationsManifestReference() {
        return annotationsManifestReference;
    }

    @Override
    public String getSignatureUri() {
        return signatureReference.getUri();
    }

    @Override
    protected byte[] getMagic() {
        return MAGIC;
    }

    @Override
    protected List<TLVElement> getElements() {
        List<TLVElement> elements = new LinkedList<>();
        elements.add(dataFilesManifestReference.getRootElement());
        elements.add(signatureReference.getRootElement());
        elements.add(annotationsManifestReference.getRootElement());
        return elements;
    }

    protected void setReferencesFromTLVElements(List<TLVElement> tlvElements) throws TLVParserException {
        for (TLVElement element : tlvElements) {
            switch (element.getType()) {
                case TlvDataFilesManifestReference.DATA_FILES_MANIFEST_REFERENCE:
                    dataFilesManifestReference = new TlvDataFilesManifestReference(readOnce(element));
                    break;
                case TlvSignatureReference.SIGNATURE_REFERENCE:
                    signatureReference = new TlvSignatureReference(readOnce(element));
                    break;
                case TlvAnnotationsManifestReference.ANNOTATIONS_MANIFEST_REFERENCE:
                    annotationsManifestReference = new TlvAnnotationsManifestReference(readOnce(element));
                    break;
                default:
                    verifyCriticalFlag(element);
            }
        }

        // Check that all mandatory elements present
        if (dataFilesManifestReference == null || signatureReference == null || annotationsManifestReference == null) {
            throw new TLVParserException("Missing mandatory elements!");
        }
    }
}
