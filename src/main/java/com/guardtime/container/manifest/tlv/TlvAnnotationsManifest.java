package com.guardtime.container.manifest.tlv;

import com.guardtime.container.BlockChainContainerException;
import com.guardtime.container.manifest.AnnotationsManifest;
import com.guardtime.ksi.tlv.TLVParserException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class TlvAnnotationsManifest implements AnnotationsManifest {
    private static final byte[] MAGIC = "KSIEANMF".getBytes(); // TODO: Replace with bytes according to spec
    private List<TlvAnnotationInfoManifest> annotationReferences;

    public TlvAnnotationsManifest(List<TlvAnnotationInfoManifest> annotationReferences) {
        this.annotationReferences = annotationReferences;
    }

    @Override
    public InputStream getInputStream() {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bos.write(MAGIC);
            for (TlvAnnotationInfoManifest manifest : annotationReferences) {
                TlvReferenceElementFactory.createAnnotationInfoReferenceTlvElement(manifest).writeTo(bos);
            }
            return new ByteArrayInputStream(bos.toByteArray());
        } catch (IOException | TLVParserException e) {
            throw new BlockChainContainerException(e);
        }
    }

    @Override
    public String getUri() {
        // TODO: Should be pretty much the same as the annotation has only with different ending.
        return null;
    }
}
