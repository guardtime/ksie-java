package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.annotation.ContainerAnnotationType;
import com.guardtime.container.manifest.AnnotationsManifest;
import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.util.Pair;
import com.guardtime.container.verification.result.RuleVerificationResult;
import com.guardtime.container.verification.rule.RuleState;

import java.util.LinkedList;
import java.util.List;

/**
 * This is a delegating rule, not verifying directly but by calling relevant rules to verify sub-components.
 * This rule focuses on verifying annotmanifest and {@link com.guardtime.container.annotation.ContainerAnnotation}s.
 */
public class AnnotationsIntegrityRule extends AbstractRule<SignatureContent> {

    public AnnotationsIntegrityRule() {
        this(RuleState.FAIL);
    }

    public AnnotationsIntegrityRule(RuleState state) {
        super(state);
    }

    @Override
    protected List<RuleVerificationResult> verifyRule(SignatureContent verifiable) {
        List<RuleVerificationResult> results = new LinkedList<>();

        //Annotmanifest existence
        results.addAll(new AnnotationsManifestExistenceRule(state).verify(verifiable));
        if (terminateVerification(results)) return results;
        //Annotmanifest integrity
        results.addAll(new AnnotationsManifestIntegrityRule(state).verify(verifiable));
        if (terminateVerification(results)) return results;
        //Annotations
        AnnotationsManifest annotationsManifest = verifiable.getAnnotationsManifest().getRight();
        List<? extends FileReference> singleAnnotationManifestReferences = annotationsManifest.getSingleAnnotationManifestReferences();
        for (FileReference reference : singleAnnotationManifestReferences) {
            List<RuleVerificationResult> manifestResults = getSingleAnnotationManifestVerificationResults(reference, verifiable);
            results.addAll(manifestResults);
            if (terminateVerification(manifestResults)) continue;
            results.addAll(getAnnotationDataVerificationResults(reference, verifiable));
        }

        return results;
    }

    private List<RuleVerificationResult> getSingleAnnotationManifestVerificationResults(FileReference reference, SignatureContent signatureContent) {
        List<RuleVerificationResult> results = new LinkedList<>();
        ContainerAnnotationType type = ContainerAnnotationType.fromContent(reference.getMimeType());
        RuleState ruleState = type.equals(ContainerAnnotationType.FULLY_REMOVABLE) ? RuleState.IGNORE : state;

        List<RuleVerificationResult> manifestExistenceResults = new SingleAnnotationManifestExistenceRule(ruleState).verify(Pair.of(signatureContent, reference));
        results.addAll(manifestExistenceResults);
        if (terminateVerification(manifestExistenceResults)) return results;
        List<RuleVerificationResult> manifestIntegrityResults = new SingleAnnotationManifestIntegrityRule(ruleState).verify(Pair.of(signatureContent, reference));
        results.addAll(manifestIntegrityResults);
        return results;
    }

    private List<RuleVerificationResult> getAnnotationDataVerificationResults(FileReference reference, SignatureContent signatureContent) {
        List<RuleVerificationResult> results = new LinkedList<>();
        ContainerAnnotationType type = ContainerAnnotationType.fromContent(reference.getMimeType());
        RuleState ruleState = type.equals(ContainerAnnotationType.NON_REMOVABLE) ? state : RuleState.IGNORE;

        List<RuleVerificationResult> annotationDataExistenceResults = new AnnotationDataExistenceRule(ruleState).verify(Pair.of(signatureContent, reference));
        results.addAll(annotationDataExistenceResults);
        if (terminateVerification(annotationDataExistenceResults)) return results;
        List<RuleVerificationResult> annotationDataIntegrityResults = new AnnotationDataIntegrityRule(ruleState).verify(Pair.of(signatureContent, reference));
        results.addAll(annotationDataIntegrityResults);
        return results;
    }
}
