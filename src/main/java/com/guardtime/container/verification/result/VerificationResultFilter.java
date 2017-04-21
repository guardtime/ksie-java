package com.guardtime.container.verification.result;

public interface VerificationResultFilter {

    boolean apply(RuleVerificationResult result);

}
