package com.guardtime.container.verification.policy.rule.generic;

import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.annotation.ContainerAnnotationType;
import com.guardtime.container.manifest.*;
import com.guardtime.container.packaging.BlockChainContainer;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.util.Pair;
import com.guardtime.container.util.Util;
import com.guardtime.container.verification.context.VerificationContext;
import com.guardtime.container.verification.policy.rule.RuleState;
import com.guardtime.container.verification.policy.rule.VerificationRule;
import com.guardtime.container.verification.result.GenericVerificationResult;
import com.guardtime.container.verification.result.RuleResult;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.ksi.hashing.DataHash;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class AnnotationIntegrityRule implements VerificationRule {
    private final RuleState state;

    public AnnotationIntegrityRule() {
        state = RuleState.FAIL;
    }

    public AnnotationIntegrityRule(RuleState state) {
        this.state = state;
    }

    @Override
    public boolean shouldBeIgnored(List<VerificationResult> previousResults) {
        return state == RuleState.IGNORE;
    }

    @Override
    public RuleState getState() {
        return state;
    }

    @Override
    public List<VerificationResult> verify(VerificationContext context) {
        BlockChainContainer container = context.getContainer();
        List<VerificationResult> results = new LinkedList<>();
        for (SignatureContent content : container.getSignatureContents()) {
            RuleResult result = RuleResult.OK;
            Pair<String, AnnotationsManifest> stringAnnotationsManifestPair = content.getAnnotationsManifest();
            if(stringAnnotationsManifestPair == null){
                // TODO: umn, what?
                continue;
            }
            AnnotationsManifest annotationsManifest = stringAnnotationsManifestPair.getRight();
            if(skipVerifyingManifest(annotationsManifest, context)) continue;
            results.addAll(verifyAnnotationsInManifest(annotationsManifest, content));
        }
        return results;
    }

    private List<VerificationResult> verifyAnnotationsInManifest(AnnotationsManifest annotationsManifest, SignatureContent content) {
        List<VerificationResult> results = new LinkedList<>();
        for(FileReference reference : annotationsManifest.getAnnotationManifestReferences()) {
            RuleResult result = RuleResult.OK;
            AnnotationInfoManifest annotationInfoManifest = getAnnotationInfoManifestForReference(reference, content);
            if(annotationInfoManifest == null) {
                result = getMissingManifestResult(reference);
            } else {
                try {
                    DataHash expectedDataHash = reference.getHash();
                    DataHash realDataHash = Util.hash(annotationInfoManifest.getInputStream(), expectedDataHash.getAlgorithm());
                    if(!realDataHash.equals(expectedDataHash)){
                        result = getFailureResult();
                    } else {
                        result = verifyAnnotation(reference, annotationInfoManifest, content);
                    }
                } catch (IOException e) {
                    // log exception?
                    result = getFailureResult();
                }
            }
            results.add(new GenericVerificationResult(result, this, reference));
        }
        return results;
    }

    private RuleResult verifyAnnotation(FileReference reference, AnnotationInfoManifest annotationInfoManifest, SignatureContent content) {
        RuleResult result = RuleResult.OK;
        try {
            ContainerAnnotation annotation = getAnnotationForManifest(annotationInfoManifest, content);
            if(annotation == null) {
                result = getMissingAnnotationResult(reference);
            } else {
                DataHash expextedDataHash = annotationInfoManifest.getDataManifestReference().getHash();
                DataHash realDataHash = annotation.getDataHash(expextedDataHash.getAlgorithm());
                if(!realDataHash.equals(expextedDataHash)){
                    result = getFailureResult();
                }
            }
        } catch (IOException e) {
            // log exception?
            result = getFailureResult();
        }
        return result;
    }

    private ContainerAnnotation getAnnotationForManifest(AnnotationInfoManifest annotationInfoManifest, SignatureContent content) {
        for(Pair<String, ContainerAnnotation> annotation : content.getAnnotations()) {
            String annotationUri = annotationInfoManifest.getAnnotationReference().getUri();
            if(annotationUri.equals(annotation.getLeft())) return annotation.getRight();
        }
        return null;
    }

    private AnnotationInfoManifest getAnnotationInfoManifestForReference(FileReference reference, SignatureContent content) {
        for(Pair<String, AnnotationInfoManifest> manifest : content.getAnnotationManifests()) {
            if(manifest.getLeft().equals(reference.getUri())){
                return manifest.getRight();
            }
        }
        return null;
    }

    private boolean skipVerifyingManifest(AnnotationsManifest annotationsManifest, VerificationContext context) {
        for(VerificationResult result : context.getResults()){
            if(result.getTested().equals(annotationsManifest)){
                return result.getResult() == RuleResult.NOK;
            }
        }
        return false;
    }

    private RuleResult getFailureResult() {
        return getState() == RuleState.WARN ? RuleResult.WARN : RuleResult.NOK;
    }


    private RuleResult getMissingManifestResult(FileReference reference) {
        ContainerAnnotationType type = ContainerAnnotationType.fromContent(reference.getMimeType());
        if(type == ContainerAnnotationType.FULLY_REMOVABLE) return RuleResult.OK;
        return getState() == RuleState.WARN ? RuleResult.WARN : RuleResult.NOK;
    }

    private RuleResult getMissingAnnotationResult(FileReference reference) {
        ContainerAnnotationType type = ContainerAnnotationType.fromContent(reference.getMimeType());
        if(type == ContainerAnnotationType.NON_REMOVABLE) return getFailureResult();
        return RuleResult.OK;
    }
}
