package com.guardtime.container.manifest;


import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.datafile.ContainerDocument;

import java.io.InputStream;
import java.util.List;

public interface ContainerManifestFactory<S extends SignatureManifest,D extends DataFilesManifest,A extends AnnotationsManifest, AI extends AnnotationInfoManifest> {

    S createSignatureManifest(D dataFilesManifest, A annotationManifest);

    D createDataFilesManifest(List<ContainerDocument> files);

    A createAnnotationsManifest(List<AI> annotationManifests);

    AI createAnnotationManifest(D dataManifest, ContainerAnnotation annotation);

    S readSignatureManifest(InputStream input);

    D readDataFilesManifest(InputStream input);

    A readAnnotationsManifest(InputStream input);

    AI readAnnotationManifest(InputStream input);

}
