package com.guardtime.container.manifest.tlv;

import com.guardtime.container.manifest.AnnotationsManifest;
import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.tlv.TLVParserException;

import java.io.InputStream;
import java.util.List;

public class TlvAnnotationsManifest extends TlvManifestStructure implements AnnotationsManifest {
    private static final byte[] MAGIC = "KSIEANMF".getBytes();  // TODO: Verify from spec
    private List<TLVElement> annotationReferences;

    public TlvAnnotationsManifest(List<TLVElement> elements, String uri) throws TLVParserException {
        super(elements);
        this.setUri(uri);
    }

    public TlvAnnotationsManifest(InputStream stream, String uri) throws TLVParserException {
        super(stream);
        this.setUri(uri);
    }

    @Override
    protected byte[] getMagic() {
        return MAGIC;
    }

    @Override
    protected List<TLVElement> getElements() {
        return annotationReferences;
    }

    @Override
    protected void setElements(List<TLVElement> tlvElements) {
        this.annotationReferences = tlvElements;
    }
}
