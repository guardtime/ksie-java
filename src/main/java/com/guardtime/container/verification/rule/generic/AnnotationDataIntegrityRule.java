package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.annotation.ContainerAnnotationType;
import com.guardtime.container.manifest.AnnotationInfoManifest;
import com.guardtime.container.manifest.AnnotationReference;
import com.guardtime.container.manifest.AnnotationsManifest;
import com.guardtime.container.manifest.FileReference;
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

public class AnnotationDataIntegrityRule extends SignatureContentRule<GenericVerificationResult> {

    private static final String KSIE_VERIFY_ANNOTATION_DATA = "KSIE_VERIFY_ANNOTATION_DATA";

    public AnnotationDataIntegrityRule() {
        super(KSIE_VERIFY_ANNOTATION_DATA);
    }

    public AnnotationDataIntegrityRule(RuleState state) {
        super(state, KSIE_VERIFY_ANNOTATION_DATA);
    }

    @Override
    protected List<GenericVerificationResult> verifySignatureContent(SignatureContent content, VerificationContext context) {
        List<GenericVerificationResult> results = new LinkedList<>();
        if (shouldIgnoreContent(content, context)) return results;

        AnnotationsManifest annotationsManifest = content.getAnnotationsManifest().getRight();
        for (FileReference reference : annotationsManifest.getAnnotationInfoManifestReferences()) {
            AnnotationInfoManifest annotationInfoManifest = content.getAnnotationInfoManifests().get(reference.getUri());
            if (shouldIgnoreAnnotation(annotationInfoManifest, context)) continue;
            AnnotationReference annotationReference = annotationInfoManifest.getAnnotationReference();
            ContainerAnnotation annotation = content.getAnnotations().get(annotationReference.getUri());
            results.add(verifyAnnotationData(reference, annotationReference, annotation));
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

    private boolean shouldIgnoreAnnotation(AnnotationInfoManifest annotationInfoManifest, VerificationContext context) {
        if (annotationInfoManifest == null) return true;
        List<RuleVerificationResult> resultsForAnnotationManifest = context.getResultsFor(annotationInfoManifest);
        if (resultsForAnnotationManifest == null) {
            return true; // Shouldn't verify annotation data if we don't know if the manifests are even valid
        }
        for (RuleVerificationResult result : resultsForAnnotationManifest) {
            if (!RuleResult.OK.equals(result.getResult())) {
                return true;
            }
        }
        return false;
    }

    private GenericVerificationResult verifyAnnotationData(FileReference reference, AnnotationReference annotationReference, ContainerAnnotation annotation) {
        RuleResult result = getFailureResult();
        try {
            DataHash expectedDataHash = annotationReference.getHash();
            DataHash realDataHash = annotation.getDataHash(expectedDataHash.getAlgorithm());
            if (realDataHash.equals(expectedDataHash)) {
                result = RuleResult.OK;
            }
        } catch (NullPointerException | IOException e) {
            LOGGER.debug("Verifying annotation data failed!", e);
            result = getMissingAnnotationResult(reference);
        }
        return new GenericVerificationResult(result, this, annotation);
    }

    private RuleResult getMissingAnnotationResult(FileReference reference) {
        ContainerAnnotationType type = ContainerAnnotationType.fromContent(reference.getMimeType());
        if (ContainerAnnotationType.NON_REMOVABLE.equals(type)) return getFailureResult();
        return RuleResult.OK;
    }
}
