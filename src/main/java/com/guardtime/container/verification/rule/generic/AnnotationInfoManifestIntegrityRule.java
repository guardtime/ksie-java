package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.annotation.ContainerAnnotationType;
import com.guardtime.container.manifest.AnnotationInfoManifest;
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
import java.util.LinkedList;
import java.util.List;

public class AnnotationInfoManifestIntegrityRule extends SignatureContentRule {

    private static final String KSIE_VERIFY_ANNOTATION_INFO_MANIFEST = "KSIE_VERIFY_ANNOTATION_INFO_MANIFEST";

    public AnnotationInfoManifestIntegrityRule() {
        super(KSIE_VERIFY_ANNOTATION_INFO_MANIFEST);
    }

    public AnnotationInfoManifestIntegrityRule(RuleState state) {
        super(state, KSIE_VERIFY_ANNOTATION_INFO_MANIFEST);
    }

    @Override
    protected List<Pair<? extends Object, ? extends RuleVerificationResult>> verifySignatureContent(SignatureContent content, VerificationContext context) {
        List<Pair<? extends Object, ? extends RuleVerificationResult>> results = new LinkedList<>();
        if (shouldIgnoreContent(content, context)) return results;

        AnnotationsManifest annotationsManifest = content.getAnnotationsManifest().getRight();
        for (FileReference reference : annotationsManifest.getAnnotationInfoManifestReferences()) {
            AnnotationInfoManifest annotationInfoManifest = content.getAnnotationInfoManifests().get(reference.getUri());
            results.add(getAnnotationInfoManifestResult(reference, annotationInfoManifest));
            results.add(getDataFilesManifestReferenceResult(reference, content, annotationInfoManifest));
        }
        return results;
    }

    private boolean shouldIgnoreContent(SignatureContent content, VerificationContext context) {
        SignatureManifest signatureManifest = content.getSignatureManifest().getRight();
        FileReference annotationsManifestReference = signatureManifest.getAnnotationsManifestReference();
        List<RuleVerificationResult> resultsForAnnotationsManifestReference = context.getResultsFor(annotationsManifestReference);
        if (resultsForAnnotationsManifestReference == null) return true;
        for (RuleVerificationResult result : resultsForAnnotationsManifestReference) {
            if (!RuleResult.OK.equals(result.getResult())) {
                return true;
            }
        }
        return false;
    }

    private Pair<FileReference, GenericVerificationResult> getAnnotationInfoManifestResult(FileReference reference, AnnotationInfoManifest annotationInfoManifest) {
        RuleResult result = getFailureResult();
        try {
            DataHash expectedHash = reference.getHash();
            // TODO: review annotationInfoManifest and add getDataHash if possible
            DataHash realHash = Util.hash(annotationInfoManifest.getInputStream(), expectedHash.getAlgorithm());
            if (realHash.equals(expectedHash)) {
                result = RuleResult.OK;
            }
        } catch (IOException e) {
            LOGGER.debug("Verifying annotation manifest failed!", e);
            result = getMissingManifestResult(reference);
        }
        return Pair.of(reference, new GenericVerificationResult(result, this));
    }

    private Pair<FileReference, GenericVerificationResult> getDataFilesManifestReferenceResult(FileReference reference, SignatureContent content, AnnotationInfoManifest annotationInfoManifest) {
        RuleResult result = getFailureResult();
        SignatureManifest signatureManifest = content.getSignatureManifest().getRight();
        FileReference expectedReference = signatureManifest.getDataFilesManifestReference();
        FileReference realReference = annotationInfoManifest.getDataManifestReference();
        if (realReference.equals(expectedReference)) {
            result = RuleResult.OK;
        }
        return Pair.of(reference, new GenericVerificationResult(result, this));
    }

    private RuleResult getMissingManifestResult(FileReference reference) {
        ContainerAnnotationType type = ContainerAnnotationType.fromContent(reference.getMimeType());
        if (ContainerAnnotationType.FULLY_REMOVABLE.equals(type)) return RuleResult.OK;
        return getFailureResult();
    }
}
