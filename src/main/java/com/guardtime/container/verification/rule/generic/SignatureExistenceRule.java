package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.signature.ContainerSignature;
import com.guardtime.container.verification.result.RuleVerificationResult;
import com.guardtime.container.verification.result.TerminatingVerificationResult;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.container.verification.rule.AbstractRule;
import com.guardtime.container.verification.rule.RuleState;

import java.util.Arrays;
import java.util.List;

/**
 * Rule that verifies that there is a signature in the container for the given {@link SignatureContent}
 */
public class SignatureExistenceRule extends AbstractRule<SignatureContent> {

    public SignatureExistenceRule(RuleState state) {
        super(state);
    }

    @Override
    protected List<RuleVerificationResult> verifyRule(SignatureContent verifiable) {
        RuleVerificationResult verificationResult;
        String uri = verifiable.getManifest().getRight().getSignatureReference().getUri();
        ContainerSignature signature = verifiable.getContainerSignature();
        if(signature == null || signature.getSignature() == null) {
            verificationResult = new TerminatingVerificationResult(VerificationResult.NOK, this, uri);
        } else {
            verificationResult = new TerminatingVerificationResult(VerificationResult.OK, this, uri);
        }
        return Arrays.asList(verificationResult);
    }

    @Override
    public String getName() {
        return "KSIE_VERIFY_SIGNATURE_EXISTS";
    }

    @Override
    public String getErrorMessage() {
        return "No signature in container for manifest!";
    }
}
