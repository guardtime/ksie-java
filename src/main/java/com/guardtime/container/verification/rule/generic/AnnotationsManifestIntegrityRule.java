package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.manifest.AnnotationsManifest;
import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.manifest.SignatureManifest;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.verification.context.VerificationContext;
import com.guardtime.container.verification.result.GenericVerificationResult;
import com.guardtime.container.verification.result.RuleResult;
import com.guardtime.container.verification.rule.RuleState;
import com.guardtime.ksi.hashing.DataHash;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class AnnotationsManifestIntegrityRule extends SignatureContentRule<GenericVerificationResult> {

    private static final String KSIE_VERIFY_ANNOTATIONS_MANIFEST = "KSIE_VERIFY_ANNOTATIONS_MANIFEST";

    public AnnotationsManifestIntegrityRule() {
        super(KSIE_VERIFY_ANNOTATIONS_MANIFEST);
    }

    public AnnotationsManifestIntegrityRule(RuleState state) {
        super(state, KSIE_VERIFY_ANNOTATIONS_MANIFEST);
    }

    @Override
    protected List<GenericVerificationResult> verifySignatureContent(SignatureContent content, VerificationContext context) {
        RuleResult result = getFailureResult();
        SignatureManifest signatureManifest = content.getSignatureManifest().getRight();
        FileReference annotationsManifestReference = signatureManifest.getAnnotationsManifestReference();
        AnnotationsManifest annotationsManifest = content.getAnnotationsManifest().getRight();
        try {
            DataHash expectedDataHash = annotationsManifestReference.getHash();
            DataHash realHash = annotationsManifest.getDataHash(expectedDataHash.getAlgorithm());
            if (expectedDataHash.equals(realHash)) {
                result = RuleResult.OK;
            }
        } catch (NullPointerException | IOException e) {
            LOGGER.debug("Verifying annotmanifest failed!", e);
        }
        return Arrays.asList(new GenericVerificationResult(result, this, annotationsManifest));
    }

}
