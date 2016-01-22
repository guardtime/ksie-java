package com.guardtime.container.manifest.tlv;

import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.manifest.ContainerManifestFactory;
import com.guardtime.container.util.Util;

import java.io.InputStream;
import java.util.List;

public class TlvContainerManifestFactory implements ContainerManifestFactory<TlvSignatureManifest, TlvDataFilesManifest, TlvAnnotationsManifest, TlvAnnotationInfoManifest> {

    private static final String TLV_EXTENSION = ".tlv";

    @Override
    public TlvSignatureManifest createSignatureManifest(TlvDataFilesManifest dataFilesManifest, TlvAnnotationsManifest annotationManifest, String manifestUri) {
        Util.notNull(dataFilesManifest, "Document manifest");
        Util.notNull(annotationManifest, "Annotations manifest");
        return new TlvSignatureManifest(dataFilesManifest, annotationManifest, manifestUri + TLV_EXTENSION);
    }

    @Override
    public TlvAnnotationsManifest createAnnotationsManifest(List annotationManifests, String manifestUri) {
        Util.notEmpty(annotationManifests, "Annotation info manifests list");
        return new TlvAnnotationsManifest(annotationManifests, manifestUri + TLV_EXTENSION);
    }

    @Override
    public TlvAnnotationInfoManifest createAnnotationManifest(TlvDataFilesManifest dataManifest, ContainerAnnotation annotation) {
        Util.notNull(dataManifest, "Document manifest");
        Util.notNull(annotation, "Annotation");
        return new TlvAnnotationInfoManifest(annotation, dataManifest);
    }

    @Override
    public TlvDataFilesManifest createDataFilesManifest(List files, String manifestUri) {
        Util.notEmpty(files, "Document files list");
        return new TlvDataFilesManifest(files, manifestUri + TLV_EXTENSION);
    }

    @Override
    public TlvSignatureManifest readSignatureManifest(InputStream input) {
        return null;
    }

    @Override
    public TlvDataFilesManifest readDataFilesManifest(InputStream input) {
        return null;
    }

    @Override
    public TlvAnnotationsManifest readAnnotationsManifest(InputStream input) {
        return null;
    }

    @Override
    public TlvAnnotationInfoManifest readAnnotationManifest(InputStream input) {
        return null;
    }
}
