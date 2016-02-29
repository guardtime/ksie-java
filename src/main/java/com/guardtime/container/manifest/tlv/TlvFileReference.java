package com.guardtime.container.manifest.tlv;

import com.guardtime.container.manifest.FileReference;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;
import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.tlv.TLVParserException;
import com.guardtime.ksi.tlv.TLVStructure;

abstract class TlvFileReference extends TLVStructure implements FileReference {

    protected static final HashAlgorithm DEFAULT_HASH_ALGORITHM = HashAlgorithm.SHA2_256;

    private String uri;
    private DataHash hash;
    private String mimeType;

    public TlvFileReference(TLVElement rootElement) throws TLVParserException {
        super(rootElement);
        for (TLVElement element : rootElement.getChildElements()) {
            switch (element.getType()) {
                case TlvReferenceBuilder.URI_TYPE:
                    this.uri = readOnce(element).getDecodedString();
                    break;
                case TlvReferenceBuilder.HASH_TYPE:
                    this.hash = readOnce(element).getDecodedDataHash();
                    break;
                case TlvReferenceBuilder.MIME_TYPE:
                    this.mimeType = readOnce(element).getDecodedString();
                    break;
                default:
                    verifyCriticalFlag(element);
            }
        }
    }

    public TlvFileReference(String uri, DataHash dataHash, String mimeType) throws TLVParserException {
        this.uri = uri;
        this.hash = dataHash;
        this.mimeType = mimeType;
        this.rootElement = new TlvReferenceBuilder()
                .withType(getElementType())
                .withUriElement(uri)
                .withHashElement(dataHash)
                .withMimeTypeElement(mimeType)
                .build();
    }

    public String getUri() {
        return uri;
    }

    public String getMimeType() {
        return mimeType;
    }

    public DataHash getHash() {
        return hash;
    }
}
