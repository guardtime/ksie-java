package com.guardtime.container.verification;

import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.util.Util;
import com.guardtime.container.verification.policy.VerificationPolicy;
import com.guardtime.container.verification.result.ResultHolder;
import com.guardtime.container.verification.result.RuleVerificationResult;
import com.guardtime.container.verification.rule.Rule;
import com.guardtime.container.verification.rule.RuleTerminatingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Helper class to verify {@link Container} based on a {@link VerificationPolicy}
 */
public class ContainerVerifier {
    private static final Logger logger = LoggerFactory.getLogger(ContainerVerifier.class);

    private VerificationPolicy policy;

    public ContainerVerifier(VerificationPolicy policy) {
        Util.notNull(policy, "Verification policy");
        this.policy = policy;
    }

    /**
     * Verifies the {@link Container} based on the rules provided by the {@link VerificationPolicy}.
     * @param container  container to be verified
     * @return {@link VerifiedContainer} based on all {@link RuleVerificationResult} gathered during verification.
     */
    public VerifiedContainer verify(Container container) {
        ResultHolder holder = new ResultHolder();
        try {
            verifyGeneralRules(container, holder);
            verifySignatureContents(container.getSignatureContents(), holder);
        } catch (RuleTerminatingException e) {
            logger.info("Container verification terminated! Reason: '{}'", e.getMessage());
        }
        return new VerifiedContainer(container, holder);
    }

    private void verifyGeneralRules(Container container, ResultHolder holder) throws RuleTerminatingException {
        for (Rule<Container> rule : policy.getContainerRules()) {
            rule.verify(holder, container);
        }
    }

    private void verifySignatureContents(List<? extends SignatureContent> signatureContents, ResultHolder holder) {
        for (SignatureContent content : signatureContents) {
            try {
                for (Rule<SignatureContent> rule : policy.getSignatureContentRules()) {
                    rule.verify(holder, content);
                }
            } catch (RuleTerminatingException e) {
                logger.info("Signature content verification terminated! Reason: '{}'", e.getMessage());
            }
        }
    }

}
