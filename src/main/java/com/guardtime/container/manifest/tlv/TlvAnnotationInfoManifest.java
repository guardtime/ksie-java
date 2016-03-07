package com.guardtime.container.manifest.tlv;

import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.manifest.AnnotationInfoManifest;
import com.guardtime.container.manifest.AnnotationReference;
import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.manifest.InvalidManifestException;
import com.guardtime.container.util.Pair;
import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.tlv.TLVInputStream;
import com.guardtime.ksi.tlv.TLVParserException;
import com.guardtime.ksi.tlv.TLVStructure;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static java.util.Arrays.asList;

class TlvAnnotationInfoManifest extends AbstractTlvManifestStructure implements AnnotationInfoManifest {

    private static final byte[] MAGIC = "KSIEANNT".getBytes();

    private TlvAnnotationReference annotationReference;
    private TlvDataFilesManifestReference dataManifestReference;

    public TlvAnnotationInfoManifest(Pair<String, ContainerAnnotation> annotation, Pair<String, TlvDataFilesManifest> dataManifest) throws InvalidManifestException {
        super(MAGIC);
        try {
            this.annotationReference = new TlvAnnotationReference(annotation);
            this.dataManifestReference = new TlvDataFilesManifestReference(dataManifest.getRight(), dataManifest.getLeft());
        } catch (TLVParserException | IOException e) {
            throw new InvalidManifestException("Failed to generate file reference TLVElement", e);
        }
    }

    public TlvAnnotationInfoManifest(InputStream stream) throws InvalidManifestException {
        super(MAGIC, stream);
        try {
            read(stream);
        } catch (TLVParserException | IOException e) {
            throw new InvalidManifestException(e);
        }
        checkMandatoryElement(dataManifestReference, "Data manifest reference");
        checkMandatoryElement(annotationReference, "Annotation reference");
    }

    private void read(InputStream stream) throws TLVParserException, IOException {
        TLVInputStream input = toTlvInputStream(stream);
        TLVElement element;
        while (input.hasNextElement()) {
            element = input.readElement();
            switch (element.getType()) {
                case TlvDataFilesManifestReference.DATA_FILES_MANIFEST_REFERENCE:
                    dataManifestReference = new TlvDataFilesManifestReference(readOnce(element));
                    break;
                case TlvAnnotationReference.ANNOTATION_REFERENCE:
                    annotationReference = new TlvAnnotationReference(readOnce(element));
                    break;
                default:
                    verifyCriticalFlag(element);
            }
        }
    }

    @Override
    protected List<TLVStructure> getElements() {
        return asList(dataManifestReference, annotationReference);
    }

    @Override
    public AnnotationReference getAnnotationReference() {
        return annotationReference;
    }

    @Override
    public FileReference getDataManifestReference() {
        return dataManifestReference;
    }

    @Override
    public boolean writable() {
        return true;
    }

}
