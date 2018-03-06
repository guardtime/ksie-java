/*
 * Copyright 2013-2018 Guardtime, Inc.
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

package com.guardtime.envelope.extending;

import com.guardtime.envelope.annotation.Annotation;
import com.guardtime.envelope.manifest.SingleAnnotationManifest;
import com.guardtime.envelope.packaging.SignatureContent;
import com.guardtime.envelope.signature.EnvelopeSignature;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper for a {@link SignatureContent} that has been processed by {@link EnvelopeSignatureExtender}.
 * Provides helper methods to view extending status.
 */
public class ExtendedSignatureContent extends SignatureContent {

    ExtendedSignatureContent(SignatureContent original) {
        super(
                new Builder()
                        .withManifest(original.getManifest())
                        .withSignature(original.getEnvelopeSignature())
                        .withDocumentsManifest(original.getDocumentsManifest())
                        .withAnnotationsManifest(original.getAnnotationsManifest())
                        .withSingleAnnotationManifests(getSingleAnnotationManifests(original))
                        .withAnnotations(getAnnotations(original))
                        .withDocuments(original.getDocuments().values())
        );
    }

    /**
     * @return True, if the {@link EnvelopeSignature} is extended.
     */
    public boolean isExtended() {
        return getEnvelopeSignature().isExtended();
    }

    private static List<Annotation> getAnnotations(SignatureContent original) {
        return new ArrayList<>(original.getAnnotations().values());
    }

    private static List<SingleAnnotationManifest> getSingleAnnotationManifests(SignatureContent original) {
        return new ArrayList<>(original.getSingleAnnotationManifests().values());
    }

}
