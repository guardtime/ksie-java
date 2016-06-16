package com.guardtime.container.verification.rule.ksi;

import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.signature.ContainerSignature;
import com.guardtime.container.verification.result.GenericVerificationResult;
import com.guardtime.container.verification.result.RuleVerificationResult;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.container.verification.rule.AbstractRule;
import com.guardtime.container.verification.rule.Rule;
import com.guardtime.container.verification.rule.RuleState;
import com.guardtime.ksi.KSI;
import com.guardtime.ksi.exceptions.KSIException;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;
import com.guardtime.ksi.unisignature.KSISignature;
import com.guardtime.ksi.unisignature.verifier.policies.Policy;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Rule that verifies the {@link ContainerSignature} of a {@link SignatureContent} by using KSI {@link Policy} to verify
 * the underlying signature.<br>Does not assume the underlying signature to be of type {@link KSISignature} but will
 * produce failure result for any other type of underlying signature.
 */
public class KsiPolicyBasedSignatureIntegrityRule extends AbstractRule<SignatureContent> implements Rule<SignatureContent> {
    private final KSI ksi;
    private final Policy policy;

    public KsiPolicyBasedSignatureIntegrityRule(KSI ksi, Policy verificationPolicy) {
        this(RuleState.FAIL, ksi, verificationPolicy);
    }

    public KsiPolicyBasedSignatureIntegrityRule(RuleState state, KSI ksi, Policy verificationPolicy) {
        super(state);
        this.ksi = ksi;
        this.policy = verificationPolicy;
    }

    @Override
    protected List<RuleVerificationResult> verifyRule(SignatureContent verifiable) {
        RuleVerificationResult verificationResult;
        String signatureUri = verifiable.getManifest().getRight().getSignatureReference().getUri();
        ContainerSignature containerSignature = verifiable.getContainerSignature();
        if (containerSignature.supportsSignatureClass(KSISignature.class)) {
            verificationResult = getKSISignatureVerificationResult((KSISignature) containerSignature.getSignature(), verifiable, signatureUri);
        } else {
            verificationResult = new GenericVerificationResult(getFailureVerificationResult(), this, signatureUri, new Exception("Unsupported "));
        }
        return Arrays.asList(verificationResult);
    }

    private RuleVerificationResult getKSISignatureVerificationResult(KSISignature signature, SignatureContent verifiable, String signatureUri) {
        VerificationResult ruleResult = getFailureVerificationResult();
        try {
            HashAlgorithm hashAlgorithm = signature.getInputHash().getAlgorithm();
            DataHash realHash = verifiable.getManifest().getRight().getDataHash(hashAlgorithm);
            com.guardtime.ksi.unisignature.verifier.VerificationResult ksiVerificationResult = ksi.verify(signature, policy, realHash);
            if (ksiVerificationResult.isOk()) {
                ruleResult = VerificationResult.OK;
            }
            return new GenericVerificationResult(ruleResult, this, signatureUri);
        } catch (KSIException | IOException e) {
            LOGGER.info("Verifying signature failed!", e);
            return new GenericVerificationResult(ruleResult, this, signatureUri, e);
        }
    }
}
