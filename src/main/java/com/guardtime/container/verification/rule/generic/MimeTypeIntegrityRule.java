package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.ContainerPackagingFactory;
import com.guardtime.container.packaging.MimeType;
import com.guardtime.container.verification.result.RuleVerificationResult;
import com.guardtime.container.verification.result.TerminatingVerificationResult;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.container.verification.rule.RuleState;
import com.guardtime.ksi.util.Util;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Rule that verifies the existence and content of a MIMETYPE file in the container. The expected content is given by
 * {@link ContainerPackagingFactory}.
 */
public class MimeTypeIntegrityRule extends AbstractContainerRule {
    private final byte[] expectedContent;

    public MimeTypeIntegrityRule(ContainerPackagingFactory packagingFactory) {
        this(RuleState.FAIL, packagingFactory);
    }

    public MimeTypeIntegrityRule(RuleState state, ContainerPackagingFactory packagingFactory) {
        super(state);
        this.expectedContent = packagingFactory.getMimeTypeContent();
    }

    @Override
    public List<RuleVerificationResult> verify(Container verifiable) {
        MimeType mimetype = verifiable.getMimeType();
        VerificationResult result = getFailureVerificationResult();
        try {
            byte[] realContent = Util.toByteArray(mimetype.getInputStream());
            if (Arrays.equals(expectedContent, realContent)) {
                result = VerificationResult.OK;
            }
        } catch (IOException e) {
            LOGGER.debug("Verifying MIME type failed!", e);
        }
        TerminatingVerificationResult verificationResult = new TerminatingVerificationResult(result, this, mimetype.getUri());
        return Arrays.asList((RuleVerificationResult) verificationResult);
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
