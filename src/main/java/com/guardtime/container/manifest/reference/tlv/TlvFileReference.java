package com.guardtime.container.manifest.reference.tlv;

import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;
import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.tlv.TLVParserException;
import com.guardtime.ksi.tlv.TLVStructure;

public abstract class TlvFileReference extends TLVStructure {
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
