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

package com.guardtime.envelope.verification.rule.generic;

import com.guardtime.envelope.annotation.Annotation;
import com.guardtime.envelope.annotation.EnvelopeAnnotationType;
import com.guardtime.envelope.manifest.AnnotationDataReference;
import com.guardtime.envelope.manifest.AnnotationsManifest;
import com.guardtime.envelope.manifest.FileReference;
import com.guardtime.envelope.manifest.SingleAnnotationManifest;
import com.guardtime.envelope.packaging.SignatureContent;
import com.guardtime.envelope.util.DataHashException;
import com.guardtime.envelope.verification.result.GenericVerificationResult;
import com.guardtime.envelope.verification.result.ResultHolder;
import com.guardtime.envelope.verification.result.RuleVerificationResult;
import com.guardtime.envelope.verification.result.VerificationResult;
import com.guardtime.envelope.verification.result.VerificationResultFilter;
import com.guardtime.envelope.verification.rule.AbstractRule;
import com.guardtime.envelope.verification.rule.RuleType;
import com.guardtime.envelope.verification.rule.state.RuleState;
import com.guardtime.envelope.verification.rule.state.RuleStateProvider;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;

import java.util.HashSet;
import java.util.Set;

import static com.guardtime.envelope.verification.result.VerificationResult.OK;
import static com.guardtime.envelope.verification.rule.RuleType.KSIE_VERIFY_ANNOTATION;
import static com.guardtime.envelope.verification.rule.RuleType.KSIE_VERIFY_ANNOTATION_DATA_EXISTS;
import static com.guardtime.envelope.verification.rule.RuleType.KSIE_VERIFY_ANNOTATION_EXISTS;
import static com.guardtime.envelope.verification.rule.RuleType.KSIE_VERIFY_ANNOTATION_MANIFEST;
import static com.guardtime.envelope.verification.rule.RuleType.KSIE_VERIFY_ANNOTATION_MANIFEST_EXISTS;

/**
 * This rule verifies that the annotation data has not been corrupted.
 * It expects to find successful results for rules verifying existence and integrity of
 * {@link com.guardtime.envelope.manifest.AnnotationsManifest} and {@link SingleAnnotationManifest} and annotation data.
 */
public class AnnotationDataIntegrityRule extends AbstractRule<SignatureContent> {

    private static final String NAME = RuleType.KSIE_VERIFY_ANNOTATION_DATA.getName();

    public AnnotationDataIntegrityRule(RuleStateProvider stateProvider) {
        super(stateProvider.getStateForRule(NAME));
    }

    @Override
    protected void verifyRule(ResultHolder holder, SignatureContent signatureContent) {
        AnnotationsManifest annotationsManifest = signatureContent.getAnnotationsManifest();
        for (FileReference reference : annotationsManifest.getSingleAnnotationManifestReferences()) {
            String manifestUri = reference.getUri();

            RuleState ruleState = getRuleState(reference);
            VerificationResult verificationResult = getFailureVerificationResult();
            GenericVerificationResult result;

            if (manifestExistenceOrIntegrityRuleFailed(holder, manifestUri)) continue;
            AnnotationDataReference annotationDataReference = getAnnotationDataReference(manifestUri, signatureContent);
            if (dataExistenceRuleFailed(holder, annotationDataReference.getUri())) continue;

            String annotationDataUri = annotationDataReference.getUri();
            Annotation annotation = signatureContent.getAnnotations().get(annotationDataUri);

            try {
                DataHash expectedHash = annotationDataReference.getHash();
                DataHash realHash = annotation.getDataHash(expectedHash.getAlgorithm());
                if (expectedHash.getAlgorithm().getStatus() == HashAlgorithm.Status.NORMAL && expectedHash.equals(realHash)) {
                    verificationResult = VerificationResult.OK;
                }
                result = new GenericVerificationResult(verificationResult, getName(), getErrorMessage(), annotationDataUri);
            } catch (DataHashException e) {
                LOGGER.info("Verifying annotation data failed!", e);
                result = new GenericVerificationResult(verificationResult, getName(), getErrorMessage(), annotationDataUri, e);
            }

            if (!verificationResult.equals(VerificationResult.OK) && ruleState.equals(RuleState.IGNORE)) {
                // We drop non OK for ignored
                continue;
            }

            holder.addResult(signatureContent, result);
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getErrorMessage() {
        return "Annotation data hash mismatch.";
    }

    @Override
    protected VerificationResultFilter getFilter(ResultHolder holder, SignatureContent verifiable) {
        final Set<RuleVerificationResult> results = new HashSet<>(holder.getResults(verifiable));
        return new VerificationResultFilter() {
            @Override
            public boolean apply(RuleVerificationResult result) {
                return results.contains(result) &&
                        (result.getRuleName().equals(KSIE_VERIFY_ANNOTATION_MANIFEST_EXISTS.getName()) ||
                        result.getRuleName().equals(KSIE_VERIFY_ANNOTATION_MANIFEST.getName()));
            }
        };
    }

    private AnnotationDataReference getAnnotationDataReference(String manifestUri, SignatureContent signatureContent) {
        SingleAnnotationManifest manifest = signatureContent.getSingleAnnotationManifests().get(manifestUri);
        return manifest.getAnnotationReference();
    }

    private RuleState getRuleState(FileReference reference) {
        EnvelopeAnnotationType type = EnvelopeAnnotationType.fromContent(reference.getMimeType());
        return type.equals(EnvelopeAnnotationType.NON_REMOVABLE) ? state : RuleState.IGNORE;
    }

    private boolean manifestExistenceOrIntegrityRuleFailed(ResultHolder holder, final String manifestUri) {
        VerificationResultFilter filter = new VerificationResultFilter() {
            @Override
            public boolean apply(RuleVerificationResult result) {
                return result.getTestedElementPath().equals(manifestUri) &&
                        (result.getRuleName().equals(KSIE_VERIFY_ANNOTATION_EXISTS.getName()) ||
                                result.getRuleName().equals(KSIE_VERIFY_ANNOTATION.getName()));
            }
        };
        return !holder.getFilteredAggregatedResult(filter, 2).equals(OK);
    }

    private boolean dataExistenceRuleFailed(ResultHolder holder, final String dataUri) {
        VerificationResultFilter filter = new VerificationResultFilter() {
            @Override
            public boolean apply(RuleVerificationResult result) {
                return (result.getTestedElementPath().equals(dataUri) &&
                        result.getRuleName().equals(KSIE_VERIFY_ANNOTATION_DATA_EXISTS.getName()));
            }
        };
        return !holder.getFilteredAggregatedResult(filter, 1).equals(OK);
    }
}
