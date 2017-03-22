package com.guardtime.container.extending;

import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.manifest.SingleAnnotationManifest;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExtendedSignatureContent extends SignatureContent {

    ExtendedSignatureContent(SignatureContent original) {
        super(
                new Builder()
                        .withManifest(original.getManifest())
                        .withSignature(original.getContainerSignature())
                        .withDocumentsManifest(original.getDocumentsManifest())
                        .withAnnotationsManifest(original.getAnnotationsManifest())
                        .withSingleAnnotationManifests(getSingleAnnotationManifests(original))
                        .withAnnotations(getAnnotations(original))
                        .withDocuments(original.getDocuments().values())
        );
    }

    public boolean isExtended() {
        return getContainerSignature().isExtended();
    }

    private static List<Pair<String, ContainerAnnotation>> getAnnotations(SignatureContent original) {
        List<Pair<String, ContainerAnnotation>> annotationPairs = new ArrayList<>(original.getAnnotations().size());
        for (Map.Entry<String, ContainerAnnotation> entry : original.getAnnotations().entrySet()) {
            annotationPairs.add(Pair.of(entry.getKey(), entry.getValue()));
        }
        return annotationPairs;
    }

    private static List<Pair<String, SingleAnnotationManifest>> getSingleAnnotationManifests(SignatureContent original) {
        List<Pair<String, SingleAnnotationManifest>> singleAnnotationManifestPairs = new ArrayList<>(original.getSingleAnnotationManifests().size());
        for (Map.Entry<String, SingleAnnotationManifest> entry : original.getSingleAnnotationManifests().entrySet()) {
            singleAnnotationManifestPairs.add(Pair.of(entry.getKey(), entry.getValue()));
        }
        return singleAnnotationManifestPairs;
    }

}
