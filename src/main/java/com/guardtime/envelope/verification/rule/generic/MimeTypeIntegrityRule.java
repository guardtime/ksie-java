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

import com.guardtime.envelope.packaging.Envelope;
import com.guardtime.envelope.packaging.EnvelopePackagingFactory;
import com.guardtime.envelope.packaging.MimeType;
import com.guardtime.envelope.verification.result.GenericVerificationResult;
import com.guardtime.envelope.verification.result.ResultHolder;
import com.guardtime.envelope.verification.result.VerificationResult;
import com.guardtime.envelope.verification.rule.AbstractRule;
import com.guardtime.envelope.verification.rule.RuleTerminatingException;
import com.guardtime.envelope.verification.rule.RuleType;
import com.guardtime.envelope.verification.rule.state.RuleStateProvider;
import com.guardtime.ksi.util.Util;

import java.io.IOException;
import java.util.Arrays;

/**
 * Rule that verifies the existence and content of a MIMETYPE file in the envelope. The expected content is given by
 * {@link EnvelopePackagingFactory}.
 */
public class MimeTypeIntegrityRule extends AbstractRule<Envelope> {
    private static final String NAME = RuleType.KSIE_FORMAT.getName();
    private final byte[] expectedContent;

    public MimeTypeIntegrityRule(RuleStateProvider provider, EnvelopePackagingFactory packagingFactory) {
        super(provider.getStateForRule(NAME));
        this.expectedContent = packagingFactory.getMimeTypeContent();
    }

    @Override
    protected void verifyRule(ResultHolder holder, Envelope verifiable) throws RuleTerminatingException {
        MimeType mimetype = verifiable.getMimeType();
        VerificationResult result = getFailureVerificationResult();
        String mimetypeUri = mimetype.getUri();
        try {
            byte[] realContent = Util.toByteArray(mimetype.getInputStream());
            if (Arrays.equals(expectedContent, realContent)) {
                result = VerificationResult.OK;
            }
            holder.addResult(new GenericVerificationResult(result, getName(), getErrorMessage(), mimetypeUri));
        } catch (IOException e) {
            LOGGER.info("Verifying MIME type failed!", e);
            holder.addResult(new GenericVerificationResult(result, getName(), getErrorMessage(), mimetypeUri, e));
        }

        if (!result.equals(VerificationResult.OK)) {
            throw new RuleTerminatingException("MIME type integrity could not be verified for '" + mimetypeUri + "'");
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getErrorMessage() {
        return "Unsupported format.";
    }
}
