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

import com.guardtime.envelope.EnvelopeElement;
import com.guardtime.envelope.manifest.FileReference;
import com.guardtime.envelope.manifest.Manifest;
import com.guardtime.envelope.packaging.SignatureContent;
import com.guardtime.envelope.util.Pair;
import com.guardtime.envelope.verification.result.ResultHolder;
import com.guardtime.envelope.verification.result.RuleVerificationResult;
import com.guardtime.envelope.verification.result.VerificationResultFilter;
import com.guardtime.envelope.verification.rule.AbstractRule;
import com.guardtime.envelope.verification.rule.RuleTerminatingException;
import com.guardtime.envelope.verification.rule.state.RuleStateProvider;

import java.util.HashSet;
import java.util.Set;

import static com.guardtime.envelope.verification.rule.RuleType.KSIE_VERIFY_ANNOTATION_MANIFEST;
import static com.guardtime.envelope.verification.rule.RuleType.KSIE_VERIFY_ANNOTATION_MANIFEST_EXISTS;

/**
 * This rule verifies that the annotmanifest has not been corrupted.
 * It expects to find successful results for rules verifying existence of
 * {@link com.guardtime.envelope.manifest.AnnotationsManifest}.
 */
public class AnnotationsManifestIntegrityRule extends AbstractRule<SignatureContent> {

    private static final String NAME = KSIE_VERIFY_ANNOTATION_MANIFEST.getName();
    private final MultiHashElementIntegrityRule integrityRule;

    public AnnotationsManifestIntegrityRule(RuleStateProvider stateProvider) {
        super(stateProvider.getStateForRule(NAME));
        integrityRule = new MultiHashElementIntegrityRule(stateProvider.getStateForRule(NAME), NAME);
    }

    @Override
    protected void verifyRule(ResultHolder holder, SignatureContent verifiable) {
        EnvelopeElement annotationsManifest = verifiable.getAnnotationsManifest();
        Manifest manifest = verifiable.getManifest();
        FileReference annotationsManifestReference = manifest.getAnnotationsManifestReference();
        ResultHolder tempHolder = new ResultHolder();
        try {
            integrityRule.verify(tempHolder, Pair.of(annotationsManifest, annotationsManifestReference));
        } catch (RuleTerminatingException e) {
            LOGGER.info("Annotations manifest hash verification failed with message: '{}'", e.getMessage());
        } finally {
            holder.addResults(verifiable, tempHolder.getResults());
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getErrorMessage() {
        return "Annotation manifest hash mismatch.";
    }

    @Override
    protected VerificationResultFilter getFilter(ResultHolder holder, SignatureContent verifiable) {
        final Set<RuleVerificationResult> results = new HashSet<>(holder.getResults(verifiable));
        return new VerificationResultFilter() {
            @Override
            public boolean apply(RuleVerificationResult result) {
                return results.contains(result) && result.getRuleName().equals(KSIE_VERIFY_ANNOTATION_MANIFEST_EXISTS.getName());
            }
        };
    }

}
