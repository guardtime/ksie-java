package com.guardtime.container.manifest.tlv;

import com.guardtime.container.manifest.AnnotationsManifest;
import com.guardtime.container.manifest.tlv.TlvFileReference;
import com.guardtime.container.util.Util;
import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.tlv.TLVParserException;

import java.io.IOException;

import static com.guardtime.container.manifest.ContainerManifestMimeType.ANNOTATIONS_MANIFEST;

class TlvAnnotationsManifestReference extends TlvFileReference {

    public static final int ANNOTATIONS_MANIFEST_REFERENCE = 0xb02;

    public TlvAnnotationsManifestReference(TLVElement rootElement) throws TLVParserException {
        super(rootElement);
    }

    public TlvAnnotationsManifestReference(String uri, AnnotationsManifest manifest) throws TLVParserException, IOException {
        super(uri, Util.hash(manifest.getInputStream(), DEFAULT_HASH_ALGORITHM), ANNOTATIONS_MANIFEST.getType());
    }

    @Override
    public int getElementType() {
        return ANNOTATIONS_MANIFEST_REFERENCE;
    }
}
