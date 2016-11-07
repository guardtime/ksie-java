package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.manifest.MultiHashElement;
import com.guardtime.container.util.DataHashException;
import com.guardtime.container.util.Pair;
import com.guardtime.container.verification.result.GenericVerificationResult;
import com.guardtime.container.verification.result.ResultHolder;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.container.verification.rule.AbstractRule;
import com.guardtime.container.verification.rule.RuleTerminatingException;
import com.guardtime.container.verification.rule.state.RuleState;
import com.guardtime.ksi.hashing.DataHash;

import java.io.IOException;
import java.util.List;

/**
 * Rule that checks whether {@link DataHash}es in {@link FileReference} match those in {@link MultiHashElement}.
 */
public class TrustedHashListIntegrityRule extends AbstractRule<Pair<MultiHashElement, FileReference>> {
    private final String ruleName;

    protected TrustedHashListIntegrityRule(RuleState state, String name) {
        super(state);
        this.ruleName = name;
    }

    @Override
    protected void verifyRule(ResultHolder holder, Pair<MultiHashElement, FileReference> verifiable) throws RuleTerminatingException {
        FileReference reference = verifiable.getRight();
        VerificationResult verificationResult = getVerificationResult(reference.getHashList(), verifiable.getLeft());
        holder.addResult(new GenericVerificationResult(verificationResult, this, reference.getUri()));

        if (verificationResult != VerificationResult.OK) {
            throw new RuleTerminatingException("Hash mismatch found.");
        }
    }

    private VerificationResult getVerificationResult(List<DataHash> hashList, MultiHashElement multiHashElement) {
        VerificationResult failureVerificationResult = getFailureVerificationResult();
        try {
            for (DataHash hash : hashList) {
                DataHash realHash = multiHashElement.getDataHash(hash.getAlgorithm());
                if (!realHash.equals(hash)) {
                    return failureVerificationResult;
                }
            }
        } catch (IOException | DataHashException e) {
            LOGGER.info("Failed to verify hash match.", e);
            return failureVerificationResult;
        }
        return VerificationResult.OK;
    }

    @Override
    public String getName() {
        return ruleName;
    }

    @Override
    public String getErrorMessage() {
        return "Hash mismatch";
    }
}
