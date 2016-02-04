package com.guardtime.container.manifest.tlv.reference;

import com.guardtime.container.manifest.AnnotationsManifest;
import com.guardtime.container.manifest.ContainerManifestMimeType;
import com.guardtime.container.manifest.tlv.TlvReferenceBuilder;
import com.guardtime.container.util.Util;
import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.tlv.TLVParserException;

import java.io.IOException;

public class AnnotationsManifestReference extends FileReference {
    public static final int ANNOTATIONS_MANIFEST_REFERENCE = 0xb02;

    public AnnotationsManifestReference(TLVElement rootElement) throws TLVParserException {
        super(rootElement);
    }

    public AnnotationsManifestReference(AnnotationsManifest manifest) throws TLVParserException, IOException {
        this(
                new TlvReferenceBuilder().
                        withType(ANNOTATIONS_MANIFEST_REFERENCE).
                        withUriElement(manifest.getUri()).
                        withHashElement(Util.hash(manifest.getInputStream(), DEFAULT_HASH_ALGORITHM)).
                        withMimeTypeElement(ContainerManifestMimeType.ANNOTATIONS_MANIFEST.getType()).
                        build()
        );
    }

    @Override
    protected Object[] getMandatoryElements() {
        return new Object[]{uri, hash, mimeType};
    }

    @Override
    public int getElementType() {
        return ANNOTATIONS_MANIFEST_REFERENCE;
    }
}
