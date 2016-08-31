package com.guardtime.container.verification.rule.signature;

import com.guardtime.container.manifest.Manifest;
import com.guardtime.container.signature.ContainerSignature;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.container.verification.rule.RuleTerminatingException;
import com.guardtime.ksi.KSI;
import com.guardtime.ksi.exceptions.KSIException;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;
import com.guardtime.ksi.unisignature.KSISignature;
import com.guardtime.ksi.unisignature.verifier.policies.Policy;

import java.io.IOException;

public class KsiPolicyBasedSignatureVerifier implements SignatureVerifier<KSISignature> {

    protected final KSI ksi;
    protected final Policy policy;

    public KsiPolicyBasedSignatureVerifier(KSI ksi, Policy policy) {
        this.ksi = ksi;
        this.policy = policy;
    }

    @Override
    public Boolean isSupported(ContainerSignature containerSignature) {
        return containerSignature.getSignature() instanceof KSISignature;
    }

    @Override
    public VerificationResult getSignatureVerificationResult(KSISignature signature, Manifest manifest) throws RuleTerminatingException {
        VerificationResult ruleResult = null;
        try {
            HashAlgorithm hashAlgorithm = signature.getInputHash().getAlgorithm();
            DataHash realHash = manifest.getDataHash(hashAlgorithm);
            com.guardtime.ksi.unisignature.verifier.VerificationResult ksiVerificationResult = getKsiVerificationResult(signature, realHash);
            if (ksiVerificationResult.isOk()) {
                ruleResult = VerificationResult.OK;
            }
        } catch (KSIException | IOException e) {
            throw new RuleTerminatingException("Failed to verify KSI signature.", e);
        }
        return ruleResult;
    }

    protected com.guardtime.ksi.unisignature.verifier.VerificationResult getKsiVerificationResult(KSISignature signature, DataHash realHash) throws KSIException {
        return ksi.verify(signature, policy, realHash);
    }
}
