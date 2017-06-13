/*
 * Copyright 2013-2017 Guardtime, Inc.
 *
 * This file is part of the Guardtime client SDK.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES, CONDITIONS, OR OTHER LICENSES OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 * "Guardtime" and "KSI" are trademarks or registered trademarks of
 * Guardtime, Inc., and no license to trademarks is granted; Guardtime
 * reserves and retains all trademark rights.
 */

package com.guardtime.container.extending;

import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.manifest.SingleAnnotationManifest;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Wrapper for a {@link SignatureContent} that has been processed by {@link ContainerSignatureExtender}.
 * Provides helper methods to view extending status.
 */
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

    /**
     * Returns true if the {@link com.guardtime.container.signature.ContainerSignature} is extended.
     */
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
        Map<String, SingleAnnotationManifest> singleAnnotationManifests = original.getSingleAnnotationManifests();
        List<Pair<String, SingleAnnotationManifest>> singleAnnotationManifestPairs = new ArrayList<>(singleAnnotationManifests.size());
        for (Map.Entry<String, SingleAnnotationManifest> entry : singleAnnotationManifests.entrySet()) {
            singleAnnotationManifestPairs.add(Pair.of(entry.getKey(), entry.getValue()));
        }
        return singleAnnotationManifestPairs;
    }

}
