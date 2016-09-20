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

public class UnimplementedHashAlgorithmExistenceRule extends AbstractRule<FileReference> {

    private final String ruleName;

    protected UnimplementedHashAlgorithmExistenceRule(RuleState state, String name) {
        super(state);
        this.ruleName = name;
    }

    @Override
    protected void verifyRule(ResultHolder holder, FileReference verifiable) throws RuleTerminatingException {
        VerificationResult verificationResult = VerificationResult.OK;
        for (DataHash hash : verifiable.getHashList()) {
            if (hash.getAlgorithm().getStatus() == HashAlgorithm.Status.NOT_IMPLEMENTED) {
                verificationResult = getFailureVerificationResult();
                break;
            }
        }

        holder.addResult(new GenericVerificationResult(verificationResult, this, verifiable.getUri()));
        if (verificationResult != VerificationResult.OK) {
            throw new RuleTerminatingException("Found a hash with not implemented hash algorithm.");
        }
    }

    @Override
    public String getName() {
        return ruleName;
    }

    @Override
    public String getErrorMessage() {
        return "One of the hash functions not implemented";
    }
}
