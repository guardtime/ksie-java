package com.guardtime.container.verification.rule.signature.ksi;

import com.guardtime.ksi.KSI;
import com.guardtime.ksi.exceptions.KSIException;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.unisignature.KSISignature;
import com.guardtime.ksi.unisignature.verifier.policies.Policy;

public class KsiPolicyBasedSignatureVerifier extends KsiBasedSignatureVerifier {

    public KsiPolicyBasedSignatureVerifier(KSI ksi, Policy policy) {
        super(ksi, policy);
    }

    protected com.guardtime.ksi.unisignature.verifier.VerificationResult getKsiVerificationResult(KSISignature signature, DataHash realHash) throws KSIException {
        return ksi.verify(signature, policy, realHash);
    }
}
