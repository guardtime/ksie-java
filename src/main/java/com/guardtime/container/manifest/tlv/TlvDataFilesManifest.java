package com.guardtime.container.manifest.tlv;

import com.guardtime.container.manifest.DataFilesManifest;
import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.tlv.TLVParserException;

import java.io.InputStream;
import java.util.List;

public class TlvDataFilesManifest extends TlvManifestStructure implements DataFilesManifest {
    private static final byte[] MAGIC = "KSIEDAMF".getBytes();  // TODO: Verify from spec
    private String uri;
    private List<TLVElement> documents;

    public TlvDataFilesManifest(List<TLVElement> elements, String uri) throws TLVParserException {
        super(MAGIC, elements);
        this.uri = uri;
    }

    public TlvDataFilesManifest(InputStream stream, String uri) throws TLVParserException {
        super(stream);
        this.uri = uri;
    }

    @Override
    public String getUri() {
        return uri;
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
    protected void setElements(List<TLVElement> tlvElements) throws TLVParserException {
        if (tlvElements == null || tlvElements.isEmpty()) {
            throw new TLVParserException("No elements in manifest");
        }
        this.documents = tlvElements;
    }
}
