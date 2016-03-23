package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.packaging.ContainerPackagingFactory;
import com.guardtime.container.packaging.MimeType;
import com.guardtime.container.verification.context.VerificationContext;
import com.guardtime.container.verification.result.GenericVerificationResult;
import com.guardtime.container.verification.result.RuleResult;
import com.guardtime.container.verification.result.RuleVerificationResult;
import com.guardtime.container.verification.result.TerminatingVerificationResult;
import com.guardtime.container.verification.rule.RuleState;
import com.guardtime.ksi.util.Util;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class MimeTypeIntegrityRule extends GenericRule {
    private static final String KSIE_VERIFY_MIME_TYPE = "KSIE_VERIFY_MIME_TYPE";
    private final byte[] expectedContent;

    public MimeTypeIntegrityRule(ContainerPackagingFactory factory) {
        this(RuleState.FAIL, factory);
    }

    public MimeTypeIntegrityRule(RuleState state, ContainerPackagingFactory factory) {
        super(state);
        this.expectedContent = factory.getMimeTypeContent();
    }

    @Override
    public List<RuleVerificationResult> verify(VerificationContext context) {
        MimeType mimetype = context.getContainer().getMimeType();
        RuleVerificationResult result = new TerminatingVerificationResult(getFailureResult(), KSIE_VERIFY_MIME_TYPE, mimetype);
        try {
            byte[] realContent = Util.toByteArray(mimetype.getInputStream());
            if (Arrays.equals(expectedContent, realContent)) {
                result = new GenericVerificationResult(RuleResult.OK, KSIE_VERIFY_MIME_TYPE, mimetype);
            }
        } catch (IOException e) {
            // TODO: Log exception?
        }
        return Arrays.asList(result);
    }
}
