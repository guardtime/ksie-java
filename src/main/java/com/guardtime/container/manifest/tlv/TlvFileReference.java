package com.guardtime.container.manifest.tlv;

import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.manifest.MultiHashElement;
import com.guardtime.container.util.DataHashException;
import com.guardtime.container.util.Util;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;
import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.tlv.TLVParserException;
import com.guardtime.ksi.tlv.TLVStructure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

abstract class TlvFileReference extends TLVStructure implements FileReference {

    private String uri;
    private List<DataHash> hashList = new LinkedList<>();
    private String mimeType;

    public TlvFileReference(TLVElement rootElement) throws TLVParserException {
        super(rootElement);
        for (TLVElement element : rootElement.getChildElements()) {
            switch (element.getType()) {
                case TlvReferenceBuilder.URI_TYPE:
                    this.uri = readOnce(element).getDecodedString();
                    break;
                case TlvReferenceBuilder.HASH_TYPE:
                    this.hashList.add(element.getDecodedDataHash());
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
        this(uri, Collections.singletonList(dataHash), mimeType);
    }

    public TlvFileReference(String uri, List<DataHash> dataHashList, String mimeType) throws TLVParserException {
        Util.notEmpty(dataHashList, "Data hashes");
        this.uri = uri;
        this.hashList.addAll(dataHashList);
        this.mimeType = mimeType;
        TlvReferenceBuilder tlvReferenceBuilder = new TlvReferenceBuilder()
                .withType(getElementType())
                .withUriElement(uri);
        for (DataHash dataHash : dataHashList) {
            tlvReferenceBuilder.withHashElement(dataHash);
        }
        this.rootElement = tlvReferenceBuilder
                .withMimeTypeElement(mimeType)
                .build();
    }

    protected static List<DataHash> generateHashes(MultiHashElement multiHashElement, List<HashAlgorithm> hashAlgorithms) throws DataHashException {
        Util.notNull(hashAlgorithms, "Hash algorithms");
        List<DataHash> hashList = new ArrayList<>();
        for (HashAlgorithm algorithm : hashAlgorithms) {
            hashList.add(multiHashElement.getDataHash(algorithm));
        }
        return hashList;
    }

    public String getUri() {
        return uri;
    }

    public String getMimeType() {
        return mimeType;
    }

    public List<DataHash> getHashList() {
        return hashList;
    }
}
