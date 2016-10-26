package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.verification.result.GenericVerificationResult;
import com.guardtime.container.verification.result.ResultHolder;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.container.verification.rule.AbstractRule;
import com.guardtime.container.verification.rule.RuleTerminatingException;
import com.guardtime.container.verification.rule.state.RuleState;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;

/**
 * Rule that checks whether there are any {@link DataHash}es with {@link HashAlgorithm} that have state of
 * {@link HashAlgorithm#status#NOT_TRUSTED}
 */
public class TrustedHashAlgorithmExistenceRule extends AbstractRule<FileReference> {
    private final String ruleName;

    protected TrustedHashAlgorithmExistenceRule(RuleState state, String name) {
        super(state);
        this.ruleName = name;
    }

    @Override
    protected void verifyRule(ResultHolder holder, FileReference verifiable) throws RuleTerminatingException {
        VerificationResult verificationResult = getFailureVerificationResult();
        for (DataHash hash : verifiable.getHashList()) {
            if (hash.getAlgorithm().getStatus() == HashAlgorithm.Status.NORMAL) {
                verificationResult = VerificationResult.OK;
            }
        }
        holder.addResult(new GenericVerificationResult(verificationResult, this, verifiable.getUri()));
        if (verificationResult != VerificationResult.OK) {
            throw new RuleTerminatingException("No hashes with trusted hash algorithm found.");
        }
    }

    @Override
    public String getName() {
        return ruleName;
    }

    @Override
    public String getErrorMessage() {
        return "No trusted hash function";
    }
}
