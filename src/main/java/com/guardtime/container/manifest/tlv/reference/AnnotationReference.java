package com.guardtime.container.manifest.tlv.reference;

import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.manifest.tlv.TlvReferenceBuilder;
import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.tlv.TLVParserException;

import java.io.IOException;

public class AnnotationReference extends FileReference {
    public static final int ANNOTATION_REFERENCE = 0xb05;

    public AnnotationReference(TLVElement rootElement) throws TLVParserException {
        super(rootElement);
    }

    public AnnotationReference(ContainerAnnotation annotation) throws TLVParserException, IOException {
        this(
                new TlvReferenceBuilder().
                        withType(ANNOTATION_REFERENCE).
                        withUriElement(annotation.getUri()).
                        withHashElement(annotation.getDataHash(DEFAULT_HASH_ALGORITHM)).
                        withDomainElement(annotation.getDomain()).
                        build()
        );
    }

    @Override
    protected Object[] getMandatoryElements() {
        return new Object[]{uri, hash, domain};
    }

    @Override
    public int getElementType() {
        return ANNOTATION_REFERENCE;
    }
}
