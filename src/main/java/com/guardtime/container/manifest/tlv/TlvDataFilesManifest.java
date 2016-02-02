package com.guardtime.container.manifest.tlv;

import com.guardtime.container.manifest.DataFilesManifest;
import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.tlv.TLVParserException;

import java.io.InputStream;
import java.util.List;

public class TlvDataFilesManifest extends TlvManifestStructure implements DataFilesManifest {
    private static final byte[] MAGIC = "KSIEDAMF".getBytes();  // TODO: Verify from spec
    private List<TLVElement> documents;

    public TlvDataFilesManifest(List<TLVElement> elements, String uri) throws TLVParserException {
        super(elements);
        this.setUri(uri);
    }

    public TlvDataFilesManifest(InputStream stream, String uri) throws TLVParserException {
        super(stream);
        this.setUri(uri);
    }

    @Override
    protected byte[] getMagic() {
        return MAGIC;
    }

    @Override
    protected List<TLVElement> getElements() {
        return documents;
    }

    @Override
    protected void setElements(List<TLVElement> tlvElements) {
        this.documents = tlvElements;
    }
}
