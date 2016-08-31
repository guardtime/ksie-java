package com.guardtime.container.verification.rule.signature.ksi;

import com.guardtime.ksi.KSI;
import com.guardtime.ksi.exceptions.KSIException;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.publication.PublicationData;
import com.guardtime.ksi.unisignature.KSISignature;
import com.guardtime.ksi.unisignature.verifier.VerificationResult;
import com.guardtime.ksi.unisignature.verifier.policies.Policy;

public class KsiPublicationAndPolicyBasedSignatureVerifier extends KsiBasedSignatureVerifier {

    private final PublicationData publication;

    public KsiPublicationAndPolicyBasedSignatureVerifier(KSI ksi, Policy policy, PublicationData publicationData) {
        super(ksi, policy);
        this.publication = publicationData;
    }

    @Override
    protected VerificationResult getKsiVerificationResult(KSISignature signature, DataHash realHash) throws KSIException {
        return ksi.verify(signature, policy, realHash, publication);
    }
}
