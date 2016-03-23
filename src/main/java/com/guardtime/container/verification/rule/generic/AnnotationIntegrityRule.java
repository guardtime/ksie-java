package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.annotation.ContainerAnnotation;
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

public class AnnotationIntegrityRule extends SignatureContentRule {

    private static final String KSIE_VERIFY_ANNOTATION_DATA = "KSIE_VERIFY_ANNOTATION_DATA";
    private static final String KSIE_VERIFY_ANNOTATION_MANIFEST = "KSIE_VERIFY_ANNOTATION_MANIFEST";

    public AnnotationIntegrityRule() {
        this(RuleState.FAIL);
    }

    public AnnotationIntegrityRule(RuleState state) {
        super(state);
    }

    public boolean shouldIgnoredContent(SignatureContent content, VerificationContext context) {
        SignatureManifest signatureManifest = content.getSignatureManifest().getRight();
        FileReference annotationsManifestReference = signatureManifest.getAnnotationsManifestReference();
        for (RuleVerificationResult result : context.getResults()) {
            if (result.getTested().equals(annotationsManifestReference)) {
                return RuleResult.NOK.equals(result.getResult());
            }
        }
        return false;
    }

    @Override
    protected List<RuleVerificationResult> verifySignatureContent(SignatureContent content, VerificationContext context) {
        List<RuleVerificationResult> results = new LinkedList<>();
        if (shouldIgnoredContent(content, context)) return results;

        AnnotationsManifest annotationsManifest = content.getAnnotationsManifest().getRight();
        for (FileReference reference : annotationsManifest.getAnnotationManifestReferences()) {
            AnnotationInfoManifest annotationInfoManifest = getAnnotationInfoManifestForReference(reference, content);
            RuleResult result = getAnnotationManifestResult(reference, annotationInfoManifest);
            results.add(new GenericVerificationResult(result, KSIE_VERIFY_ANNOTATION_MANIFEST, reference));
            if (RuleResult.OK.equals(result)) {
                // Manifest was OK, need to verify data
                results.add(verifyAnnotation(reference, annotationInfoManifest, content));
            }
        }
        return results;
    }

    private RuleResult getAnnotationManifestResult(FileReference reference, AnnotationInfoManifest annotationInfoManifest) {
        RuleResult result = getFailureResult();
        try {
            DataHash expectedDataHash = reference.getHash();
            DataHash realDataHash = Util.hash(annotationInfoManifest.getInputStream(), expectedDataHash.getAlgorithm());
            if (realDataHash.equals(expectedDataHash)) {
                result = RuleResult.OK;
            }
        } catch (IOException e) {
            // TODO: log exception?
            result = getMissingManifestResult(reference);
        }
        return result;
    }

    private RuleVerificationResult verifyAnnotation(FileReference reference, AnnotationInfoManifest annotationInfoManifest, SignatureContent content) {
        RuleResult result = getFailureResult();
        try {
            ContainerAnnotation annotation = getAnnotationForManifest(annotationInfoManifest, content);
            DataHash expextedDataHash = annotationInfoManifest.getAnnotationReference().getHash();
            DataHash realDataHash = annotation.getDataHash(expextedDataHash.getAlgorithm());
            if (realDataHash.equals(expextedDataHash)) {
                result = RuleResult.OK;
            }
        } catch (NullPointerException | IOException e) {
            // TODO: log exception?
            result = getMissingAnnotationResult(reference);
        }
        return new GenericVerificationResult(result, KSIE_VERIFY_ANNOTATION_DATA, reference);
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

    private AnnotationInfoManifest getAnnotationInfoManifestForReference(FileReference reference, SignatureContent content) {
        for (Pair<String, AnnotationInfoManifest> manifest : content.getAnnotationManifests()) {
            if (manifest.getLeft().equals(reference.getUri())) {
                return manifest.getRight();
            }
        }
        return null;
    }


    private RuleResult getMissingManifestResult(FileReference reference) {
        ContainerAnnotationType type = ContainerAnnotationType.fromContent(reference.getMimeType());
        if (ContainerAnnotationType.FULLY_REMOVABLE.equals(type)) return RuleResult.OK;
        return getFailureResult();
    }

    private RuleResult getMissingAnnotationResult(FileReference reference) {
        ContainerAnnotationType type = ContainerAnnotationType.fromContent(reference.getMimeType());
        if (ContainerAnnotationType.NON_REMOVABLE.equals(type)) return getFailureResult();
        return RuleResult.OK;
    }
}
