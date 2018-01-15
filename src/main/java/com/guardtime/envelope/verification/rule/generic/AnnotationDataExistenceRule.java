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

package com.guardtime.envelope.verification.rule.generic;

import com.guardtime.envelope.annotation.Annotation;
import com.guardtime.envelope.annotation.EnvelopeAnnotationType;
import com.guardtime.envelope.manifest.FileReference;
import com.guardtime.envelope.manifest.SingleAnnotationManifest;
import com.guardtime.envelope.packaging.Envelope;
import com.guardtime.envelope.packaging.SignatureContent;
import com.guardtime.envelope.verification.result.GenericVerificationResult;
import com.guardtime.envelope.verification.result.ResultHolder;
import com.guardtime.envelope.verification.result.RuleVerificationResult;
import com.guardtime.envelope.verification.result.VerificationResult;
import com.guardtime.envelope.verification.result.VerificationResultFilter;
import com.guardtime.envelope.verification.rule.AbstractRule;
import com.guardtime.envelope.verification.rule.RuleType;
import com.guardtime.envelope.verification.rule.state.RuleState;
import com.guardtime.envelope.verification.rule.state.RuleStateProvider;

import java.util.HashSet;
import java.util.Set;

import static com.guardtime.envelope.verification.result.VerificationResult.OK;
import static com.guardtime.envelope.verification.rule.RuleType.KSIE_VERIFY_ANNOTATION;
import static com.guardtime.envelope.verification.rule.RuleType.KSIE_VERIFY_ANNOTATION_EXISTS;
import static com.guardtime.envelope.verification.rule.RuleType.KSIE_VERIFY_ANNOTATION_MANIFEST;
import static com.guardtime.envelope.verification.rule.RuleType.KSIE_VERIFY_ANNOTATION_MANIFEST_EXISTS;

/**
 * This rule verifies that the annotation data is actually present in the {@link Envelope}
 * It expects to find successful results for rules verifying existence and integrity of
 * {@link com.guardtime.envelope.manifest.AnnotationsManifest} and {@link SingleAnnotationManifest}
 */
public class AnnotationDataExistenceRule extends AbstractRule<SignatureContent> {

    private static final String NAME = RuleType.KSIE_VERIFY_ANNOTATION_DATA_EXISTS.getName();

    public AnnotationDataExistenceRule(RuleStateProvider stateProvider) {
        super(stateProvider.getStateForRule(NAME));
    }

    @Override
    protected void verifyRule(ResultHolder holder, SignatureContent verifiable) {
        for (FileReference reference : verifiable.getAnnotationsManifest().getSingleAnnotationManifestReferences()) {
            String manifestUri = reference.getUri();
            if (anyRuleFailed(holder, manifestUri)) continue;
            RuleState ruleState = getRuleState(reference);
            VerificationResult result = getFailureVerificationResult();

            String dataPath = getAnnotationDataPath(manifestUri, verifiable);
            Annotation annotation = verifiable.getAnnotations().get(dataPath);
            if (annotation != null) {
                result = OK;
            }

            if (!ruleState.equals(RuleState.IGNORE) || result.equals(OK)) {
                holder.addResult(verifiable, new GenericVerificationResult(result, getName(), getErrorMessage(), dataPath));
            }
        }
    }

    private RuleState getRuleState(FileReference reference) {
        EnvelopeAnnotationType type = EnvelopeAnnotationType.fromContent(reference.getMimeType());
        return type.equals(EnvelopeAnnotationType.NON_REMOVABLE) ? state : RuleState.IGNORE;
    }

    private String getAnnotationDataPath(String manifestUri, SignatureContent signatureContent) {
        SingleAnnotationManifest manifest = signatureContent.getSingleAnnotationManifests().get(manifestUri);
        return manifest.getAnnotationReference().getUri();
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getErrorMessage() {
        return "Annotation data missing.";
    }

    @Override
    protected VerificationResultFilter getFilter(ResultHolder holder, SignatureContent verifiable) {
        final Set<RuleVerificationResult> results = new HashSet<>(holder.getResults(verifiable));
        return new VerificationResultFilter() {
            @Override
            public boolean apply(RuleVerificationResult result) {
                return results.contains(result) &&
                        (
                                result.getRuleName().equals(KSIE_VERIFY_ANNOTATION_MANIFEST_EXISTS.getName()) ||
                                result.getRuleName().equals(KSIE_VERIFY_ANNOTATION_MANIFEST.getName())
                        );
            }
        };
    }

    private boolean anyRuleFailed(ResultHolder holder, final String uri) {
        VerificationResultFilter filter = new VerificationResultFilter() {
            @Override
            public boolean apply(RuleVerificationResult result) {
                return result.getTestedElementPath().equals(uri) &&
                        (
                                result.getRuleName().equals(KSIE_VERIFY_ANNOTATION_EXISTS.getName()) ||
                                result.getRuleName().equals(KSIE_VERIFY_ANNOTATION.getName())
                        );
            }
        };
        return !holder.getFilteredAggregatedResult(filter, 2).equals(OK);
    }
}
