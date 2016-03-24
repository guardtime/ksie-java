package com.guardtime.container.verification.result;

public interface RuleVerificationResult {

    RuleResult getResult();

    String getRuleName();

    boolean terminatesVerification();
}
