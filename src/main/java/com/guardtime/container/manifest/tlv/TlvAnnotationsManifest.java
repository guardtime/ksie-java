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
        super(uri, stream);
        fillReferencesListFromTLVElements(
                parseElementsFromStream(stream)
        );
    }

    public TlvAnnotationsManifest(Map<ContainerAnnotation, TlvAnnotationInfoManifest> annotationManifests, String uri) throws TLVParserException {
        super(uri);
        fillReferencesListFromMap(annotationManifests);
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

    private void fillReferencesListFromMap(Map<ContainerAnnotation, TlvAnnotationInfoManifest> annotationManifests) throws TLVParserException {
        try {
            for (ContainerAnnotation annotation : annotationManifests.keySet()) {
                TlvAnnotationInfoManifest manifest = annotationManifests.get(annotation);
                this.annotationReferences.add(new AnnotationInfoReference(manifest, annotation.getAnnotationType()));
            }
        } catch (IOException e) {
            throw new TLVParserException("Failed to generate file reference TLVElement", e);
        }
    }

    protected void fillReferencesListFromTLVElements(List<TLVElement> tlvElements) throws TLVParserException {
        for (TLVElement element : tlvElements) {
            this.annotationReferences.add(new AnnotationInfoReference(element));
        }
    }
}
