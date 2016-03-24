package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.annotation.ContainerAnnotationType;
import com.guardtime.container.manifest.AnnotationInfoManifest;
import com.guardtime.container.manifest.AnnotationsManifest;
import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.manifest.SignatureManifest;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.util.Pair;
import com.guardtime.container.verification.context.VerificationContext;
import com.guardtime.container.verification.result.GenericVerificationResult;
import com.guardtime.container.verification.result.RuleResult;
import com.guardtime.container.verification.result.RuleVerificationResult;
import com.guardtime.container.verification.rule.RuleState;
import com.guardtime.ksi.hashing.DataHash;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class AnnotationDataIntegrityRule extends SignatureContentRule {

    private static final String KSIE_VERIFY_ANNOTATION_DATA = "KSIE_VERIFY_ANNOTATION_DATA";

    public AnnotationDataIntegrityRule() {
        super(KSIE_VERIFY_ANNOTATION_DATA);
    }

    public AnnotationDataIntegrityRule(RuleState state) {
        super(state, KSIE_VERIFY_ANNOTATION_DATA);
    }

    @Override
    protected List<Pair<? extends Object, ? extends RuleVerificationResult>> verifySignatureContent(SignatureContent content, VerificationContext context) {
        List<Pair<? extends Object, ? extends RuleVerificationResult>> results = new LinkedList<>();
        if (shouldIgnoreContent(content, context)) return results;

        AnnotationsManifest annotationsManifest = content.getAnnotationsManifest().getRight();
        for (FileReference reference : annotationsManifest.getAnnotationManifestReferences()) {
            AnnotationInfoManifest annotationInfoManifest = getAnnotationInfoManifestForReference(reference, content);
            if (shouldIgnoreReference(annotationInfoManifest, context)) continue;
            results.add(verifyAnnotationData(reference, annotationInfoManifest, content));
        }
        return results;
    }

    private boolean shouldIgnoreReference(AnnotationInfoManifest annotationInfoManifest, VerificationContext context) {
        if (annotationInfoManifest == null) return true;
        List<RuleVerificationResult> resultsForAnnotationManifest = context.getResultsFor(annotationInfoManifest);
        if (resultsForAnnotationManifest == null)
            return true; // Shouldn't verify annotation data if we don't know if the manifests are even valid
        for (RuleVerificationResult result : resultsForAnnotationManifest) {
            if (!RuleResult.OK.equals(result.getResult())) {
                return true;
            }
        }
        return false;
    }

    private boolean shouldIgnoreContent(SignatureContent content, VerificationContext context) {
        SignatureManifest signatureManifest = content.getSignatureManifest().getRight();
        FileReference annotationsManifestReference = signatureManifest.getAnnotationsManifestReference();
        for (RuleVerificationResult result : context.getResultsFor(annotationsManifestReference)) {
            if (!RuleResult.OK.equals(result.getResult())) {
                return true;
            }
        }
        return false;
    }

    private AnnotationInfoManifest getAnnotationInfoManifestForReference(FileReference reference, SignatureContent content) {
        for (Pair<String, AnnotationInfoManifest> manifest : content.getAnnotationManifests()) {
            if (manifest.getLeft().equals(reference.getUri())) {
                return manifest.getRight();
            }
        }
        return null;
    }

    private Pair<? extends Object, ? extends RuleVerificationResult> verifyAnnotationData(FileReference reference, AnnotationInfoManifest annotationInfoManifest, SignatureContent content) {
        RuleResult result = getFailureResult();
        try {
            ContainerAnnotation annotation = getAnnotationForManifest(annotationInfoManifest, content);
            DataHash expectedDataHash = annotationInfoManifest.getAnnotationReference().getHash();
            DataHash realDataHash = annotation.getDataHash(expectedDataHash.getAlgorithm());
            if (realDataHash.equals(expectedDataHash)) {
                result = RuleResult.OK;
            }
        } catch (NullPointerException | IOException e) {
            // TODO: log exception?
            result = getMissingAnnotationResult(reference);
        }
        return Pair.of(reference, new GenericVerificationResult(result, this));
    }

    private ContainerAnnotation getAnnotationForManifest(AnnotationInfoManifest annotationInfoManifest, SignatureContent content) {
        String annotationUri = annotationInfoManifest.getAnnotationReference().getUri();
        for (Pair<String, ContainerAnnotation> annotation : content.getAnnotations()) {
            if (annotationUri.equals(annotation.getLeft())) {
                return annotation.getRight();
            }
        }
        return null;
    }

    private RuleResult getMissingAnnotationResult(FileReference reference) {
        ContainerAnnotationType type = ContainerAnnotationType.fromContent(reference.getMimeType());
        if (ContainerAnnotationType.NON_REMOVABLE.equals(type)) return getFailureResult();
        return RuleResult.OK;
    }
}
