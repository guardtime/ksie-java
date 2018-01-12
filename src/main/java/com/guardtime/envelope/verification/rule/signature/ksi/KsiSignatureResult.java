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

package com.guardtime.envelope.verification.rule.signature.ksi;

import com.guardtime.envelope.verification.result.SignatureResult;
import com.guardtime.ksi.unisignature.KSISignature;
import com.guardtime.ksi.unisignature.verifier.VerificationResult;

public class KsiSignatureResult implements SignatureResult<KSISignature, VerificationResult> {
    private final VerificationResult result;
    private final com.guardtime.envelope.verification.result.VerificationResult simpleResult;
    private final KSISignature signature;

    public KsiSignatureResult(VerificationResult result,
                              com.guardtime.envelope.verification.result.VerificationResult simpleResult,
                              KSISignature signature) {
        this.result = result;
        this.simpleResult = simpleResult;
        this.signature = signature;
    }

    @Override
    public com.guardtime.envelope.verification.result.VerificationResult getSimplifiedResult() {
        return simpleResult;
    }

    @Override
    public KSISignature getSignature() {
        return signature;
    }

    @Override
    public VerificationResult getFullResult() {
        return result;
    }
}
