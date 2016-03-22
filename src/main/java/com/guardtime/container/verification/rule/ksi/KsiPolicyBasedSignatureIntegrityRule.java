package com.guardtime.container.verification.rule.ksi;

import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.signature.ContainerSignature;
import com.guardtime.container.verification.context.VerificationContext;
import com.guardtime.container.verification.result.GenericVerificationResult;
import com.guardtime.container.verification.result.RuleResult;
import com.guardtime.container.verification.result.RuleVerificationResult;
import com.guardtime.container.verification.rule.RuleState;
import com.guardtime.container.verification.rule.generic.SignatureContentRule;
import com.guardtime.ksi.KSI;
import com.guardtime.ksi.exceptions.KSIException;
import com.guardtime.ksi.unisignature.KSISignature;
import com.guardtime.ksi.unisignature.verifier.policies.Policy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class KsiPolicyBasedSignatureIntegrityRule extends SignatureContentRule {
    private final String name;
    private final KSI ksi;
    private Policy verificationPolicy;

    public KsiPolicyBasedSignatureIntegrityRule(KSI ksi, Policy policy) {
        this(ksi, policy, RuleState.FAIL);
    }

    public KsiPolicyBasedSignatureIntegrityRule(KSI ksi, Policy policy, RuleState state) {
        super(state);
        this.ksi = ksi;
        this.verificationPolicy = policy;
        this.name = "KSIE_VERIFY_MANIFEST_SIGNATURE";
    }

    @Override
    protected List<RuleVerificationResult> verifySignatureContent(SignatureContent content, VerificationContext context) {
        RuleResult ruleResult = getFailureResult();
        ContainerSignature contentSignature = content.getSignature();
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            contentSignature.writeTo(bos);
            KSISignature signature = ksi.read(bos.toByteArray());
            com.guardtime.ksi.unisignature.verifier.VerificationResult results = ksi.verify(signature, verificationPolicy);
            if (results.isOk()) {
                ruleResult = RuleResult.OK;
            }
        } catch (KSIException | IOException e) {
            // TODO: log exception ?
        }
        return Arrays.asList((RuleVerificationResult) new GenericVerificationResult(ruleResult, name, contentSignature));
    }
}
