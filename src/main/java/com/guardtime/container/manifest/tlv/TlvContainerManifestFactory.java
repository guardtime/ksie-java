package com.guardtime.container.manifest.tlv;

import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.manifest.*;
import com.guardtime.container.util.Util;

import java.io.InputStream;
import java.util.List;

public class TlvContainerManifestFactory implements ContainerManifestFactory<TlvSignatureManifest, TlvDataFilesManifest, TlvAnnotationsManifest, TlvAnnotationInfoManifest> {

    @Override
    public TlvSignatureManifest createSignatureManifest(TlvDataFilesManifest dataFilesManifest, TlvAnnotationsManifest annotationManifest) {
        Util.notNull(dataFilesManifest, "Document manifest");
        Util.notNull(annotationManifest, "Annotations manifest");
        return new TlvSignatureManifest(dataFilesManifest, annotationManifest);
    }

    @Override
    public TlvAnnotationsManifest createAnnotationsManifest(List annotationManifests) {
        Util.notEmpty(annotationManifests, "Annotation info manifests list");
        return new TlvAnnotationsManifest(annotationManifests);
    }

    @Override
    public TlvAnnotationInfoManifest createAnnotationManifest(TlvDataFilesManifest dataManifest, ContainerAnnotation annotation) {
        Util.notNull(dataManifest, "Document manifest");
        Util.notNull(annotation, "Annotation");
        return new TlvAnnotationInfoManifest(annotation, dataManifest);
    }

    @Override
    public TlvDataFilesManifest createDataFilesManifest(List files) {
        Util.notEmpty(files, "Document files list");
        return new TlvDataFilesManifest(files);
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
