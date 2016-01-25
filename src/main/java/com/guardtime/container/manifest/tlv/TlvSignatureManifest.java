package com.guardtime.container.manifest.tlv;

import com.guardtime.container.BlockChainContainerException;
import com.guardtime.container.manifest.SignatureManifest;
import com.guardtime.container.util.Util;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;

import java.io.InputStream;

public class TlvSignatureManifest implements SignatureManifest {
    private static final byte[] MAGIC = "KSIEMFST".getBytes(); // TODO: Replace with bytes according to spec
    private TlvDataFilesManifest dataManifest;
    private TlvAnnotationsManifest annotationsManifest;
    private String uri;

    public TlvSignatureManifest(TlvDataFilesManifest dataManifest, TlvAnnotationsManifest annotationsManifest, String manifestUri) {
        this.dataManifest = dataManifest;
        this.annotationsManifest = annotationsManifest;
        this.uri = manifestUri;
    }

    @Override
    public DataHash getDataHash(HashAlgorithm algorithm) throws BlockChainContainerException {
        return Util.hash(getInputStream(), algorithm);
    }

    @Override
    public InputStream getInputStream() throws BlockChainContainerException {
        return TlvUtil.generateInputStream(
                MAGIC,
                TlvReferenceElementFactory.createDataManifestReferenceTlvElement(dataManifest),
                TlvReferenceElementFactory.createSignatureReferenceTlvElement(),
                TlvReferenceElementFactory.createAnnotationsManifestReferenceTlvElement(annotationsManifest)
        );
    }

    @Override
    public String getUri() {
        return uri;
    }
}
