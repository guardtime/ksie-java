package com.guardtime.container.verification.rule;

import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.manifest.MultiHashElement;
import com.guardtime.container.util.DataHashException;
import com.guardtime.container.verification.result.RuleVerificationResult;
import com.guardtime.container.verification.result.TerminatingVerificationResult;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public abstract class AbstractRule<O> implements Rule<O> {
    protected static final Logger LOGGER = LoggerFactory.getLogger(Rule.class);

    protected final RuleState state;

    protected AbstractRule(RuleState state) {
        this.state = state;
    }

    protected VerificationResult getFailureVerificationResult() {
        switch (state) {
            case WARN:
                return VerificationResult.WARN;
            case IGNORE:
                return VerificationResult.OK;
            default:
                return VerificationResult.NOK;
        }
    }

    /**
     * Returns true if any of the {@link RuleVerificationResult}s are not OK
     */
    protected boolean mustTerminateVerification(List<RuleVerificationResult> verificationResults) {
        if (verificationResults.isEmpty()) return true;
        for (RuleVerificationResult result : verificationResults) {
            if (result.terminatesVerification() && !VerificationResult.OK.equals(result.getVerificationResult())) {
                return true;
            }
        }
        return false;
    }

    protected boolean ignoreRule() {
        return this.state == RuleState.IGNORE;
    }

    public List<RuleVerificationResult> verify(O verifiable) {
        if (ignoreRule()) return new LinkedList<>();
        return verifyRule(verifiable);
    }

    protected abstract List<RuleVerificationResult> verifyRule(O verifiable);

    protected List<RuleVerificationResult> getFileReferenceHashListVerificationResult(MultiHashElement multiHashElement, FileReference fileReference) {
        String testedElement = fileReference.getUri();
        RuleVerificationResult result;
        VerificationResult verificationResult = getFailureVerificationResult();
        try {
            for (DataHash expectedHash : fileReference.getHashList()) {
                if (expectedHash.getAlgorithm().getStatus() == HashAlgorithm.Status.NOT_IMPLEMENTED) {
                    LOGGER.warn("Will not perform hash verification for '{}' because algorithm is not implemented", expectedHash);
                    continue; // Skip not implemented
                }
                if (expectedHash.getAlgorithm().getStatus() == HashAlgorithm.Status.NOT_TRUSTED) {
                    LOGGER.warn("Hash with untrusted algorithm used for verification");
                }
                DataHash realHash = multiHashElement.getDataHash(expectedHash.getAlgorithm());
                if (expectedHash.equals(realHash)) {
                    verificationResult = VerificationResult.OK;
                    LOGGER.info("Generated hash matches hash in reference. Hash: '{}'", realHash);
                } else {
                    LOGGER.warn("Generated hash does not match hash in reference. Expecting '{}', got '{}'", expectedHash, realHash);
                }
            }
            result = new TerminatingVerificationResult(verificationResult, this, testedElement);
        } catch (IOException | DataHashException e) {
            LOGGER.info("Verifying hash failed!", e);
            result = new TerminatingVerificationResult(verificationResult, this, testedElement, e);
        }
        return Arrays.asList(result);
    }


    public String getName() {
        return null;
    }

    public String getErrorMessage() {
        return null;
    }
}
