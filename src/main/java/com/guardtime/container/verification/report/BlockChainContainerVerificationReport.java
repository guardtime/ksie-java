package com.guardtime.container.verification.report;

import com.guardtime.container.verification.policy.rule.VerificationRule;
import com.guardtime.container.verification.result.VerifierResult;

import java.util.List;

public interface BlockChainContainerVerificationReport {
    VerifierResult getResult();

    List<VerificationRule> getUsedRules();

    String getErrorMessage();
}
