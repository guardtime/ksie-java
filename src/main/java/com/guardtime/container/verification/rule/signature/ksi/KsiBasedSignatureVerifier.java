package com.guardtime.container.verification.rule.signature.ksi;

import com.guardtime.container.manifest.Manifest;
import com.guardtime.container.signature.ContainerSignature;
import com.guardtime.container.util.Util;
import com.guardtime.container.verification.result.SignatureResult;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.container.verification.rule.RuleTerminatingException;
import com.guardtime.container.verification.rule.signature.SignatureVerifier;
import com.guardtime.ksi.KSI;
import com.guardtime.ksi.exceptions.KSIException;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;
import com.guardtime.ksi.publication.PublicationData;
import com.guardtime.ksi.unisignature.KSISignature;
import com.guardtime.ksi.unisignature.verifier.policies.Policy;

import java.io.IOException;

public class KsiBasedSignatureVerifier implements SignatureVerifier<KSISignature> {

    protected final KSI ksi;
    protected final Policy policy;
    private PublicationData publication;

    public KsiBasedSignatureVerifier(KSI ksi, Policy policy) {
        this(ksi, policy, null);
    }

    public KsiBasedSignatureVerifier(KSI ksi, Policy policy, PublicationData publicationData) {
        Util.notNull(ksi, "KSI instance");
        this.ksi = ksi;
        this.policy = policy;
        this.publication = publicationData;
    }

    @Override
    public Boolean isSupported(ContainerSignature containerSignature) {
        return containerSignature.getSignature() instanceof KSISignature;
    }

    @Override
    public SignatureResult getSignatureVerificationResult(KSISignature signature, Manifest manifest) throws RuleTerminatingException {
        VerificationResult ruleResult = null;
        try {
            HashAlgorithm hashAlgorithm = signature.getInputHash().getAlgorithm();
            DataHash realHash = manifest.getDataHash(hashAlgorithm);
            com.guardtime.ksi.unisignature.verifier.VerificationResult ksiVerificationResult = ksi.verify(signature, policy, realHash, publication);
            if (ksiVerificationResult.isOk()) {
                ruleResult = VerificationResult.OK;
            }
            return new KsiSignatureResult(ksiVerificationResult, ruleResult, signature);
        } catch (KSIException | IOException e) {
            throw new RuleTerminatingException("Failed to verify KSI signature.", e);
        }
    }

}
