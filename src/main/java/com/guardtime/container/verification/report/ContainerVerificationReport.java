package com.guardtime.container.verification.report;

import com.guardtime.container.verification.result.RawVerifierResult;
import com.guardtime.container.verification.result.RuleVerificationResult;

import java.util.List;

public interface ContainerVerificationReport {
    RawVerifierResult getResult();

    List<String> getProcessedRulesNames();

    List<RuleVerificationResult> getRuleVerficationResults();

    String getErrorMessage();
}
