package com.guardtime.container.verification.rule.signature.ksi;

import com.guardtime.container.verification.result.SignatureResult;
import com.guardtime.ksi.unisignature.KSISignature;
import com.guardtime.ksi.unisignature.verifier.VerificationResult;

class KsiSignatureResult implements SignatureResult<KSISignature, VerificationResult> {
    private final VerificationResult result;
    private final com.guardtime.container.verification.result.VerificationResult simpleResult;
    private final KSISignature signature;

    KsiSignatureResult(VerificationResult result, com.guardtime.container.verification.result.VerificationResult simpleResult, KSISignature signature) {
        this.result = result;
        this.simpleResult = simpleResult;
        this.signature = signature;
    }

    @Override
    public com.guardtime.container.verification.result.VerificationResult getSimplifiedResult() {
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
