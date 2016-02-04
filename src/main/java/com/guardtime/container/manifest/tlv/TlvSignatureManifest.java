package com.guardtime.container.manifest.tlv;

import com.guardtime.container.BlockChainContainerException;
import com.guardtime.container.manifest.AnnotationsManifest;
import com.guardtime.container.manifest.DataFilesManifest;
import com.guardtime.container.manifest.SignatureManifest;
import com.guardtime.container.manifest.tlv.reference.AnnotationsManifestReference;
import com.guardtime.container.manifest.tlv.reference.DataManifestReference;
import com.guardtime.container.manifest.tlv.reference.SignatureReference;
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
    private DataManifestReference dataFilesManifestReference;
    private SignatureReference signatureReference;
    private AnnotationsManifestReference annotationsManifestReference;

    public TlvSignatureManifest(DataFilesManifest dataFilesManifest, AnnotationsManifest annotationsManifest, String signaturePath, String uri) throws TLVParserException {
        super(uri);
        try {
            this.dataFilesManifestReference = new DataManifestReference(dataFilesManifest);
            this.signatureReference = new SignatureReference(signaturePath);
            this.annotationsManifestReference = new AnnotationsManifestReference(annotationsManifest);
        } catch (IOException e) {
            throw new TLVParserException("Failed to generate TLVElement", e);
        }
    }

    public TlvSignatureManifest(InputStream stream, String uri) throws TLVParserException {
        super(uri);
        setElements(parseElementsFromStream(stream));
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

    protected void setElements(List<TLVElement> tlvElements) throws TLVParserException {
        for (TLVElement element : tlvElements) {
            switch (element.getType()) {
                case DataManifestReference.DATA_FILES_MANIFEST_REFERENCE:
                    dataFilesManifestReference = new DataManifestReference(readOnce(element));
                    break;
                case SignatureReference.SIGNATURE_REFERENCE:
                    signatureReference = new SignatureReference(readOnce(element));
                    break;
                case AnnotationsManifestReference.ANNOTATIONS_MANIFEST_REFERENCE:
                    annotationsManifestReference = new AnnotationsManifestReference(readOnce(element));
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
