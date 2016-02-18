package com.guardtime.container.manifest.tlv;

import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.manifest.AnnotationInfoManifest;
import com.guardtime.container.manifest.DataFilesManifest;
import com.guardtime.container.manifest.reference.tlv.TlvAnnotationReference;
import com.guardtime.container.manifest.reference.tlv.TlvDataFilesManifestReference;
import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.tlv.TLVParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

public class TlvAnnotationInfoManifest extends TlvManifestStructure implements AnnotationInfoManifest {
    private static final byte[] MAGIC = "KSIEANNT".getBytes();  // TODO: Verify from spec
    private TlvAnnotationReference annotationReference;
    private TlvDataFilesManifestReference dataManifestReference;

    public TlvAnnotationInfoManifest(ContainerAnnotation annotation, DataFilesManifest dataManifest, String uri) throws TLVParserException {
        super(uri);
        try {
            this.annotationReference = new TlvAnnotationReference(annotation);
            this.dataManifestReference = new TlvDataFilesManifestReference(dataManifest);
        } catch (IOException e) {
            throw new TLVParserException("Failed to generate file reference TLVElement", e);
        }
    }

    public TlvAnnotationInfoManifest(InputStream stream, String uri) throws TLVParserException {
        super(uri, stream);
        setReferencesFromTLVElements(
                parseElementsFromStream(stream)
        );
    }

    @Override
    protected byte[] getMagic() {
        return MAGIC;
    }

    @Override
    protected List<TLVElement> getElements() {
        LinkedList<TLVElement> returnable = new LinkedList<>();
        returnable.add(dataManifestReference.getRootElement());
        returnable.add(annotationReference.getRootElement());
        return returnable;
    }

    protected void setReferencesFromTLVElements(List<TLVElement> tlvElements) throws TLVParserException {
        for (TLVElement element : tlvElements) {
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

        // Check that all mandatory elements present
        if (dataManifestReference == null || annotationReference == null) {
            throw new TLVParserException("Missing mandatory elements!");
        }
    }
}
