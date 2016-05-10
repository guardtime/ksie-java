package com.guardtime.container.packaging;

import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.datafile.ContainerDocument;
import com.guardtime.container.extending.SignatureExtender;
import com.guardtime.container.manifest.SingleAnnotationManifest;
import com.guardtime.container.manifest.AnnotationsManifest;
import com.guardtime.container.manifest.DataFilesManifest;
import com.guardtime.container.manifest.SignatureManifest;
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
     *
     * @return
     */
    ContainerSignature getSignature();

    Pair<String, DataFilesManifest> getDataManifest();

    Pair<String, AnnotationsManifest> getAnnotationsManifest();

    Pair<String, SignatureManifest> getSignatureManifest();

    /**
     * Updates the existing ContainerSignature maintained by the SignatureContent to extend it to a trust anchor
     *
     * @param signatureExtender
     *         Provides the signature specific logic for extending the signature.
     * @return true when signature is extended.
     */
    boolean extendSignature(SignatureExtender signatureExtender);

    Map<String, SingleAnnotationManifest> getSingleAnnotationManifests();
}
