package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.manifest.Manifest;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.signature.ContainerSignature;
import com.guardtime.container.verification.result.GenericVerificationResult;
import com.guardtime.container.verification.result.ResultHolder;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.container.verification.rule.AbstractRule;
import com.guardtime.container.verification.rule.RuleTerminatingException;
import com.guardtime.container.verification.rule.RuleType;
import com.guardtime.container.verification.rule.signature.SignatureVerifier;
import com.guardtime.container.verification.rule.state.RuleStateProvider;

/**
 * Rule that verifies the {@link ContainerSignature} of a {@link SignatureContent} by using a {@link SignatureVerifier}
 * to verify the underlying signature.
 */
public class ContainerSignatureIntegrityRule extends AbstractRule<SignatureContent> {
    private static final String NAME = RuleType.KSIE_VERIFY_MANIFEST.name();
    private final SignatureVerifier verifier;

    public ContainerSignatureIntegrityRule(RuleStateProvider stateProvider, SignatureVerifier verifier) {
        super(stateProvider.getStateForRule(NAME));
        this.verifier = verifier;
    }

    @Override
    protected void verifyRule(ResultHolder holder, SignatureContent verifiable) {
        Manifest manifest = verifiable.getManifest().getRight();
        String signatureUri = manifest.getSignatureReference().getUri();
        ContainerSignature containerSignature = verifiable.getContainerSignature();
        if (verifier.isSupported(containerSignature)) {
            try {
                VerificationResult result = verifier.getSignatureVerificationResult(containerSignature.getSignature(), manifest);
                if (result == null) {
                    result = getFailureVerificationResult();
                }
                holder.addResult(new GenericVerificationResult(result, this, signatureUri));
            } catch (RuleTerminatingException e) {
                LOGGER.info("Verifying signature failed!", e);
                holder.addResult(new GenericVerificationResult(getFailureVerificationResult(), this, signatureUri, e));
            }
        } else {
            holder.addResult(new GenericVerificationResult(getFailureVerificationResult(), this, signatureUri, new RuleTerminatingException("Unsupported signature type!")));
        }
    }

    public String getName() {
        return NAME;
    }

    public String getErrorMessage() {
        return "Signature mismatch.";
    }
}
