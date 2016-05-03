package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.verification.context.VerificationContext;
import com.guardtime.container.verification.result.RuleVerificationResult;
import com.guardtime.container.verification.rule.RuleState;

import java.util.LinkedList;
import java.util.List;

/**
 * Type of {@link GenericRule} which verifies each {@link SignatureContent} separately from each other.
 */
public abstract class SignatureContentRule<O extends RuleVerificationResult> extends GenericRule<O> {
    public SignatureContentRule(String name) {
        super(name);
    }

    public SignatureContentRule(RuleState state, String name) {
        super(state, name);
    }

    public List<O> verify(VerificationContext context) {
        List<O> results = new LinkedList<>();
        for (SignatureContent content : context.getContainer().getSignatureContents()) {
            results.addAll(verifySignatureContent(content, context));
        }
        return results;
    }

    protected abstract List<O> verifySignatureContent(SignatureContent content, VerificationContext context);
}
