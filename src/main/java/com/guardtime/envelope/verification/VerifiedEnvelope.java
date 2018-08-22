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

package com.guardtime.envelope.verification;

import com.guardtime.envelope.packaging.Envelope;
import com.guardtime.envelope.packaging.SignatureContent;
import com.guardtime.envelope.packaging.exception.EnvelopeMergingException;
import com.guardtime.envelope.verification.result.ResultHolder;
import com.guardtime.envelope.verification.result.RuleVerificationResult;
import com.guardtime.envelope.verification.result.VerificationResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Encompasses all results from verifying an {@link Envelope}.
 * Provides easier access to overall result of verification.
 */
public class VerifiedEnvelope extends Envelope {
    private final VerificationResult aggregateResult;
    private final ResultHolder resultHolder;

    public VerifiedEnvelope(Envelope envelope, ResultHolder holder) {
        super(getWrappedSignatureContents(envelope.getSignatureContents(), holder), envelope.getUnknownFiles());
        this.resultHolder = holder;
        this.aggregateResult = resultHolder.getAggregatedResult();
    }

    private static Collection<SignatureContent> getWrappedSignatureContents(Collection<SignatureContent> originalContents,
                                                                            ResultHolder resultHolder) {
        List<SignatureContent> verifiedContents = new ArrayList<>(originalContents.size());
        for (SignatureContent content : originalContents) {
            verifiedContents.add(new VerifiedSignatureContent(content, resultHolder));
        }
        return verifiedContents;
    }

    /**
     * Provides access to all the {@link RuleVerificationResult}s gathered during verification.
     * @return List of {@link RuleVerificationResult}s.
     */
    public List<RuleVerificationResult> getResults() {
        return resultHolder.getResults();
    }

    /**
     * Provides access to the overall {@link VerificationResult} of the verification.
     * @return Overall verification result.
     */
    public VerificationResult getVerificationResult() {
        return aggregateResult;
    }

    public List<VerifiedSignatureContent> getVerifiedSignatureContents() {
        List<VerifiedSignatureContent> result = new ArrayList<>();
        for (SignatureContent con : getSignatureContents()) {
            result.add((VerifiedSignatureContent) con);
        }
        return result;
    }

    public void add(SignatureContent content) throws EnvelopeMergingException {
        super.add(new VerifiedSignatureContent(content, resultHolder));
    }

    public void addAll(Collection<SignatureContent> contents) throws EnvelopeMergingException {
        super.addAll(getWrappedSignatureContents(contents, resultHolder));
    }

}
