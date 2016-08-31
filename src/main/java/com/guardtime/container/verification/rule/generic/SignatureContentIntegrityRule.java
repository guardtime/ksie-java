package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.verification.result.ResultHolder;
import com.guardtime.container.verification.rule.*;

/**
 * This is a delegating rule, not verifying directly but by calling relevant rules to verify sub-components. This rule
 * verifies each {@link SignatureContent} in a {@link Container} by calling rules to verify the signature, the documents
 * and the annotations contained by the {@link SignatureContent}.
 */
public class SignatureContentIntegrityRule extends AbstractRule<Container> implements ContainerRule {
    private final Rule signatureRule;
    private DocumentsIntegrityRule documentsIntegrityRule;
    private AnnotationsIntegrityRule annotationsIntegrityRule;

    public SignatureContentIntegrityRule(RuleState state, Rule signatureRule) {
        super(state);
        this.signatureRule = signatureRule;
        documentsIntegrityRule = new DocumentsIntegrityRule(state);
        annotationsIntegrityRule = new AnnotationsIntegrityRule(state);
    }

    @Override
    protected void verifyRule(ResultHolder holder, Container verifiable) throws RuleTerminatingException {
        for (SignatureContent content : verifiable.getSignatureContents()) {
            signatureRule.verify(holder, content);
            documentsIntegrityRule.verify(holder, content);
            annotationsIntegrityRule.verify(holder, content);
        }
    }

}
