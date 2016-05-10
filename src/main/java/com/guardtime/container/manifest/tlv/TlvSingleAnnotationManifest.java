package com.guardtime.container.manifest.tlv;

import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.manifest.SingleAnnotationManifest;
import com.guardtime.container.manifest.AnnotationDataReference;
import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.manifest.InvalidManifestException;
import com.guardtime.container.util.Pair;
import com.guardtime.container.util.Util;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;
import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.tlv.TLVInputStream;
import com.guardtime.ksi.tlv.TLVParserException;
import com.guardtime.ksi.tlv.TLVStructure;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static java.util.Arrays.asList;

class TlvSingleAnnotationManifest extends AbstractTlvManifestStructure implements SingleAnnotationManifest {

    private static final byte[] MAGIC = "KSIEANNT".getBytes();

    private TlvAnnotationDataReference annotationReference;
    private TlvDataFilesManifestReference dataManifestReference;

    public TlvSingleAnnotationManifest(Pair<String, ContainerAnnotation> annotation, Pair<String, TlvDataFilesManifest> dataManifest) throws InvalidManifestException {
        super(MAGIC);
        try {
            this.annotationReference = new TlvAnnotationDataReference(annotation);
            this.dataManifestReference = new TlvDataFilesManifestReference(dataManifest.getRight(), dataManifest.getLeft());
        } catch (TLVParserException | IOException e) {
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
        return asList(dataManifestReference, annotationReference);
    }

    @Override
    public AnnotationDataReference getAnnotationReference() {
        return annotationReference;
    }

    @Override
    public FileReference getDataManifestReference() {
        return dataManifestReference;
    }

    @Override
    public DataHash getDataHash(HashAlgorithm algorithm) throws IOException {
        return Util.hash(getInputStream(), algorithm);
    }

}
