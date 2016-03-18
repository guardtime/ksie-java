package com.guardtime.container.verification.report;

import com.guardtime.container.verification.policy.rule.Rule;
import com.guardtime.container.verification.result.VerifierResult;

import java.util.List;

public interface BlockChainContainerVerificationReport {
    VerifierResult getResult();

    List<Rule> getUsedRules();

    String getErrorMessage();
}
