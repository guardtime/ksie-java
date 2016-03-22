package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.verification.context.VerificationContext;
import com.guardtime.container.verification.result.RuleVerificationResult;
import com.guardtime.container.verification.rule.RuleState;

import java.util.LinkedList;
import java.util.List;

public abstract class SignatureContentRule extends GenericRule {

    public SignatureContentRule(RuleState state) {
        super(state);
    }

    public List<RuleVerificationResult> verify(VerificationContext context) {
        List<RuleVerificationResult> results = new LinkedList<>();
        for (SignatureContent content : context.getContainer().getSignatureContents()) {
            results.addAll(verifySignatureContent(content, context));
        }
        return results;
    }

    protected abstract List<RuleVerificationResult> verifySignatureContent(SignatureContent content, VerificationContext context);
}
