package com.guardtime.container.manifest.tlv;

import com.guardtime.container.BlockChainContainerException;
import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.manifest.AnnotationInfoManifest;

import java.io.InputStream;

public class TlvAnnotationInfoManifest implements AnnotationInfoManifest {
    private static final byte[] MAGIC = "KSIEANNT".getBytes(); // TODO: Replace with bytes according to spec
    private static final String TLV_EXTENSION = "tlv";
    private ContainerAnnotation annotation;
    private TlvDataFilesManifest dataManifest;

    public TlvAnnotationInfoManifest(ContainerAnnotation annotation, TlvDataFilesManifest dataManifest) {
        this.annotation = annotation;
        this.dataManifest = dataManifest;
    }

    @Override
    public InputStream getInputStream() throws BlockChainContainerException {
        return TlvUtil.generateInputStream(
                MAGIC,
                TlvReferenceElementFactory.createDataManifestReferenceTlvElement(dataManifest),
                TlvReferenceElementFactory.createAnnotationReferenceTlvElement(annotation)
        );
    }

    @Override
    public String getUri() {
        String baseUri = annotation.getUri();
        String uriStr;
        if (annotation.getUri().lastIndexOf(".") > 0) {
            uriStr = baseUri.substring(0, annotation.getUri().lastIndexOf(".")) + TLV_EXTENSION;
        } else {
            uriStr = baseUri + "." + TLV_EXTENSION;
        }
        return uriStr;
    }

    public ContainerAnnotation getAnnotation() {
        return annotation;
    }
}
