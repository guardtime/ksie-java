package com.guardtime.container.verification.rule.ksi;

import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.signature.ContainerSignature;
import com.guardtime.container.verification.context.VerificationContext;
import com.guardtime.container.verification.result.GenericVerificationResult;
import com.guardtime.container.verification.result.RuleResult;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.container.verification.rule.RuleState;
import com.guardtime.container.verification.rule.SignatureContentRule;
import com.guardtime.ksi.KSI;
import com.guardtime.ksi.exceptions.KSIException;
import com.guardtime.ksi.unisignature.KSISignature;
import com.guardtime.ksi.unisignature.verifier.policies.CalendarBasedVerificationPolicy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class CalendarBasedSignatureIntegrityRule implements SignatureContentRule {
    private final RuleState state;
    private final String name;
    private final KSI ksi;

    public CalendarBasedSignatureIntegrityRule(KSI ksi) {
        this(ksi, RuleState.FAIL);
    }

    public CalendarBasedSignatureIntegrityRule(KSI ksi, RuleState state) {
        this.ksi = ksi;
        this.state = state;
        this.name = "KSIE_VERIFY_MANIFEST_SIGNATURE";
    }

    @Override
    public List<? extends VerificationResult> verify(SignatureContent content, VerificationContext context) {
        RuleResult ruleResult = getFailureResult();
        ContainerSignature contentSignature = content.getSignature();
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            contentSignature.writeTo(bos);
            KSISignature signature = ksi.read(bos.toByteArray());
            com.guardtime.ksi.unisignature.verifier.VerificationResult results = ksi.verify(signature, new CalendarBasedVerificationPolicy());
            if (results.isOk()) {
                ruleResult = RuleResult.OK;
            }
        } catch (KSIException | IOException e) {
            // TODO: log exception ?
        }
        return Arrays.asList((VerificationResult) new GenericVerificationResult(ruleResult, name, contentSignature));
    }

    @Override
    public boolean shouldBeIgnored(SignatureContent content, VerificationContext context) {
        return state == RuleState.IGNORE;
    }

    private RuleResult getFailureResult() {
        return state == RuleState.WARN ? RuleResult.WARN : RuleResult.NOK;
    }
}
