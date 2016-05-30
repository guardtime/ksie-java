package com.guardtime.container.verification.rule.ksi;

import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.signature.ksi.KsiContainerSignature;
import com.guardtime.ksi.unisignature.verifier.policies.Policy;
import com.guardtime.container.verification.result.GenericVerificationResult;
import com.guardtime.container.verification.result.RuleVerificationResult;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.container.verification.rule.RuleState;
import com.guardtime.container.verification.rule.generic.AbstractRule;
import com.guardtime.ksi.KSI;
import com.guardtime.ksi.exceptions.KSIException;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;
import com.guardtime.ksi.unisignature.KSISignature;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class KsiPolicyBasedSignatureIntegrityRule extends AbstractRule<SignatureContent> {
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
    public List<RuleVerificationResult> verifyRule(SignatureContent verifiable) {
        VerificationResult ruleResult = getFailureVerificationResult();
        try {
            KsiContainerSignature ksiContainerSignature = (KsiContainerSignature) verifiable.getSignature();
            KSISignature signature = ksiContainerSignature.getSignature();
            HashAlgorithm hashAlgorithm = signature.getInputHash().getAlgorithm();
            DataHash realHash = verifiable.getManifest().getRight().getDataHash(hashAlgorithm);
            com.guardtime.ksi.unisignature.verifier.VerificationResult ksiVerificationResult = ksi.verify(signature, policy, realHash);
            if (ksiVerificationResult.isOk()) {
                ruleResult = VerificationResult.OK;
            }
        } catch (ClassCastException | KSIException | IOException e) {
            LOGGER.debug("Verifying signature failed!", e);
        }
        String signatureUri = verifiable.getManifest().getRight().getSignatureReference().getUri();
        GenericVerificationResult verificationResult = new GenericVerificationResult(ruleResult, this, signatureUri);
        return Arrays.asList((RuleVerificationResult) verificationResult);
    }
}
