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

import com.guardtime.envelope.document.Document;
import com.guardtime.envelope.manifest.FileReference;
import com.guardtime.envelope.EnvelopeElement;
import com.guardtime.envelope.packaging.SignatureContent;
import com.guardtime.envelope.util.Pair;
import com.guardtime.envelope.verification.result.ResultHolder;
import com.guardtime.envelope.verification.result.RuleVerificationResult;
import com.guardtime.envelope.verification.result.VerificationResultFilter;
import com.guardtime.envelope.verification.rule.AbstractRule;
import com.guardtime.envelope.verification.rule.RuleTerminatingException;
import com.guardtime.envelope.verification.rule.RuleType;
import com.guardtime.envelope.verification.rule.state.RuleStateProvider;

import java.util.HashSet;
import java.util.Set;

import static com.guardtime.envelope.verification.result.VerificationResult.OK;
import static com.guardtime.envelope.verification.rule.RuleType.KSIE_VERIFY_DATA_EXISTS;
import static com.guardtime.envelope.verification.rule.RuleType.KSIE_VERIFY_DATA_MANIFEST;
import static com.guardtime.envelope.verification.rule.RuleType.KSIE_VERIFY_DATA_MANIFEST_EXISTS;

/**
 * This rule verifies that the {@link Document} being tested has not been corrupted.
 * It expects to find successful results for rules verifying existence and integrity of
 * {@link com.guardtime.envelope.manifest.DocumentsManifest} and document existence.
 */
public class DocumentIntegrityRule extends AbstractRule<SignatureContent> {

    private static final String NAME = RuleType.KSIE_VERIFY_DATA_HASH.getName();
    private final MultiHashElementIntegrityRule integrityRule;

    public DocumentIntegrityRule(RuleStateProvider stateProvider) {
        super(stateProvider.getStateForRule(NAME));
        integrityRule = new MultiHashElementIntegrityRule(stateProvider.getStateForRule(NAME), NAME);
    }

    @Override
    protected void verifyRule(ResultHolder holder, SignatureContent verifiable) throws RuleTerminatingException {
        for (FileReference documentReference : verifiable.getDocumentsManifest().getDocumentReferences()) {
            String uri = documentReference.getUri();
            if (existenceRuleFailed(holder, verifiable, uri)) continue;

            EnvelopeElement document = verifiable.getDocuments().get(uri);

            ResultHolder tempHolder = new ResultHolder();
            try {
                integrityRule.verify(tempHolder, Pair.of(document, documentReference));
            } catch (RuleTerminatingException e) {
                LOGGER.info("Data file hash verification failed with message: '{}'", e.getMessage());
            } finally {
                holder.addResults(verifiable, tempHolder.getResults());
            }
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getErrorMessage() {
        return "Data file hash mismatch.";
    }

    @Override
    protected VerificationResultFilter getFilter(ResultHolder holder, SignatureContent verifiable) {
        final Set<RuleVerificationResult> results = new HashSet<>(holder.getResults(verifiable));
        return new VerificationResultFilter() {
            @Override
            public boolean apply(RuleVerificationResult result) {
                return results.contains(result) && (result.getRuleName().equals(KSIE_VERIFY_DATA_MANIFEST_EXISTS.getName()) ||
                        result.getRuleName().equals(KSIE_VERIFY_DATA_MANIFEST.getName()));
            }
        };
    }

    private boolean existenceRuleFailed(ResultHolder holder, SignatureContent verifiable, final String documentUri) {
        final Set<RuleVerificationResult> results = new HashSet<>(holder.getResults(verifiable));
        VerificationResultFilter filter = new VerificationResultFilter() {
            @Override
            public boolean apply(RuleVerificationResult result) {
                return results.contains(result) && (result.getRuleName().equals(KSIE_VERIFY_DATA_EXISTS.getName()) &&
                        result.getTestedElementPath().equals(documentUri));
            }
        };
        return !holder.getFilteredAggregatedResult(filter, 1).equals(OK);
    }

}
