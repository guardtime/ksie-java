package com.guardtime.container.manifest.tlv.reference;

import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.tlv.TLVParserException;
import com.guardtime.ksi.tlv.TLVStructure;

import java.io.IOException;

import static com.guardtime.container.manifest.tlv.reference.TlvReferenceBuilder.*;

public class AnnotationReference extends TLVStructure {
    public static final int ANNOTATION_REFERENCE = 0xb05;

    private String uri;
    private DataHash hash;
    private String domain;

    public AnnotationReference(TLVElement rootElement) throws TLVParserException {
        super(rootElement);
        for (TLVElement element : rootElement.getChildElements()) {
            switch (element.getType()) {
                case URI_TYPE:
                    this.uri = readOnce(element).getDecodedString();
                    break;
                case HASH_TYPE:
                    this.hash = readOnce(element).getDecodedDataHash();
                    break;
                case DOMAIN_TYPE:
                    this.domain = readOnce(element).getDecodedString();
                    break;
                default:
                    verifyCriticalFlag(element);
            }
        }
    }

    public AnnotationReference(ContainerAnnotation annotation) throws TLVParserException, IOException {
        this(
                new TlvReferenceBuilder().
                        withType(ANNOTATION_REFERENCE).
                        withUriElement(annotation.getUri()).
                        withHashElement(annotation.getDataHash(FileReference.DEFAULT_HASH_ALGORITHM)).
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
}
