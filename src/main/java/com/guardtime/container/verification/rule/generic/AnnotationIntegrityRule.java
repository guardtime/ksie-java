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
    private final String name;

    public AnnotationIntegrityRule() {
        this(RuleState.FAIL);
    }

    public AnnotationIntegrityRule(RuleState state) {
        super(state);
        this.name = null; // TODO: Look into option of having nested rules or nested policies inside rules or sth and how the naming of such rules should be handled.
    }

    public boolean shouldIgnoredContent(SignatureContent content, VerificationContext context) {
        SignatureManifest signatureManifest = content.getSignatureManifest().getRight();
        FileReference annotationsManifestReference = signatureManifest.getAnnotationsManifestReference();
        for (RuleVerificationResult result : context.getResults()) {
            if (result.getTested().equals(annotationsManifestReference)) {
                return result.getResult() == RuleResult.NOK;
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
            results.add(new GenericVerificationResult(result, name, reference));
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
