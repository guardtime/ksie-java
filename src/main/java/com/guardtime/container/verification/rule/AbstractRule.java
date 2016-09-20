package com.guardtime.container.verification.rule;

import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.manifest.MultiHashElement;
import com.guardtime.container.util.DataHashException;
import com.guardtime.container.verification.result.GenericVerificationResult;
import com.guardtime.container.verification.result.ResultHolder;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

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

    @Override
    public void verify(ResultHolder holder, O verifiable) throws RuleTerminatingException {
        if (this.state != RuleState.IGNORE) verifyRule(holder, verifiable);
    }

    protected abstract void verifyRule(ResultHolder holder, O verifiable) throws RuleTerminatingException;

    protected void verifyMultiHashElement(MultiHashElement multiHashElement, FileReference fileReference, ResultHolder holder) throws RuleTerminatingException {
        String testedElement = fileReference.getUri();
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
            holder.addResult(new GenericVerificationResult(verificationResult, this, testedElement));
        } catch (IOException | DataHashException e) {
            LOGGER.info("Verifying hash failed!", e);
            holder.addResult(new GenericVerificationResult(verificationResult, this, testedElement, e));
        }

        if (!verificationResult.equals(VerificationResult.OK)) {
            throw new RuleTerminatingException("'" + testedElement + "' integrity could not be verified.");
        }
    }

    public String getName() {
        return null;
    }

    @Override
    public String getErrorMessage() {
        return null;
    }
}
