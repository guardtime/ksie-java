package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.verification.result.RuleVerificationResult;
import com.guardtime.container.verification.rule.Rule;
import com.guardtime.container.verification.rule.RuleState;

import java.util.LinkedList;
import java.util.List;

/**
 * This is a delegating rule, not verifying directly but by calling relevant rules to verify sub-components. This rule
 * verifies each {@link SignatureContent} in a {@link Container} by calling rules to verify the signature, the documents
 * and the annotations contained by the {@link SignatureContent}.
 */
public class SignatureContentIntegrityRule extends AbstractContainerRule {
    private final Rule signatureRule;

    public SignatureContentIntegrityRule(Rule signatureRule) {
        this(signatureRule, RuleState.FAIL);
    }

    public SignatureContentIntegrityRule(Rule signatureRule, RuleState state) {
        super(state);
        this.signatureRule = signatureRule;
    }

    @Override
    protected List<RuleVerificationResult> verifyRule(Container verifiable) {
        List<RuleVerificationResult> results = new LinkedList<>();
        for (SignatureContent content : verifiable.getSignatureContents()) {
            results.addAll(signatureRule.verify(content));
            results.addAll(new DocumentsIntegrityRule(state).verify(content));
            results.addAll(new AnnotationsIntegrityRule(state).verify(content));
        }
        return results;
    }

}
