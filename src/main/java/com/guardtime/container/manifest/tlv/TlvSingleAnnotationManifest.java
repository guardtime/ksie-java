package com.guardtime.container.manifest.tlv;

import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.hash.HashAlgorithmProvider;
import com.guardtime.container.manifest.AnnotationDataReference;
import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.manifest.InvalidManifestException;
import com.guardtime.container.manifest.SingleAnnotationManifest;
import com.guardtime.container.util.DataHashException;
import com.guardtime.container.util.Pair;
import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.tlv.TLVInputStream;
import com.guardtime.ksi.tlv.TLVParserException;
import com.guardtime.ksi.tlv.TLVStructure;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static java.util.Arrays.asList;

class TlvSingleAnnotationManifest extends AbstractTlvManifestStructure implements SingleAnnotationManifest {

    private static final byte[] MAGIC = "KSIEANNT".getBytes(StandardCharsets.UTF_8);

    private TlvAnnotationDataReference annotationReference;
    private TlvDocumentsManifestReference documentsManifestReference;

    public TlvSingleAnnotationManifest(Pair<String, ContainerAnnotation> annotation, Pair<String, TlvDocumentsManifest> documentsManifest, HashAlgorithmProvider algorithmProvider) throws InvalidManifestException {
        super(MAGIC);
        try {
            this.annotationReference = new TlvAnnotationDataReference(annotation, algorithmProvider);
            this.documentsManifestReference = new TlvDocumentsManifestReference(documentsManifest.getRight(), documentsManifest.getLeft(), algorithmProvider);
        } catch (TLVParserException | DataHashException e) {
            throw new InvalidManifestException("Failed to generate file reference TLVElement", e);
        }
    }

    public TlvSingleAnnotationManifest(InputStream stream) throws InvalidManifestException {
        super(MAGIC, stream);
        try {
            read(stream);
        } catch (TLVParserException e) {
            throw new InvalidManifestException("Failed to parse TlvSingleAnnotationManifest from InputStream", e);
        } catch (IOException e) {
            throw new InvalidManifestException("Failed to read InputStream", e);
        }
        checkMandatoryElement(documentsManifestReference, "Data manifest reference");
        checkMandatoryElement(annotationReference, "Annotation reference");
    }

    private void read(InputStream stream) throws TLVParserException, IOException {
        TLVInputStream input = toTlvInputStream(stream);
        TLVElement element;
        while (input.hasNextElement()) {
            element = input.readElement();
            switch (element.getType()) {
                case TlvDocumentsManifestReference.DOCUMENTS_MANIFEST_REFERENCE:
                    documentsManifestReference = new TlvDocumentsManifestReference(readOnce(element));
                    break;
                case TlvAnnotationDataReference.ANNOTATION_REFERENCE:
                    annotationReference = new TlvAnnotationDataReference(readOnce(element));
                    break;
                default:
                    verifyCriticalFlag(element);
            }
        }
    }

    @Override
    protected List<TLVStructure> getElements() {
        return asList(documentsManifestReference, annotationReference);
    }

    @Override
    public AnnotationDataReference getAnnotationReference() {
        return annotationReference;
    }

    public FileReference getDocumentsManifestReference() {
        return documentsManifestReference;
    }

}
