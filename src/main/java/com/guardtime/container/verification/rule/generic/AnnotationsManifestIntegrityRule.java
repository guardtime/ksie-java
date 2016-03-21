package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.manifest.AnnotationsManifest;
import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.util.Util;
import com.guardtime.container.verification.context.VerificationContext;
import com.guardtime.container.verification.rule.RuleState;
import com.guardtime.container.verification.rule.SignatureContentRule;
import com.guardtime.container.verification.result.GenericVerificationResult;
import com.guardtime.container.verification.result.RuleResult;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.ksi.hashing.DataHash;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class AnnotationsManifestIntegrityRule implements SignatureContentRule {
    private final String name;
    private final RuleState state;

    public AnnotationsManifestIntegrityRule() {
        this(RuleState.FAIL);
    }

    public AnnotationsManifestIntegrityRule(RuleState state) {
        this.state = state;
        this.name = "KSIE_VERIFY_ANNOTATIONS_MANIFEST";
    }

    @Override
    public List<VerificationResult> verify(SignatureContent content, VerificationContext context) {
        RuleResult result = getFailureResult();
        FileReference annotationsManifestReference = content.getSignatureManifest().getRight().getAnnotationsManifestReference();
        try {
            AnnotationsManifest annotationsManifest = content.getAnnotationsManifest().getRight();
            DataHash expectedDataHash = annotationsManifestReference.getHash();
            DataHash realHash = Util.hash(annotationsManifest.getInputStream(), expectedDataHash.getAlgorithm());
            if (expectedDataHash.equals(realHash)) {
                result = RuleResult.OK;
            }
        } catch (NullPointerException | IOException e) {
            // TODO: log exception?
        }
        return Arrays.asList((VerificationResult) new GenericVerificationResult(result, name, annotationsManifestReference));
    }

    @Override
    public boolean shouldBeIgnored(SignatureContent content, VerificationContext context) {
        return state == RuleState.IGNORE;
    }

    private RuleResult getFailureResult() {
        return state == RuleState.WARN ? RuleResult.WARN : RuleResult.NOK;
    }
}
