package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.manifest.MultiHashElement;
import com.guardtime.container.util.Pair;
import com.guardtime.container.verification.result.ResultHolder;
import com.guardtime.container.verification.rule.AbstractRule;
import com.guardtime.container.verification.rule.RuleTerminatingException;
import com.guardtime.container.verification.rule.state.RuleState;

public class MultiHashElementIntegrityRule extends AbstractRule<Pair<MultiHashElement, FileReference>> {

    private final UnimplementedHashAlgorithmExistenceRule unimplementedHashAlgorithmExistenceRule;
    private final TrustedHashAlgorithmExistenceRule trustedHashAlgorithmExistenceRule;
    private final TrustedHashListIntegrityRule trustedHashesIntegrityRule;

    protected MultiHashElementIntegrityRule(RuleState state, String name) {
        super(state);

        unimplementedHashAlgorithmExistenceRule = new UnimplementedHashAlgorithmExistenceRule(state, name);
        trustedHashAlgorithmExistenceRule = new TrustedHashAlgorithmExistenceRule(state, name);
        trustedHashesIntegrityRule = new TrustedHashListIntegrityRule(state, name);
    }

    @Override
    protected void verifyRule(ResultHolder holder, Pair<MultiHashElement, FileReference> verifiable) throws RuleTerminatingException {
        unimplementedHashAlgorithmExistenceRule.verify(holder, verifiable.getRight());
        trustedHashAlgorithmExistenceRule.verify(holder, verifiable.getRight());
        trustedHashesIntegrityRule.verify(holder, verifiable);
    }
}
