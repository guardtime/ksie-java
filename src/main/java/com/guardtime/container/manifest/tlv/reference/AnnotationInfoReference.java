package com.guardtime.container.manifest.tlv.reference;

import com.guardtime.container.annotation.ContainerAnnotationType;
import com.guardtime.container.manifest.AnnotationInfoManifest;
import com.guardtime.container.util.Util;
import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.tlv.TLVParserException;

import java.io.IOException;

public class AnnotationInfoReference extends FileReference {
    public static final int ANNOTATION_INFO_REFERENCE = 0xb04;

    public AnnotationInfoReference(TLVElement rootElement) throws TLVParserException {
        super(rootElement);
    }

    public AnnotationInfoReference(AnnotationInfoManifest manifest, ContainerAnnotationType annotationType) throws TLVParserException, IOException {
        this(
                new TlvReferenceBuilder().
                        withType(ANNOTATION_INFO_REFERENCE).
                        withUriElement(manifest.getUri()).
                        withHashElement(Util.hash(manifest.getInputStream(), DEFAULT_HASH_ALGORITHM)).
                        withMimeTypeElement(annotationType.getContent()).
                        build()
        );
    }

    @Override
    protected Object[] getMandatoryElements() {
        return new Object[]{uri, hash, mimeType};
    }

    @Override
    public int getElementType() {
        return ANNOTATION_INFO_REFERENCE;
    }
}
