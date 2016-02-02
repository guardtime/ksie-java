package com.guardtime.container.manifest.tlv;

import com.guardtime.container.manifest.AnnotationInfoManifest;
import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.tlv.TLVParserException;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

public class TlvAnnotationInfoManifest extends TlvManifestStructure implements AnnotationInfoManifest {
    private static final byte[] MAGIC = "KSIEANNT".getBytes();  // TODO: Verify from spec
    private TLVElement annotationReference;
    private TLVElement dataManifestReference;

    public TlvAnnotationInfoManifest(List<TLVElement> elements, String uri) throws TLVParserException {
        super(elements);
        this.setUri(uri);
    }

    public TlvAnnotationInfoManifest(InputStream stream, String uri) throws TLVParserException {
        super(stream);
        this.setUri(uri);
    }

    @Override
    protected byte[] getMagic() {
        return MAGIC;
    }

    @Override
    protected List<TLVElement> getElements() {
        LinkedList<TLVElement> returnable = new LinkedList<>();
        returnable.add(dataManifestReference);
        returnable.add(annotationReference);
        return returnable;
    }

    @Override
    protected void setElements(List<TLVElement> tlvElements) throws TLVParserException {
        for (TLVElement element : tlvElements) {
            switch (TlvTypes.fromValue(element.getType())) {
                case DATA_FILES_MANIFEST_REFERENCE:
                    dataManifestReference = readOnce(element);
                    break;
                case ANNOTATION_REFERENCE:
                    annotationReference = readOnce(element);
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
