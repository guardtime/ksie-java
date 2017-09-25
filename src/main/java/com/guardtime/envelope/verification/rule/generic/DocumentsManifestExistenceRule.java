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

import com.guardtime.envelope.manifest.DocumentsManifest;
import com.guardtime.envelope.manifest.FileReference;
import com.guardtime.envelope.manifest.Manifest;
import com.guardtime.envelope.packaging.Envelope;
import com.guardtime.envelope.packaging.SignatureContent;
import com.guardtime.envelope.util.Pair;
import com.guardtime.envelope.verification.result.GenericVerificationResult;
import com.guardtime.envelope.verification.result.ResultHolder;
import com.guardtime.envelope.verification.result.VerificationResult;
import com.guardtime.envelope.verification.rule.AbstractRule;
import com.guardtime.envelope.verification.rule.RuleTerminatingException;
import com.guardtime.envelope.verification.rule.RuleType;
import com.guardtime.envelope.verification.rule.state.RuleStateProvider;

/**
 * This rule verifies that the documents manifest is actually present in the {@link
 * Envelope}
 */
public class DocumentsManifestExistenceRule extends AbstractRule<SignatureContent> {

    private static final String NAME = RuleType.KSIE_VERIFY_DATA_MANIFEST_EXISTS.getName();

    public DocumentsManifestExistenceRule(RuleStateProvider stateProvider) {
        super(stateProvider.getStateForRule(NAME));
    }

    @Override
    protected void verifyRule(ResultHolder holder, SignatureContent verifiable) throws RuleTerminatingException {

        VerificationResult verificationResult = getFailureVerificationResult();
        Manifest manifest = verifiable.getManifest().getRight();
        FileReference documentsManifestReference = manifest.getDocumentsManifestReference();
        Pair<String, DocumentsManifest> documentsManifest = verifiable.getDocumentsManifest();
        if (documentsManifest != null) {
            verificationResult = VerificationResult.OK;
        }
        String manifestUri = documentsManifestReference.getUri();
        holder.addResult(
                verifiable,
                new GenericVerificationResult(verificationResult, getName(), getErrorMessage(), manifestUri)
        );
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getErrorMessage() {
        return "Datamanifest is not present in the envelope.";
    }
}
