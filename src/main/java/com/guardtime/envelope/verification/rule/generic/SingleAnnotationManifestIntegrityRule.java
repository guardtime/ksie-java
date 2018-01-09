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

import com.guardtime.envelope.EnvelopeElement;
import com.guardtime.envelope.annotation.EnvelopeAnnotationType;
import com.guardtime.envelope.manifest.DocumentsManifest;
import com.guardtime.envelope.manifest.FileReference;
import com.guardtime.envelope.manifest.SingleAnnotationManifest;
import com.guardtime.envelope.packaging.SignatureContent;
import com.guardtime.envelope.util.Pair;
import com.guardtime.envelope.verification.result.GenericVerificationResult;
import com.guardtime.envelope.verification.result.ResultHolder;
import com.guardtime.envelope.verification.result.RuleVerificationResult;
import com.guardtime.envelope.verification.result.VerificationResultFilter;
import com.guardtime.envelope.verification.rule.AbstractRule;
import com.guardtime.envelope.verification.rule.RuleTerminatingException;
import com.guardtime.envelope.verification.rule.state.RuleState;
import com.guardtime.envelope.verification.rule.state.RuleStateProvider;

import java.util.HashSet;
import java.util.Set;

import static com.guardtime.envelope.verification.result.VerificationResult.NOK;
import static com.guardtime.envelope.verification.result.VerificationResult.OK;
import static com.guardtime.envelope.verification.rule.RuleType.KSIE_VERIFY_ANNOTATION;
import static com.guardtime.envelope.verification.rule.RuleType.KSIE_VERIFY_ANNOTATION_EXISTS;
import static com.guardtime.envelope.verification.rule.RuleType.KSIE_VERIFY_ANNOTATION_MANIFEST;
import static com.guardtime.envelope.verification.rule.RuleType.KSIE_VERIFY_ANNOTATION_MANIFEST_EXISTS;

/**
 * This rule verifies the validity of the manifest file containing meta-data for an annotation.
 * It expects to find successful results for rules verifying existence and integrity of
 * {@link com.guardtime.envelope.manifest.AnnotationsManifest} and existence of {@link SingleAnnotationManifest}.
 */
public class SingleAnnotationManifestIntegrityRule extends AbstractRule<SignatureContent> {

    private static final String NAME = KSIE_VERIFY_ANNOTATION.getName();
    private final MultiHashElementIntegrityRule multiHashElementIntegrityRule;

    public SingleAnnotationManifestIntegrityRule(RuleStateProvider stateProvider) {
        super(stateProvider.getStateForRule(NAME));
        multiHashElementIntegrityRule = new MultiHashElementIntegrityRule(stateProvider.getStateForRule(NAME), NAME);
    }

    @Override
    protected void verifyRule(ResultHolder holder, SignatureContent verifiable) throws RuleTerminatingException {
        for (FileReference reference : verifiable.getAnnotationsManifest().getSingleAnnotationManifestReferences()) {
            String singleAnnotationManifestUri = reference.getUri();
            if (existenceRuleFailed(holder, singleAnnotationManifestUri)) continue;

            SingleAnnotationManifest manifest = verifiable.getSingleAnnotationManifests().get(singleAnnotationManifestUri);
            DocumentsManifest documentsManifest = verifiable.getDocumentsManifest();
            FileReference documentsManifestReference = manifest.getDocumentsManifestReference();
            ResultHolder tempHolder = new ResultHolder();
            try {
                multiHashElementIntegrityRule.verify(tempHolder, Pair.of((EnvelopeElement) manifest, reference));

                if (!documentsManifest.getPath().equals(documentsManifestReference.getUri())) {
                    tempHolder.addResult(
                            verifiable,
                            new GenericVerificationResult(NOK, getName(), getErrorMessage(), reference.getUri())
                    );
                    throw new RuleTerminatingException("Documents manifest path mismatch found.");
                }
                multiHashElementIntegrityRule.verifyRule(
                        tempHolder,
                        Pair.of((EnvelopeElement) documentsManifest, documentsManifestReference)
                );
            } catch (RuleTerminatingException e) {
                // we do not let this through
                LOGGER.info("Annotation manifest hash verification failed with message: '{}'", e.getMessage());
            } finally {
                RuleState ruleState = getRuleState(reference);
                for (RuleVerificationResult result : tempHolder.getResults()) {
                    if (!result.getVerificationResult().equals(OK) && ruleState.equals(RuleState.IGNORE)) {
                        // We ignore problems
                        continue;
                    }
                    holder.addResult(verifiable, result);
                }
            }
        }
    }

    private RuleState getRuleState(FileReference reference) {
        EnvelopeAnnotationType type = EnvelopeAnnotationType.fromContent(reference.getMimeType());
        return type.equals(EnvelopeAnnotationType.FULLY_REMOVABLE) ? RuleState.IGNORE : state;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getErrorMessage() {
        return "Annotation meta-data mismatch.";
    }

    @Override
    protected VerificationResultFilter getFilter(ResultHolder holder, SignatureContent verifiable) {
        final Set<RuleVerificationResult> results = new HashSet<>(holder.getResults(verifiable));
        return new VerificationResultFilter() {
            @Override
            public boolean apply(RuleVerificationResult result) {
                return results.contains(result) && (result.getRuleName().equals(KSIE_VERIFY_ANNOTATION_MANIFEST_EXISTS.getName()) ||
                        result.getRuleName().equals(KSIE_VERIFY_ANNOTATION_MANIFEST.getName()));
            }
        };
    }

    private boolean existenceRuleFailed(ResultHolder holder, final String uri) {
        VerificationResultFilter filter = new VerificationResultFilter() {
            @Override
            public boolean apply(RuleVerificationResult result) {
                return result.getRuleName().equals(KSIE_VERIFY_ANNOTATION_EXISTS.getName()) &&
                        result.getTestedElementPath().equals(uri);
            }
        };
        return !holder.getFilteredAggregatedResult(filter, 1).equals(OK);
    }

}
