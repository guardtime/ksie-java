package com.guardtime.container.manifest.tlv;

import com.guardtime.container.BlockChainContainerException;
import com.guardtime.container.manifest.SignatureManifest;
import com.guardtime.container.util.Util;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;
import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.tlv.TLVParserException;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

public class TlvSignatureManifest extends TlvManifestStructure implements SignatureManifest {
    private static final byte[] MAGIC = "KSIEMFST".getBytes();  // TODO: Verify from spec
    private String uri;
    private TLVElement dataFilesManifestReference;
    private TLVElement signatureReference;
    private TLVElement annotationsManifestReference;

    public TlvSignatureManifest(List<TLVElement> elements, String uri) throws TLVParserException {
        super(MAGIC, elements);
        this.uri = uri;
    }

    public TlvSignatureManifest(InputStream stream, String uri) throws TLVParserException {
        super(stream);
        this.uri = uri;
    }

    @Override
    public DataHash getDataHash(HashAlgorithm algorithm) throws BlockChainContainerException {
        return Util.hash(getInputStream(), algorithm);
    }

    @Override
    protected byte[] getMagic() {
        return MAGIC;
    }

    @Override
    protected List<TLVElement> getElements() {
        List<TLVElement> elements = new LinkedList<>();
        elements.add(dataFilesManifestReference);
        elements.add(signatureReference);
        elements.add(annotationsManifestReference);
        return elements;
    }

    @Override
    protected void setElements(List<TLVElement> tlvElements) throws TLVParserException {
        if (tlvElements == null || tlvElements.isEmpty()) {
            throw new TLVParserException("No elements in manifest");
        }

        for(TLVElement element : tlvElements){
            switch (TlvTypes.fromValue(element.getType())){
                case DATA_FILES_MANIFEST_REFERENCE:
                    dataFilesManifestReference = readOnce(element);
                    break;
                case SIGNATURE_REFERENCE:
                    signatureReference = readOnce(element);
                    break;
                case ANNOTATIONS_MANIFEST_REFERENCE:
                    annotationsManifestReference = readOnce(element);
                    break;
                default:
                    throw new TLVParserException("Unexpected element in manifest!");
            }
        }
        // Check that all mandatory elements present
        if (dataFilesManifestReference == null || signatureReference == null || annotationsManifestReference == null) {
            throw new TLVParserException("Missing mandatory elements!");
        }
    }

    @Override
    public String getUri() {
        return uri;
    }
}
