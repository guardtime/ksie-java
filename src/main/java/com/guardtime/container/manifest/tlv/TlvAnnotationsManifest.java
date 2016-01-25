package com.guardtime.container.manifest.tlv;

import com.guardtime.container.BlockChainContainerException;
import com.guardtime.container.manifest.AnnotationsManifest;
import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.tlv.TLVParserException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

public class TlvAnnotationsManifest implements AnnotationsManifest {
    private static final byte[] MAGIC = "KSIEANMF".getBytes(); // TODO: Replace with bytes according to spec
    private List<TlvAnnotationInfoManifest> annotationReferences;
    private String manifestUri;

    public TlvAnnotationsManifest(List<TlvAnnotationInfoManifest> annotationReferences, String manifestUri) {
        this.annotationReferences = annotationReferences;
        this.manifestUri = manifestUri;
    }

    @Override
    public InputStream getInputStream() throws BlockChainContainerException {
        LinkedList<TLVElement> elements = new LinkedList<>(); // TODO: Possibly a better solution,  revisit
        for (TlvAnnotationInfoManifest manifest : annotationReferences) {
            elements.add(TlvReferenceElementFactory.createAnnotationInfoReferenceTlvElement(manifest));
        }
        return TlvUtil.generateInputStream(MAGIC, elements.toArray(new TLVElement[elements.size()]));
    }

    @Override
    public String getUri() {
        return manifestUri;
    }
}
