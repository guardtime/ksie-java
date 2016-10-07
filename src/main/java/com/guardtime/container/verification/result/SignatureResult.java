package com.guardtime.container.verification.result;

public interface SignatureResult<S, VR> {

    VerificationResult getSimplifiedResult();

    S getSignature();

    VR getFullResult();

}
