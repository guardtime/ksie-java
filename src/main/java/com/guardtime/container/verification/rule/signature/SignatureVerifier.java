package com.guardtime.container.verification.rule.signature;

import com.guardtime.container.manifest.Manifest;
import com.guardtime.container.signature.ContainerSignature;
import com.guardtime.container.verification.result.SignatureResult;
import com.guardtime.container.verification.rule.RuleTerminatingException;

/**
 * Provides signature specific verification for Container verification process.
 * @param <S>    Signature type that can be verified
 */
public interface SignatureVerifier<S> {

    Boolean isSupported(ContainerSignature containerSignature);

    SignatureResult getSignatureVerificationResult(S signature, Manifest manifest) throws RuleTerminatingException;
}
