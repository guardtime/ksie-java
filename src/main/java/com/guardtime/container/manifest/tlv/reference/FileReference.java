package com.guardtime.container.manifest.tlv.reference;

import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;
import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.tlv.TLVParserException;
import com.guardtime.ksi.tlv.TLVStructure;

public abstract class FileReference extends TLVStructure {
    private static final int URI_TYPE = 0x1;
    private static final int HASH_TYPE = 0x2;
    private static final int MIME_TYPE = 0x3;
    private static final int DOMAIN_TYPE = 0x4;
    protected static final HashAlgorithm DEFAULT_HASH_ALGORITHM = HashAlgorithm.SHA2_256;

    protected String uri;
    protected DataHash hash;
    protected String mimeType;
    protected String domain;

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
                case DOMAIN_TYPE:
                    this.domain = readOnce(element).getDecodedString();
                    break;
                default:
                    verifyCriticalFlag(element);
            }
        }
        verifyMandatoryElements(getMandatoryElements());
    }

    public String getUri() {
        return uri;
    }

    protected abstract Object[] getMandatoryElements();

    private void verifyMandatoryElements(Object... objects) throws TLVParserException {
        for (Object obj : objects) {
            if (obj == null) throw new TLVParserException("Missing mandatory element!");
        }
    }
}
