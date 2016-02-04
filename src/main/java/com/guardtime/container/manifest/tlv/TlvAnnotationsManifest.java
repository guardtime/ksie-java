package com.guardtime.container.manifest.tlv;

import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.manifest.AnnotationsManifest;
import com.guardtime.container.manifest.tlv.reference.AnnotationInfoReference;
import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.tlv.TLVParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TlvAnnotationsManifest extends TlvManifestStructure implements AnnotationsManifest {
    private static final byte[] MAGIC = "KSIEANMF".getBytes();  // TODO: Verify from spec
    private List<AnnotationInfoReference> annotationReferences = new LinkedList<>();

    public TlvAnnotationsManifest(InputStream stream, String uri) throws TLVParserException {
        super(uri);
        setElements(parseElementsFromStream(stream));
    }

    public TlvAnnotationsManifest(Map<ContainerAnnotation, TlvAnnotationInfoManifest> annotationManifests, String uri) throws IOException, TLVParserException {
        super(uri);
        for (ContainerAnnotation annotation : annotationManifests.keySet()) {
            TlvAnnotationInfoManifest manifest = annotationManifests.get(annotation);
            this.annotationReferences.add(new AnnotationInfoReference(manifest, annotation.getAnnotationType()));
        }
    }

    @Override
    protected byte[] getMagic() {
        return MAGIC;
    }

    @Override
    protected List<TLVElement> getElements() {
        List<TLVElement> returnable = new LinkedList<>();
        for (AnnotationInfoReference reference : annotationReferences) {
            returnable.add(reference.getRootElement());
        }
        return returnable;
    }

    protected void setElements(List<TLVElement> tlvElements) throws TLVParserException {
        for (TLVElement element : tlvElements) {
            this.annotationReferences.add(new AnnotationInfoReference(element));
        }
    }
}
