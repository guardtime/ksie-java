package com.guardtime.container.verification.rule.generic.ksi;

import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.signature.ContainerSignature;
import com.guardtime.container.signature.ksi.KsiContainerSignature;
import com.guardtime.container.verification.context.VerificationContext;
import com.guardtime.container.verification.result.GenericVerificationResult;
import com.guardtime.container.verification.result.RuleResult;
import com.guardtime.container.verification.rule.RuleState;
import com.guardtime.container.verification.rule.generic.SignatureContentRule;
import com.guardtime.ksi.KSI;
import com.guardtime.ksi.exceptions.KSIException;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;
import com.guardtime.ksi.unisignature.KSISignature;
import com.guardtime.ksi.unisignature.verifier.VerificationResult;
import com.guardtime.ksi.unisignature.verifier.policies.Policy;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Rule that verifies the {@link ContainerSignature} of a {@link SignatureContent} by using KSI {@link Policy} to verify
 * the underlying signature.<br>Does not assume the underlying signature to be of type {@link KSISignature} but will
 * produce failure result for any other type of underlying signature.
 */
public class KsiPolicyBasedSignatureIntegrityRule extends SignatureContentRule<GenericVerificationResult> {
    private static final String KSIE_VERIFY_MANIFEST_SIGNATURE = "KSIE_VERIFY_MANIFEST_SIGNATURE";
    private final KSI ksi;
    private Policy verificationPolicy;

    public KsiPolicyBasedSignatureIntegrityRule(KSI ksi, Policy policy) {
        this(ksi, policy, RuleState.FAIL);
    }

    public KsiPolicyBasedSignatureIntegrityRule(KSI ksi, Policy policy, RuleState state) {
        super(state, KSIE_VERIFY_MANIFEST_SIGNATURE);
        this.ksi = ksi;
        this.verificationPolicy = policy;
    }

    @Override
    protected List<GenericVerificationResult> verifySignatureContent(SignatureContent content, VerificationContext context) {
        RuleResult ruleResult = getFailureResult();
        try {
            KsiContainerSignature ksiContainerSignature = (KsiContainerSignature) content.getContainerSignature();
            KSISignature signature = ksiContainerSignature.getSignature();
            HashAlgorithm hashAlgorithm = signature.getInputHash().getAlgorithm();
            DataHash actualHash = content.getManifest().getRight().getDataHash(hashAlgorithm);
            VerificationResult results = ksi.verify(signature, verificationPolicy, actualHash);
            if (results.isOk()) {
                ruleResult = RuleResult.OK;
            }
        } catch (ClassCastException | KSIException | IOException e) {
            LOGGER.debug("Verifying signature failed!", e);
        }
        return Arrays.asList(new GenericVerificationResult(ruleResult, this, content.getContainerSignature()));
    }
}
