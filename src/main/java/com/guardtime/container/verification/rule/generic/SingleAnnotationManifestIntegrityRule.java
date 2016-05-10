package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.annotation.ContainerAnnotationType;
import com.guardtime.container.manifest.SingleAnnotationManifest;
import com.guardtime.container.manifest.AnnotationsManifest;
import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.manifest.SignatureManifest;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.verification.context.VerificationContext;
import com.guardtime.container.verification.result.GenericVerificationResult;
import com.guardtime.container.verification.result.RuleResult;
import com.guardtime.container.verification.result.RuleVerificationResult;
import com.guardtime.container.verification.rule.RuleState;
import com.guardtime.ksi.hashing.DataHash;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Rule that verifies the type and hash integrity of {@link SingleAnnotationManifest} as noted by its {@link
 * AnnotationsManifest}.
 */
public class SingleAnnotationManifestIntegrityRule extends SignatureContentRule<GenericVerificationResult> {

    private static final String KSIE_VERIFY_SINGLE_ANNOTATION_MANIFEST = "KSIE_VERIFY_SINGLE_ANNOTATION_MANIFEST";

    public SingleAnnotationManifestIntegrityRule() {
        super(KSIE_VERIFY_SINGLE_ANNOTATION_MANIFEST);
    }

    public SingleAnnotationManifestIntegrityRule(RuleState state) {
        super(state, KSIE_VERIFY_SINGLE_ANNOTATION_MANIFEST);
    }

    @Override
    protected List<GenericVerificationResult> verifySignatureContent(SignatureContent content, VerificationContext context) {
        List<GenericVerificationResult> results = new LinkedList<>();
        if (shouldIgnoreContent(content, context)) return results;

        AnnotationsManifest annotationsManifest = content.getAnnotationsManifest().getRight();
        for (FileReference reference : annotationsManifest.getSingleAnnotationManifestReferences()) {
            SingleAnnotationManifest singleAnnotationManifest = content.getSingleAnnotationManifests().get(reference.getUri());
            results.add(getSingleAnnotationManifestResult(reference, singleAnnotationManifest));
            results.add(getDataFilesManifestReferenceResult(content, singleAnnotationManifest));
        }
        return results;
    }

    private boolean shouldIgnoreContent(SignatureContent content, VerificationContext context) {
        AnnotationsManifest annotationsManifest = content.getAnnotationsManifest().getRight();
        List<RuleVerificationResult> resultsForAnnotationsManifest = context.getResultsFor(annotationsManifest);
        if (resultsForAnnotationsManifest == null) return true;
        for (RuleVerificationResult result : resultsForAnnotationsManifest) {
            if (!RuleResult.OK.equals(result.getResult())) {
                return true;
            }
        }
        return false;
    }

    private GenericVerificationResult getSingleAnnotationManifestResult(FileReference reference, SingleAnnotationManifest singleAnnotationManifest) {
        RuleResult result = getFailureResult();
        try {
            DataHash expectedDataHash = reference.getHash();
            DataHash realDataHash = singleAnnotationManifest.getDataHash(expectedDataHash.getAlgorithm());
            if (realDataHash.equals(expectedDataHash)) {
                result = RuleResult.OK;
            }
        } catch (IOException e) {
            LOGGER.debug("Verifying annotation manifest failed!", e);
            result = getMissingManifestResult(reference);
        }
        return new GenericVerificationResult(result, this, singleAnnotationManifest);
    }

    private GenericVerificationResult getDataFilesManifestReferenceResult(SignatureContent content, SingleAnnotationManifest singleAnnotationManifest) {
        RuleResult result = getFailureResult();
        SignatureManifest signatureManifest = content.getSignatureManifest().getRight();
        FileReference expectedReference = signatureManifest.getDataFilesManifestReference();
        FileReference realReference = singleAnnotationManifest.getDataManifestReference();
        if (realReference.equals(expectedReference)) {
            result = RuleResult.OK;
        }
        return new GenericVerificationResult(result, this, singleAnnotationManifest);
    }

    private RuleResult getMissingManifestResult(FileReference reference) {
        ContainerAnnotationType type = ContainerAnnotationType.fromContent(reference.getMimeType());
        if (ContainerAnnotationType.FULLY_REMOVABLE.equals(type)) return RuleResult.OK;
        return getFailureResult();
    }
}
