package com.guardtime.container.packaging;

import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.datafile.ContainerDocument;
import com.guardtime.container.extending.SignatureExtender;
import com.guardtime.container.manifest.AnnotationInfoManifest;
import com.guardtime.container.manifest.AnnotationsManifest;
import com.guardtime.container.manifest.DataFilesManifest;
import com.guardtime.container.manifest.SignatureManifest;
import com.guardtime.container.signature.ContainerSignature;
import com.guardtime.container.util.Pair;

import java.util.Map;

/**
 * Structure that groups together all KSIE container internal structure elements(manifests), documents and annotations
 * that are directly connected to a signature, which is also included in the structure.
 */
public interface SignatureContent {

    Map<String, ContainerDocument> getDocuments();

    Map<String, ContainerAnnotation> getAnnotations();

    ContainerSignature getSignature();

    Pair<String, DataFilesManifest> getDataManifest();

    Pair<String, AnnotationsManifest> getAnnotationsManifest();

    Pair<String, SignatureManifest> getSignatureManifest();

    /**
     * Updates the existing ContainerSignature maintained by the SignatureContent to extend it to a trust anchor
     * @param signatureExtender
     * @return true when signature is extended
     */
    boolean extendSignature(SignatureExtender signatureExtender);

    Map<String, AnnotationInfoManifest> getAnnotationInfoManifests();
}
