package com.guardtime.container.verification.report;

import com.guardtime.container.verification.result.VerifierResult;

import java.util.List;

public interface ContainerVerificationReport {
    VerifierResult getResult();

    List<String> getUsedRules();

    String getErrorMessage();
}
