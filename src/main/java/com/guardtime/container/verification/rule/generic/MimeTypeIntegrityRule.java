package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.ContainerPackagingFactory;
import com.guardtime.container.packaging.MimeType;
import com.guardtime.container.verification.result.RuleVerificationResult;
import com.guardtime.container.verification.result.TerminatingVerificationResult;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.container.verification.rule.AbstractRule;
import com.guardtime.container.verification.rule.ContainerRule;
import com.guardtime.container.verification.rule.RuleState;
import com.guardtime.ksi.util.Util;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Rule that verifies the existence and content of a MIMETYPE file in the container. The expected content is given by
 * {@link ContainerPackagingFactory}.
 */
public class MimeTypeIntegrityRule extends AbstractRule<Container> implements ContainerRule {
    private final byte[] expectedContent;

    public MimeTypeIntegrityRule(ContainerPackagingFactory packagingFactory) {
        this(RuleState.FAIL, packagingFactory);
    }

    public MimeTypeIntegrityRule(RuleState state, ContainerPackagingFactory packagingFactory) {
        super(state);
        this.expectedContent = packagingFactory.getMimeTypeContent();
    }

    @Override
    protected List<RuleVerificationResult> verifyRule(Container verifiable) {
        RuleVerificationResult verificationResult;
        MimeType mimetype = verifiable.getMimeType();
        VerificationResult result = getFailureVerificationResult();
        try {
            byte[] realContent = Util.toByteArray(mimetype.getInputStream());
            if (Arrays.equals(expectedContent, realContent)) {
                result = VerificationResult.OK;
            }
            verificationResult = new TerminatingVerificationResult(result, this, mimetype.getUri());
        } catch (IOException e) {
            LOGGER.info("Verifying MIME type failed!", e);
            verificationResult = new TerminatingVerificationResult(result, this, mimetype.getUri(), e);
        }
        return Arrays.asList(verificationResult);
    }

    @Override
    public String getName() {
        return "KSIE_FORMAT";
    }

    @Override
    public String getErrorMessage() {
        return "Unsupported format.";
    }
}
