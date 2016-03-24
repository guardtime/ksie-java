package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.util.Pair;
import com.guardtime.container.verification.context.VerificationContext;
import com.guardtime.container.verification.result.RuleVerificationResult;
import com.guardtime.container.verification.rule.RuleState;

import java.util.LinkedList;
import java.util.List;

public abstract class SignatureContentRule extends GenericRule {
    public SignatureContentRule(String name) {
        super(name);
    }

    public SignatureContentRule(RuleState state, String name) {
        super(state, name);
    }

    public List<Pair<? extends Object, ? extends RuleVerificationResult>> verify(VerificationContext context) {
        List<Pair<? extends Object, ? extends RuleVerificationResult>> results = new LinkedList<>();
        for (SignatureContent content : context.getContainer().getSignatureContents()) {
            results.addAll(verifySignatureContent(content, context));
        }
        return results;
    }

    protected abstract List<Pair<? extends Object, ? extends RuleVerificationResult>> verifySignatureContent(SignatureContent content, VerificationContext context);
}
