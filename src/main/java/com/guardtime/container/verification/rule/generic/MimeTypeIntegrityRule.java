package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.packaging.ContainerPackagingFactory;
import com.guardtime.container.packaging.MimeType;
import com.guardtime.container.util.Pair;
import com.guardtime.container.verification.context.VerificationContext;
import com.guardtime.container.verification.result.GenericVerificationResult;
import com.guardtime.container.verification.result.RuleResult;
import com.guardtime.container.verification.result.RuleVerificationResult;
import com.guardtime.container.verification.result.TerminatingVerificationResult;
import com.guardtime.container.verification.rule.RuleState;
import com.guardtime.ksi.util.Util;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class MimeTypeIntegrityRule extends GenericRule {
    private static final String KSIE_VERIFY_MIME_TYPE = "KSIE_VERIFY_MIME_TYPE";
    private final byte[] expectedContent;

    public MimeTypeIntegrityRule(ContainerPackagingFactory factory) {
        this(RuleState.FAIL, factory);
    }

    public MimeTypeIntegrityRule(RuleState state, ContainerPackagingFactory factory) {
        super(state, KSIE_VERIFY_MIME_TYPE);
        this.expectedContent = factory.getMimeTypeContent();
    }

    @Override
    public List<Pair<? extends Object, ? extends RuleVerificationResult>> verify(VerificationContext context) {
        MimeType mimetype = context.getContainer().getMimeType();
        Pair<? extends Object, ? extends RuleVerificationResult> result = Pair.of(mimetype, new TerminatingVerificationResult(getFailureResult(), this));
        try {
            byte[] realContent = Util.toByteArray(mimetype.getInputStream());
            if (Arrays.equals(expectedContent, realContent)) {
                result = Pair.of(mimetype, new GenericVerificationResult(RuleResult.OK, this));
            }
        } catch (IOException e) {
            // TODO: Log exception?
        }
        List<Pair<? extends Object, ? extends RuleVerificationResult>> returnable = new LinkedList<>();
        returnable.add(result);
        return returnable;
    }
}
