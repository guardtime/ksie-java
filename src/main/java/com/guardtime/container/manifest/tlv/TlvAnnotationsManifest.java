package com.guardtime.container.manifest.tlv;

import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.annotation.ContainerAnnotationType;
import com.guardtime.container.manifest.AnnotationsManifest;
import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.manifest.InvalidManifestException;
import com.guardtime.container.util.Pair;
import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.tlv.TLVInputStream;
import com.guardtime.ksi.tlv.TLVParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

class TlvAnnotationsManifest extends AbstractTlvManifestStructure implements AnnotationsManifest {

    private static final byte[] MAGIC = "KSIEANMF".getBytes();  // TODO: Verify from spec

    private List<TlvAnnotationInfoManifestReference> annotationReferences = new LinkedList<>();

    public TlvAnnotationsManifest(Map<ContainerAnnotation, Pair<String, TlvAnnotationInfoManifest>> annotationManifests) throws InvalidManifestException {
        super(MAGIC);
        try {
            for (Pair<String, TlvAnnotationInfoManifest> annotation : annotationManifests.values()) {
                //TODO annotation type
                this.annotationReferences.add(new TlvAnnotationInfoManifestReference(annotation.getLeft(), annotation.getRight(), ContainerAnnotationType.FULLY_REMOVABLE));
            }
        } catch (TLVParserException | IOException e) {
            throw new InvalidManifestException("Failed to generate file reference TLVElement", e);
        }
    }

    public TlvAnnotationsManifest(InputStream stream) throws InvalidManifestException {
        super(MAGIC, stream);
        try {
            read(stream);
        } catch (TLVParserException | IOException e) {
            throw new InvalidManifestException(e);
        }
    }

    private void read(InputStream stream) throws TLVParserException, IOException {
        TLVInputStream input = toTlvInputStream(stream);
        TLVElement element;
        while (input.hasNextElement()) {
            element = input.readElement();
            //TODO unknown elements
            this.annotationReferences.add(new TlvAnnotationInfoManifestReference(element));
        }
    }

    @Override
    protected List<TlvAnnotationInfoManifestReference> getElements() {
        return annotationReferences;
    }

    @Override
    public List<? extends FileReference> getAnnotationManifestReferences() {
        return annotationReferences;
    }
}
