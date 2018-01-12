package com.guardtime.envelope.verification.policy;

import com.guardtime.envelope.packaging.Envelope;
import com.guardtime.envelope.packaging.SignatureContent;
import com.guardtime.envelope.verification.rule.Rule;

import java.util.ArrayList;
import java.util.List;

abstract class AbstractVerificationPolicy implements VerificationPolicy {
    protected ArrayList<Rule<SignatureContent>> signatureContentRules = new ArrayList<>();
    protected ArrayList<Rule<Envelope>> envelopeRules = new ArrayList<>();

    public List<Rule<SignatureContent>> getSignatureContentRules() {
        return signatureContentRules;
    }

    public List<Rule<Envelope>> getEnvelopeRules() {
        return envelopeRules;
    }
}
