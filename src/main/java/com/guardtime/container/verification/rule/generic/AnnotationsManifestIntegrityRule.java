package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.manifest.AnnotationsManifest;
import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.manifest.SignatureManifest;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.util.Pair;
import com.guardtime.container.util.Util;
import com.guardtime.container.verification.context.VerificationContext;
import com.guardtime.container.verification.result.GenericVerificationResult;
import com.guardtime.container.verification.result.RuleResult;
import com.guardtime.container.verification.result.RuleVerificationResult;
import com.guardtime.container.verification.rule.RuleState;
import com.guardtime.ksi.hashing.DataHash;

import java.io.IOException;
import java.util.List;

public class AnnotationsManifestIntegrityRule extends SignatureContentRule {

    private static final String KSIE_VERIFY_ANNOTATIONS_MANIFEST = "KSIE_VERIFY_ANNOTATIONS_MANIFEST";

    public AnnotationsManifestIntegrityRule() {
        super(KSIE_VERIFY_ANNOTATIONS_MANIFEST);
    }

    public AnnotationsManifestIntegrityRule(RuleState state) {
        super(state, KSIE_VERIFY_ANNOTATIONS_MANIFEST);
    }

    @Override
    protected List<Pair<? extends Object, ? extends RuleVerificationResult>> verifySignatureContent(SignatureContent content, VerificationContext context) {
        RuleResult result = getFailureResult();
        SignatureManifest signatureManifest = content.getSignatureManifest().getRight();
        FileReference annotationsManifestReference = signatureManifest.getAnnotationsManifestReference();
        try {
            AnnotationsManifest annotationsManifest = content.getAnnotationsManifest().getRight();
            DataHash expectedDataHash = annotationsManifestReference.getHash();
            // TODO: review annotationsManifest and add getDataHash if possible
            DataHash realHash = Util.hash(annotationsManifest.getInputStream(), expectedDataHash.getAlgorithm());
            if (expectedDataHash.equals(realHash)) {
                result = RuleResult.OK;
            }
        } catch (NullPointerException | IOException e) {
            LOGGER.debug("Verifying annotmanifest failed!", e);
        }
        return asReturnablePairList(annotationsManifestReference, new GenericVerificationResult(result, this));
    }

}
