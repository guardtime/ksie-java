package com.guardtime.container.packaging;

import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.datafile.ContainerDocument;
import com.guardtime.container.manifest.AnnotationInfoManifest;
import com.guardtime.container.manifest.AnnotationsManifest;
import com.guardtime.container.manifest.DataFilesManifest;
import com.guardtime.container.manifest.SignatureManifest;
import com.guardtime.container.signature.ContainerSignature;
import com.guardtime.container.util.Pair;

import java.util.List;

public interface SignatureContent {

    List<ContainerDocument> getDocuments();

    List<Pair<String, ContainerAnnotation>> getAnnotations();

    ContainerSignature getSignature();

    Pair<String, DataFilesManifest> getDataManifest();

    Pair<String, AnnotationsManifest> getAnnotationsManifest();

    Pair<String, SignatureManifest> getSignatureManifest();

    List<Pair<String, AnnotationInfoManifest>> getAnnotationInfoManifests();
}
