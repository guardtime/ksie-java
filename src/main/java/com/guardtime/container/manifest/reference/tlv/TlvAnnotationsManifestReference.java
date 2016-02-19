package com.guardtime.container.manifest.reference.tlv;

import com.guardtime.container.manifest.AnnotationsManifest;
import com.guardtime.container.manifest.ContainerManifestMimeType;
import com.guardtime.container.manifest.reference.AnnotationsManifestReference;
import com.guardtime.container.util.Util;
import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.tlv.TLVParserException;

import java.io.IOException;

public class TlvAnnotationsManifestReference extends TlvFileReference implements AnnotationsManifestReference {
    public static final int ANNOTATIONS_MANIFEST_REFERENCE = 0xb02;

    public TlvAnnotationsManifestReference(TLVElement rootElement) throws TLVParserException {
        super(rootElement);
    }

    public TlvAnnotationsManifestReference(AnnotationsManifest manifest) throws TLVParserException, IOException {
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
    public int getElementType() {
        return ANNOTATIONS_MANIFEST_REFERENCE;
    }
}
