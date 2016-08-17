package com.guardtime.container.manifest;

import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.document.ContainerDocument;
import com.guardtime.container.hash.HashAlgorithmProvider;
import com.guardtime.container.util.Pair;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Creates or parses manifests used for container internal structure.
 * @param <M>     Signature manifest implementation.
 * @param <D>     Data files manifest implementation.
 * @param <A>     Annotations manifest implementation.
 * @param <SA>    Annotation info manifest implementation.
 */
public interface ContainerManifestFactory<M extends Manifest, D extends DocumentsManifest, A extends AnnotationsManifest, SA extends SingleAnnotationManifest> {

    HashAlgorithmProvider getHashAlgorithmProvider();

    M createManifest(Pair<String, D> documentsManifest, Pair<String, A> annotationManifest, Pair<String, String> signatureReference) throws InvalidManifestException;

    D createDocumentsManifest(List<ContainerDocument> files) throws InvalidManifestException;

    A createAnnotationsManifest(Map<String, Pair<ContainerAnnotation, SA>> annotationManifests) throws InvalidManifestException;

    SA createSingleAnnotationManifest(Pair<String, D> documentsManifest, Pair<String, ContainerAnnotation> annotation) throws InvalidManifestException;

    M readManifest(InputStream input) throws InvalidManifestException;

    D readDocumentsManifest(InputStream input) throws InvalidManifestException;

    A readAnnotationsManifest(InputStream input) throws InvalidManifestException;

    SA readSingleAnnotationManifest(InputStream input) throws InvalidManifestException;

    ManifestFactoryType getManifestFactoryType();

}
