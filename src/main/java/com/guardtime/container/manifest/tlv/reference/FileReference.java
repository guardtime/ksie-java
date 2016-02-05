package com.guardtime.container.manifest.tlv.reference;

import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;
import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.tlv.TLVParserException;
import com.guardtime.ksi.tlv.TLVStructure;

import static com.guardtime.container.manifest.tlv.reference.TlvReferenceBuilder.*;

public abstract class FileReference extends TLVStructure {
    protected static final HashAlgorithm DEFAULT_HASH_ALGORITHM = HashAlgorithm.SHA2_256;

    private String uri;
    private DataHash hash;
    private String mimeType;

    public FileReference(TLVElement rootElement) throws TLVParserException {
        super(rootElement);
        for (TLVElement element : rootElement.getChildElements()) {
            switch (element.getType()) {
                case URI_TYPE:
                    this.uri = readOnce(element).getDecodedString();
                    break;
                case HASH_TYPE:
                    this.hash = readOnce(element).getDecodedDataHash();
                    break;
                case MIME_TYPE:
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
}
