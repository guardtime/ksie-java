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

import com.guardtime.envelope.manifest.AnnotationsManifest;
import com.guardtime.envelope.manifest.FileReference;
import com.guardtime.envelope.manifest.Manifest;
import com.guardtime.envelope.packaging.SignatureContent;
import com.guardtime.envelope.verification.result.GenericVerificationResult;
import com.guardtime.envelope.verification.result.ResultHolder;
import com.guardtime.envelope.verification.result.VerificationResult;
import com.guardtime.envelope.verification.rule.AbstractRule;
import com.guardtime.envelope.verification.rule.state.RuleStateProvider;

import static com.guardtime.envelope.verification.rule.RuleType.KSIE_VERIFY_ANNOTATION_MANIFEST_EXISTS;

/**
 * This rule verifies that the annotations manifest is actually present in the {@link
 * com.guardtime.envelope.packaging.Envelope}
 */
public class AnnotationsManifestExistenceRule extends AbstractRule<SignatureContent> {

    private static final String NAME = KSIE_VERIFY_ANNOTATION_MANIFEST_EXISTS.getName();

    public AnnotationsManifestExistenceRule(RuleStateProvider stateProvider) {
        super(stateProvider.getStateForRule(NAME));
    }

    @Override
    protected void verifyRule(ResultHolder holder, SignatureContent verifiable) {
        VerificationResult verificationResult = getFailureVerificationResult();
        Manifest manifest = verifiable.getManifest();
        FileReference annotationsManifestReference = manifest.getAnnotationsManifestReference();
        String annotationsManifestUri = annotationsManifestReference.getUri();
        AnnotationsManifest annotationsManifest = verifiable.getAnnotationsManifest();
        if (annotationsManifest != null && annotationsManifest.getPath().equals(annotationsManifestUri)) {
            verificationResult = VerificationResult.OK;
        }
        holder.addResult(
                verifiable,
                new GenericVerificationResult(verificationResult, getName(), getErrorMessage(), annotationsManifestUri)
        );
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getErrorMessage() {
        return "Annotations manifest is not present in the envelope.";
    }
}
