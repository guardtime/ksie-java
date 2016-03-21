package com.guardtime.container.verification;

import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.verification.context.VerificationContext;
import com.guardtime.container.verification.policy.VerificationPolicy;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.container.verification.result.VerifierResult;
import com.guardtime.container.verification.rule.ContainerRule;
import com.guardtime.container.verification.rule.SignatureContentRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class BlockChainContainerVerifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(BlockChainContainerVerifier.class);

    private VerificationPolicy policy;

    public BlockChainContainerVerifier(VerificationPolicy policy) {
        this.policy = policy;
    }

    /**
     * Verifies the VerificationContext based on the rules provided by the VerificationPolicy. Appends results from
     * rules to the pre-existing list of results contained in the context
     *
     * @param context
     *         containing verifiable container and a list of results from performed rules
     * @return VerificationResult based on the updated VerificationContext
     */
    public VerifierResult verify(VerificationContext context) {
        try {
            verifyGeneralRules(context);
            List<? extends SignatureContent> signatureContents = context.getContainer().getSignatureContents();
            for (SignatureContent content : signatureContents) {
                verifySignatureContentRules(content, context);
            }
        } catch (VerificationTerminationException e) {
            LOGGER.info(e.getMessage());
        }
        return new VerifierResult(context);
    }

    private void verifyGeneralRules(VerificationContext context) throws VerificationTerminationException {
        List<VerificationResult> results = context.getResults();
        List<ContainerRule> generalRules = policy.getGeneralRules();
        for (ContainerRule rule : generalRules) {
            if (rule.shouldBeIgnored(results)) continue;
            List<VerificationResult> verificationResults = rule.verify(context);
            results.addAll(verificationResults);
            checkResults(verificationResults);
        }
    }

    private void verifySignatureContentRules(SignatureContent content, VerificationContext context) throws VerificationTerminationException {
        List<VerificationResult> results = context.getResults();
        List<SignatureContentRule> signatureContentRules = policy.getSignatureContentRules();
        for (SignatureContentRule rule : signatureContentRules) {
            if (rule.shouldBeIgnored(content, context)) continue;
            List<? extends VerificationResult> verificationResults = rule.verify(content, context);
            results.addAll(verificationResults);
            checkResults(verificationResults);
        }
    }

    private void checkResults(List<? extends VerificationResult> verificationResults) throws VerificationTerminationException {
        for (VerificationResult result : verificationResults) {
            if (result.terminatesVerification()) {
                throw new VerificationTerminationException("Rule " + result.getRuleName() + " resulted in verification termination!");
            }
        }
    }

}
