package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.verification.result.RuleVerificationResult;
import com.guardtime.container.verification.rule.Rule;
import com.guardtime.container.verification.rule.RuleState;

import java.util.LinkedList;
import java.util.List;

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
    public List<RuleVerificationResult> verify(Container verifiable) {
        List<RuleVerificationResult> results = new LinkedList<>();
        for(SignatureContent content : verifiable.getSignatureContents()){
            results.addAll(signatureRule.verify(content));
            results.addAll(new DocumentsIntegrityRule(state).verify(content));
            results.addAll(new AnnotationsIntegrityRule(state).verify(content));
        }
        return results;
    }

}
