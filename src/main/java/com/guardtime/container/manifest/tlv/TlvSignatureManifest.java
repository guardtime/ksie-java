package com.guardtime.container.manifest.tlv;

import com.guardtime.container.BlockChainContainerException;
import com.guardtime.container.datafile.ContainerDocument;
import com.guardtime.container.manifest.SignatureManifest;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.tlv.TLVParserException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class TlvSignatureManifest implements SignatureManifest {
    private static final byte[] MAGIC = "KSIEMFST".getBytes(); // TODO: Replace with bytes according to spec
    private TlvDataFilesManifest dataManifest;
    private TlvAnnotationsManifest annotationsManifest;

    public TlvSignatureManifest(TlvDataFilesManifest dataManifest, TlvAnnotationsManifest annotationsManifest) {
        this.dataManifest = dataManifest;
        this.annotationsManifest = annotationsManifest;
    }

    @Override
    public DataHash getDataHash() {
        return null;
    }

    @Override
    public InputStream getInputStream() {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bos.write(MAGIC);
            TlvReferenceElementFactory.createDataManifestReferenceTlvElement(dataManifest).writeTo(bos);
            TlvReferenceElementFactory.createSignatureReferenceTlvElement().writeTo(bos);
            TlvReferenceElementFactory.createAnnotationsManifestReferenceTlvElement(annotationsManifest).writeTo(bos);
            return new ByteArrayInputStream(bos.toByteArray());
        } catch (IOException | TLVParserException e) {
            throw new BlockChainContainerException(e);
        }
    }

    @Override
    public String getUri() {
        return null;
    }
}
