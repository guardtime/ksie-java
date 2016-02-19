package com.guardtime.container.manifest.reference.tlv;

import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.manifest.reference.AnnotationReference;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.tlv.TLVParserException;
import com.guardtime.ksi.tlv.TLVStructure;

import java.io.IOException;

public class TlvAnnotationReference extends TLVStructure implements AnnotationReference {
    public static final int ANNOTATION_REFERENCE = 0xb05;

    private String uri;
    private DataHash hash;
    private String domain;

    public TlvAnnotationReference(TLVElement rootElement) throws TLVParserException {
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

    public TlvAnnotationReference(ContainerAnnotation annotation) throws TLVParserException, IOException {
        this(
                new TlvReferenceBuilder().
                        withType(ANNOTATION_REFERENCE).
                        withUriElement(annotation.getUri()).
                        withHashElement(annotation.getDataHash(TlvFileReference.DEFAULT_HASH_ALGORITHM)).
                        withDomainElement(annotation.getDomain()).
                        build()
        );
    }

    @Override
    public int getElementType() {
        return ANNOTATION_REFERENCE;
    }

    public String getUri() {
        return uri;
    }

    public String getDomain() {
        return domain;
    }

    @Override
    public DataHash getHash() {
        return hash;
    }
}
