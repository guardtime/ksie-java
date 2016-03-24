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

    private static final String KSIE_VERIFY_ANNOTATION_MANIFEST = "KSIE_VERIFY_ANNOTATION_MANIFEST";

    public AnnotationInfoManifestIntegrityRule() {
        super(KSIE_VERIFY_ANNOTATION_MANIFEST);
    }

    public AnnotationInfoManifestIntegrityRule(RuleState state) {
        super(state, KSIE_VERIFY_ANNOTATION_MANIFEST);
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

    @Override
    protected List<Pair<? extends Object, ? extends RuleVerificationResult>> verifySignatureContent(SignatureContent content, VerificationContext context) {
        List<Pair<? extends Object, ? extends RuleVerificationResult>> results = new LinkedList<>();
        if (shouldIgnoreContent(content, context)) return results;

        AnnotationsManifest annotationsManifest = content.getAnnotationsManifest().getRight();
        for (FileReference reference : annotationsManifest.getAnnotationManifestReferences()) {
            AnnotationInfoManifest annotationInfoManifest = getAnnotationInfoManifestForReference(reference, content);
            RuleResult result = getAnnotationManifestResult(reference, annotationInfoManifest);
            results.add(Pair.of(reference, new GenericVerificationResult(result, this)));
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
}
