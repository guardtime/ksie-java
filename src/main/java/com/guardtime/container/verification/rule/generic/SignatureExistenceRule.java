package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.signature.ContainerSignature;
import com.guardtime.container.verification.result.GenericVerificationResult;
import com.guardtime.container.verification.result.ResultHolder;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.container.verification.rule.AbstractRule;
import com.guardtime.container.verification.rule.RuleTerminatingException;
import com.guardtime.container.verification.rule.RuleType;
import com.guardtime.container.verification.rule.state.RuleStateProvider;

/**
 * Rule that verifies that there is a signature in the container for the given {@link SignatureContent}
 * Will terminate verification upon non OK results.
 */
public class SignatureExistenceRule extends AbstractRule<SignatureContent> {

    private static final String NAME = RuleType.KSIE_VERIFY_SIGNATURE_EXISTS.getName();

    public SignatureExistenceRule(RuleStateProvider stateProvider) {
        super(stateProvider.getStateForRule(NAME));
    }

    @Override
    protected void verifyRule(ResultHolder holder, SignatureContent verifiable) throws RuleTerminatingException {
        String uri = verifiable.getManifest().getRight().getSignatureReference().getUri();
        ContainerSignature signature = verifiable.getContainerSignature();
        if (signature == null || signature.getSignature() == null) {
            holder.addResult(
                    verifiable,
                    new GenericVerificationResult(VerificationResult.NOK, getName(), getErrorMessage(), uri)
            );
            throw new RuleTerminatingException("No signature present!");
        } else {
            holder.addResult(
                    verifiable,
                    new GenericVerificationResult(VerificationResult.OK, getName(), getErrorMessage(), uri)
            );
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getErrorMessage() {
        return "No signature in container for manifest!";
    }
}
