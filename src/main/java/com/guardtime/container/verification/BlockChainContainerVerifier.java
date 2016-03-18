package com.guardtime.container.verification;

import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.verification.context.VerificationContext;
import com.guardtime.container.verification.policy.VerificationPolicy;
import com.guardtime.container.verification.policy.rule.VerificationRule;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.container.verification.result.VerifierResult;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class BlockChainContainerVerifier {
    private VerificationPolicy policy;

    public BlockChainContainerVerifier(VerificationPolicy policy) {
        this.policy = policy;
    }

    /**
     * Verifies the VerificationContext based on the rules provided by the VerificationPolicy.
     * Appends results from rules to the pre-existing list of results contained in the context
     * @param context containing verifiable container and a list of results from performed rules
     * @return VerificationResult based on the updated VerificationContext
     */
//    public VerifierResult verify(VerificationContext context) {
//        List<VerificationResult> results = context.getResults();
//        for (VerificationRule rule : policy.getRules()) {
//            if (rule.shouldBeIgnored(results)) continue;
//            results.addAll(rule.verify(context));
//        }
//        return new VerifierResult(context);
//    }


    public VerifierResult verify(VerificationContext context) {
        List<VerificationResult> results = context.getResults();
        results.addAll(verifyGeneralRules(context));
        List<? extends SignatureContent> signatureContents = context.getContainer().getSignatureContents();
        for(SignatureContent content : signatureContents) {
            results.addAll(verifySignatureContentRules(content, context));
        }
        return new VerifierResult(context);
    }

    private List<VerificationResult> verifySignatureContentRules(SignatureContent content, VerificationContext context) {
        List<VerificationResult> results = new LinkedList<>();
        for (SignatureContentRule rule : policy.getSignatureContentRules()) {
            if (rule.shouldBeIgnored(content, context)) continue;
            results.addAll(rule.verify(content, context));
        }

        return results;
    }

    private List<VerificationResult> verifyGeneralRules(VerificationContext context) {
        List<VerificationResult> results = new LinkedList<>();
        for (VerificationRule rule : policy.getGeneralRules()) {
            if (rule.shouldBeIgnored(results)) continue;
            results.addAll(rule.verify(context));
        }
        return results;
    }

}
