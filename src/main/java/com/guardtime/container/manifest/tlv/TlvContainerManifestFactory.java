package com.guardtime.container.manifest.tlv;

import com.guardtime.container.BlockChainContainerException;
import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.datafile.ContainerDocument;
import com.guardtime.container.manifest.ContainerManifestFactory;
import com.guardtime.container.util.Util;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class TlvContainerManifestFactory implements ContainerManifestFactory<TlvSignatureManifest, TlvDataFilesManifest, TlvAnnotationsManifest, TlvAnnotationInfoManifest> {

    private final String TLV_EXTENSION = ".tlv";

    @Override
    public TlvSignatureManifest createSignatureManifest(TlvDataFilesManifest dataFilesManifest, TlvAnnotationsManifest annotationsManifest, String manifestUri) throws BlockChainContainerException {
        Util.notNull(dataFilesManifest, "Document manifest");
        Util.notNull(annotationsManifest, "Annotations manifest");

        String signatureURI = "META-INF/signature1.ksig"; // TODO: Find a solution to generate correct signature path
        return new TlvSignatureManifest(dataFilesManifest, annotationsManifest, signatureURI, manifestUri + TLV_EXTENSION);
    }

    @Override
    public TlvDataFilesManifest createDataFilesManifest(List<ContainerDocument> files, String manifestUri) throws BlockChainContainerException {
        Util.notEmpty(files, "Document files list");
        return new TlvDataFilesManifest(files, manifestUri + TLV_EXTENSION);
    }

    @Override
    public TlvAnnotationsManifest createAnnotationsManifest(Map<ContainerAnnotation, TlvAnnotationInfoManifest> annotationManifests, String manifestUri) throws BlockChainContainerException {
        if (annotationManifests.isEmpty()) {
            throw new IllegalArgumentException("Annotation info manifests list");
        }
        return new TlvAnnotationsManifest(annotationManifests, manifestUri + TLV_EXTENSION);
    }

    @Override
    public TlvAnnotationInfoManifest createAnnotationManifest(TlvDataFilesManifest dataManifest, ContainerAnnotation annotation, String manifestUri) throws BlockChainContainerException {
        Util.notNull(dataManifest, "Document manifest");
        Util.notNull(annotation, "Annotation");
        return new TlvAnnotationInfoManifest(annotation, dataManifest, manifestUri + TLV_EXTENSION);
    }

    @Override
    public TlvSignatureManifest readSignatureManifest(InputStream input, String manifestUri) throws BlockChainContainerException {
        return new TlvSignatureManifest(input, manifestUri);
    }

    @Override
    public TlvDataFilesManifest readDataFilesManifest(InputStream input, String manifestUri) throws BlockChainContainerException {
        return new TlvDataFilesManifest(input, manifestUri);
    }

    @Override
    public TlvAnnotationsManifest readAnnotationsManifest(InputStream input, String manifestUri) throws BlockChainContainerException {
        return new TlvAnnotationsManifest(input, manifestUri);
    }

    @Override
    public TlvAnnotationInfoManifest readAnnotationManifest(InputStream input, String manifestUri) throws BlockChainContainerException {
        return new TlvAnnotationInfoManifest(input, manifestUri);
    }
}
