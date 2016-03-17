package com.guardtime.container.manifest;

import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.datafile.ContainerDocument;
import com.guardtime.container.util.Pair;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface ContainerManifestFactory<S extends SignatureManifest, D extends DataFilesManifest, A extends AnnotationsManifest, AI extends AnnotationInfoManifest> {

    S createSignatureManifest(Pair<String, D> dataFilesManifest, Pair<String, A> annotationManifest, Pair<String, String> signatureReference) throws InvalidManifestException;

    D createDataFilesManifest(List<ContainerDocument> files) throws InvalidManifestException;

    A createAnnotationsManifest(Map<String, Pair<ContainerAnnotation, AI>> annotationManifests) throws InvalidManifestException;

    AI createAnnotationManifest(Pair<String, D> dataManifest, Pair<String, ContainerAnnotation> annotation) throws InvalidManifestException;

    S readSignatureManifest(InputStream input) throws InvalidManifestException;

    D readDataFilesManifest(InputStream input) throws InvalidManifestException;

    A readAnnotationsManifest(InputStream input) throws InvalidManifestException;

    AI readAnnotationManifest(InputStream input) throws InvalidManifestException;

    ManifestFactoryType getManifestFactoryType();

}
