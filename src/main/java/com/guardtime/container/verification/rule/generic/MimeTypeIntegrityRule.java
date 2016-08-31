package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.ContainerPackagingFactory;
import com.guardtime.container.packaging.MimeType;
import com.guardtime.container.verification.result.GenericVerificationResult;
import com.guardtime.container.verification.result.ResultHolder;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.container.verification.rule.AbstractRule;
import com.guardtime.container.verification.rule.ContainerRule;
import com.guardtime.container.verification.rule.RuleState;
import com.guardtime.container.verification.rule.RuleTerminatingException;
import com.guardtime.ksi.util.Util;

import java.io.IOException;
import java.util.Arrays;

/**
 * Rule that verifies the existence and content of a MIMETYPE file in the container. The expected content is given by
 * {@link ContainerPackagingFactory}.
 */
public class MimeTypeIntegrityRule extends AbstractRule<Container> implements ContainerRule {
    private final byte[] expectedContent;

    public MimeTypeIntegrityRule(RuleState state, ContainerPackagingFactory packagingFactory) {
        super(state);
        this.expectedContent = packagingFactory.getMimeTypeContent();
    }

    @Override
    protected void verifyRule(ResultHolder holder, Container verifiable) throws RuleTerminatingException {
        MimeType mimetype = verifiable.getMimeType();
        VerificationResult result = getFailureVerificationResult();
        String mimetypeUri = mimetype.getUri();
        try {
            byte[] realContent = Util.toByteArray(mimetype.getInputStream());
            if (Arrays.equals(expectedContent, realContent)) {
                result = VerificationResult.OK;
            }
            holder.addResult(new GenericVerificationResult(result, this, mimetypeUri));
        } catch (IOException e) {
            LOGGER.info("Verifying MIME type failed!", e);
            holder.addResult(new GenericVerificationResult(result, this, mimetypeUri, e));
        }

        if (!result.equals(VerificationResult.OK)) {
            throw new RuleTerminatingException("MIME type integrity could not be verified for '" + mimetypeUri + "'");
        }
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
