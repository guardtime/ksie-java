package com.guardtime.container.verification.result;

import com.guardtime.container.ContainerFileElement;

public interface RuleVerificationResult {

    RuleResult getResult();

    String getRuleName();

    boolean terminatesVerification();

    ContainerFileElement getTestedElement();
}
