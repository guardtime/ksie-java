package com.guardtime.container.verification.result;

public interface VerificationResult {

    Object getTested(); // TODO: Rethink this as it may bee to ambiguous at the moment

    RuleResult getResult();

    String getRuleName();
}
