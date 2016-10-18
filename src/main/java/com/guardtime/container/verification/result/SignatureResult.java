package com.guardtime.container.verification.result;

/**
 * Contains an aggregate {@link VerificationResult} for signature verification as well as a signature type specific result for
 * signature verification.
 * @param <S>     Signature type.
 * @param <VR>    Signature verification result type.
 */
public interface SignatureResult<S, VR> {

    VerificationResult getSimplifiedResult();

    S getSignature();

    VR getFullResult();

}
