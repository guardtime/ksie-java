package com.guardtime.container.manifest.tlv;

import com.guardtime.container.BlockChainContainerException;
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

    public TlvAnnotationsManifest(InputStream stream, String uri) throws BlockChainContainerException {
        super(uri);
        try {
            fillReferencesListFromTLVElements(
                    parseElementsFromStream(stream)
            );
        } catch (TLVParserException e) {
            throw new BlockChainContainerException(e);
        }
    }

    public TlvAnnotationsManifest(Map<ContainerAnnotation, TlvAnnotationInfoManifest> annotationManifests, String uri) throws BlockChainContainerException {
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

    private void fillReferencesListFromMap(Map<ContainerAnnotation, TlvAnnotationInfoManifest> annotationManifests) throws BlockChainContainerException {
        try {
            for (ContainerAnnotation annotation : annotationManifests.keySet()) {
                TlvAnnotationInfoManifest manifest = annotationManifests.get(annotation);
                this.annotationReferences.add(new AnnotationInfoReference(manifest, annotation.getAnnotationType()));
            }
        } catch (TLVParserException | IOException e) {
            throw new BlockChainContainerException(e);
        }
    }

    protected void fillReferencesListFromTLVElements(List<TLVElement> tlvElements) throws TLVParserException {
        for (TLVElement element : tlvElements) {
            this.annotationReferences.add(new AnnotationInfoReference(element));
        }
    }
}
