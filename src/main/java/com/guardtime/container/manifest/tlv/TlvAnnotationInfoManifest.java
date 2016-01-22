package com.guardtime.container.manifest.tlv;

import com.guardtime.container.BlockChainContainerException;
import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.manifest.AnnotationInfoManifest;
import com.guardtime.ksi.tlv.TLVParserException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

class TlvAnnotationInfoManifest implements AnnotationInfoManifest {
    private static final byte[] MAGIC = "KSIEANNT".getBytes(); // TODO: Replace with bytes according to spec
    private static final String TLV_EXTENSION = "tlv";
    private ContainerAnnotation annotation;
    private TlvDataFilesManifest dataManifest;

    public TlvAnnotationInfoManifest(ContainerAnnotation annotation, TlvDataFilesManifest dataManifest) throws BlockChainContainerException {
        this.annotation = annotation;
        this.dataManifest = dataManifest;
    }

    @Override
    public InputStream getInputStream() {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bos.write(MAGIC);
            TlvReferenceElementFactory.createDataManifestReferenceTlvElement(dataManifest).writeTo(bos);
            TlvReferenceElementFactory.createAnnotationReferenceTlvElement(annotation).writeTo(bos);
            return new ByteArrayInputStream(bos.toByteArray());
        } catch (IOException | TLVParserException e) {
            throw new BlockChainContainerException(e);
        }
    }

    @Override
    public String getUri() {
        String baseUri = annotation.getUri();
        return baseUri.substring(0, annotation.getUri().lastIndexOf(".")) + TLV_EXTENSION;
    }

    public ContainerAnnotation getAnnotation() {
        return annotation;
    }
}
