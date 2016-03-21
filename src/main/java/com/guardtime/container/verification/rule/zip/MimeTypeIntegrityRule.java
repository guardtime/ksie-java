package com.guardtime.container.verification.rule.zip;

import com.guardtime.container.packaging.MimeType;
import com.guardtime.container.verification.context.VerificationContext;
import com.guardtime.container.verification.result.GenericVerificationResult;
import com.guardtime.container.verification.result.RuleResult;
import com.guardtime.container.verification.result.TerminatingVerificationResult;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.container.verification.rule.ContainerRule;
import com.guardtime.container.verification.rule.RuleState;
import com.guardtime.ksi.util.Util;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class MimeTypeIntegrityRule implements ContainerRule {
    private final RuleState state;
    private byte[] expectedContent = "application/guardtime.ksie10+zip".getBytes(); // TODO: Set the expected content

    public MimeTypeIntegrityRule() {
        this(RuleState.FAIL);
    }

    public MimeTypeIntegrityRule(RuleState state) {
        this.state = state;
    }

    @Override
    public List<VerificationResult> verify(VerificationContext context) {
        MimeType mimetype = context.getContainer().getMimeType();
        VerificationResult result = new TerminatingVerificationResult(RuleResult.OK, this, mimetype);
        try {
            byte[] realContent = Util.toByteArray(mimetype.getInputStream());
            if (Arrays.equals(expectedContent, realContent)) {
                result = new GenericVerificationResult(RuleResult.OK, this, mimetype);
            }
        } catch (IOException e) {
            // TODO: Log exception?
        }
        return Arrays.asList(result);
    }

    @Override
    public boolean shouldBeIgnored(List<VerificationResult> previousResults) {
        return state == RuleState.IGNORE;
    }

    @Override
    public RuleState getState() {
        return state;
    }

    @Override
    public String getName() {
        return "KSIE_VERIFY_MIME_TYPE";
    }

    private RuleResult getFailureResult() {
        return getState() == RuleState.WARN ? RuleResult.WARN : RuleResult.NOK;
    }
}
