package com.guardtime.container.packaging;

import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.document.ContainerDocument;
import com.guardtime.container.manifest.AnnotationsManifest;
import com.guardtime.container.manifest.DocumentsManifest;
import com.guardtime.container.manifest.Manifest;
import com.guardtime.container.manifest.SingleAnnotationManifest;
import com.guardtime.container.signature.ContainerSignature;
import com.guardtime.container.util.Pair;

import java.util.Map;

/**
 * Structure that groups together all container internal structure elements(manifests), documents, annotations and
 * signature that are directly connected to the signature.
 */
public interface SignatureContent {

    /**
     * Provides access to all {@link ContainerDocument} contained by the structure.
     *
     * @return Map containing the name and the document.
     */
    Map<String, ContainerDocument> getDocuments();

    /**
     * Provides access to all {@link ContainerAnnotation} contained by the structure.
     *
     * @return Map containing path and annotation where path is used for container management.
     */
    Map<String, ContainerAnnotation> getAnnotations();

    /**
     * Provides access to the {@link ContainerSignature} which signs the structure and its content.
     */
    ContainerSignature getContainerSignature();

    Pair<String, DocumentsManifest> getDocumentsManifest();

    Pair<String, AnnotationsManifest> getAnnotationsManifest();

    Pair<String, Manifest> getManifest();

    Map<String, SingleAnnotationManifest> getSingleAnnotationManifests();
}
