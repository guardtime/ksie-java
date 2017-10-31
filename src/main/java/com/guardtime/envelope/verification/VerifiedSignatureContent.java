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

package com.guardtime.envelope.verification;

import com.guardtime.envelope.annotation.Annotation;
import com.guardtime.envelope.manifest.SingleAnnotationManifest;
import com.guardtime.envelope.packaging.SignatureContent;
import com.guardtime.envelope.verification.result.ResultHolder;
import com.guardtime.envelope.verification.result.RuleVerificationResult;
import com.guardtime.envelope.verification.result.SignatureResult;
import com.guardtime.envelope.verification.result.VerificationResult;
import com.guardtime.envelope.verification.result.VerificationResultFilter;

import java.util.ArrayList;
import java.util.List;

public class VerifiedSignatureContent extends SignatureContent {
    private final List<RuleVerificationResult> results;
    private final VerificationResult aggregateResult;
    private List<SignatureResult> signatureResults;


    VerifiedSignatureContent(SignatureContent original, ResultHolder holder) {
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
        this.results = holder.getResults(original);
        this.signatureResults = holder.getSignatureResults(original);
        this.aggregateResult = holder.getFilteredAggregatedResult(new VerificationResultFilter() {
            @Override
            public boolean apply(RuleVerificationResult result) {
                return results != null && results.contains(result);
            }
        });
    }


    private static List<Annotation> getAnnotations(SignatureContent original) {
        return new ArrayList<>(original.getAnnotations().values());
    }

    private static List<SingleAnnotationManifest> getSingleAnnotationManifests(SignatureContent original) {
        return new ArrayList<>(original.getSingleAnnotationManifests().values());
    }

    /**
     * Provides access to {@link SignatureResult}s related to this {@link SignatureContent}.
     */
    public List<SignatureResult> getSignatureResults() {
        return signatureResults;
    }

    /**
     * Provides access to {@link RuleVerificationResult}s related to this {@link SignatureContent}.
     */
    public List<RuleVerificationResult> getResults() {
        return results;
    }

    /**
     * Provides access to the overall {@link VerificationResult} of the verification.
     */
    public VerificationResult getVerificationResult() {
        return aggregateResult;
    }

}
