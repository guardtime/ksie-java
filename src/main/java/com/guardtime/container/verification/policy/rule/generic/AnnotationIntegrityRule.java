package com.guardtime.container.verification.policy.rule.generic;

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
import com.guardtime.container.verification.policy.rule.RuleState;
import com.guardtime.container.verification.policy.rule.SignatureContentRule;
import com.guardtime.container.verification.result.GenericVerificationResult;
import com.guardtime.container.verification.result.RuleResult;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.ksi.hashing.DataHash;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class AnnotationIntegrityRule implements SignatureContentRule {
    private final RuleState state;

    public AnnotationIntegrityRule() {
        state = RuleState.FAIL;
    }

    public AnnotationIntegrityRule(RuleState state) {
        this.state = state;
    }

    @Override
    public boolean shouldBeIgnored(SignatureContent content, VerificationContext context) {
        if (state == RuleState.IGNORE) return true;

        SignatureManifest signatureManifest = content.getSignatureManifest().getRight();
        FileReference annotationsManifestReference = signatureManifest.getAnnotationsManifestReference();
        for (VerificationResult result : context.getResults()) {
            if (result.getTested().equals(annotationsManifestReference)) {
                return result.getResult() == RuleResult.NOK;
            }
        }
        return false;
    }

    @Override
    public RuleState getState() {
        return state;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public List<VerificationResult> verify(SignatureContent content, VerificationContext context) {
        List<VerificationResult> results = new LinkedList<>();
        AnnotationsManifest annotationsManifest = content.getAnnotationsManifest().getRight();

        for (FileReference reference : annotationsManifest.getAnnotationManifestReferences()) {
            RuleResult result = getFailureResult(); //RuleResult.OK;
            AnnotationInfoManifest annotationInfoManifest = getAnnotationInfoManifestForReference(reference, content);
            if (annotationInfoManifest == null) {
                result = getMissingManifestResult(reference);
            } else {
                try {
                    DataHash expectedDataHash = reference.getHash();
                    DataHash realDataHash = Util.hash(annotationInfoManifest.getInputStream(), expectedDataHash.getAlgorithm());
                    if (realDataHash.equals(expectedDataHash)) {
                        result = RuleResult.OK;
                    } else {
                        result = verifyAnnotation(reference, annotationInfoManifest, content);
                    }
                } catch (IOException e) {
                    // TODO: log exception?
                }
            }
            results.add(new GenericVerificationResult(result, this, reference));
        }
        return results;
    }

    private RuleResult verifyAnnotation(FileReference reference, AnnotationInfoManifest annotationInfoManifest, SignatureContent content) {
        RuleResult result = getFailureResult();
        try {
            ContainerAnnotation annotation = getAnnotationForManifest(annotationInfoManifest, content);
            if (annotation == null) {
                result = getMissingAnnotationResult(reference);
            } else {
                DataHash expextedDataHash = annotationInfoManifest.getDataManifestReference().getHash();
                DataHash realDataHash = annotation.getDataHash(expextedDataHash.getAlgorithm());
                if (realDataHash.equals(expextedDataHash)) {
                    result = RuleResult.OK;
                }
            }
        } catch (IOException e) {
            // TODO: log exception?
        }
        return result;
    }

    private ContainerAnnotation getAnnotationForManifest(AnnotationInfoManifest annotationInfoManifest, SignatureContent content) {
        String annotationUri = annotationInfoManifest.getAnnotationReference().getUri();
        for (Pair<String, ContainerAnnotation> annotation : content.getAnnotations()) {
            if (annotationUri.equals(annotation.getLeft())) return annotation.getRight();
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

    private RuleResult getFailureResult() {
        return getState() == RuleState.WARN ? RuleResult.WARN : RuleResult.NOK;
    }


    private RuleResult getMissingManifestResult(FileReference reference) {
        ContainerAnnotationType type = ContainerAnnotationType.fromContent(reference.getMimeType());
        if (type == ContainerAnnotationType.FULLY_REMOVABLE) return RuleResult.OK;
        return getFailureResult();
    }

    private RuleResult getMissingAnnotationResult(FileReference reference) {
        ContainerAnnotationType type = ContainerAnnotationType.fromContent(reference.getMimeType());
        if (type == ContainerAnnotationType.NON_REMOVABLE) return getFailureResult();
        return RuleResult.OK;
    }
}
