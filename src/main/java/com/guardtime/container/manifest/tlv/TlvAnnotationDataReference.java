package com.guardtime.container.manifest.tlv;

import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.hash.HashAlgorithmProvider;
import com.guardtime.container.manifest.AnnotationDataReference;
import com.guardtime.container.util.DataHashException;
import com.guardtime.container.util.Pair;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.tlv.TLVParserException;
import com.guardtime.ksi.tlv.TLVStructure;

class TlvAnnotationDataReference extends TLVStructure implements AnnotationDataReference {

    public static final int ANNOTATION_REFERENCE = 0xb05;

    private String uri;
    private DataHash hash;
    private String domain;

    public TlvAnnotationDataReference(TLVElement rootElement) throws TLVParserException {
        super(rootElement);
        for (TLVElement element : rootElement.getChildElements()) {
            switch (element.getType()) {
                case TlvReferenceBuilder.URI_TYPE:
                    this.uri = readOnce(element).getDecodedString();
                    break;
                case TlvReferenceBuilder.HASH_TYPE:
                    this.hash = readOnce(element).getDecodedDataHash();
                    break;
                case TlvReferenceBuilder.DOMAIN_TYPE:
                    this.domain = readOnce(element).getDecodedString();
                    break;
                default:
                    verifyCriticalFlag(element);
            }
        }
    }

    public TlvAnnotationDataReference(Pair<String, ContainerAnnotation> annotationPair, HashAlgorithmProvider algorithmProvider) throws TLVParserException, DataHashException {
        ContainerAnnotation annotation = annotationPair.getRight();
        this.uri = annotationPair.getLeft();
        this.hash = annotation.getDataHash(algorithmProvider.getAnnotationDataReferenceHashAlgorithm());
        this.domain = annotation.getDomain();
        this.rootElement = new TlvReferenceBuilder().
                withType(ANNOTATION_REFERENCE).
                withUriElement(uri).
                withHashElement(hash).
                withDomainElement(domain).
                build();
    }

    @Override
    public int getElementType() {
        return ANNOTATION_REFERENCE;
    }

    @Override
    public String getUri() {
        return uri;
    }

    @Override
    public String getDomain() {
        return domain;
    }

    @Override
    public DataHash getHash() {
        return hash;
    }
}
