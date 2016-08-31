package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.manifest.AnnotationsManifest;
import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.util.Pair;
import com.guardtime.container.verification.result.ResultHolder;
import com.guardtime.container.verification.rule.AbstractRule;
import com.guardtime.container.verification.rule.RuleState;
import com.guardtime.container.verification.rule.RuleTerminatingException;

import java.util.List;

/**
 * This is a delegating rule, not verifying directly but by calling relevant rules to verify sub-components. This rule
 * focuses on verifying annotmanifest and {@link com.guardtime.container.annotation.ContainerAnnotation}s.
 */
public class AnnotationsIntegrityRule extends AbstractRule<SignatureContent> {

    private AnnotationsManifestExistenceRule annotationsManifestExistenceRule;
    private AnnotationsManifestIntegrityRule annotationsManifestIntegrityRule;
    private SingleAnnotationManifestExistenceRule singleAnnotationManifestExistenceRule;
    private SingleAnnotationManifestIntegrityRule singleAnnotationManifestIntegrityRule;
    private AnnotationDataExistenceRule annotationDataExistenceRule;
    private AnnotationDataIntegrityRule annotationDataIntegrityRule;

    public AnnotationsIntegrityRule(RuleState state) {
        super(state);
        annotationsManifestExistenceRule = new AnnotationsManifestExistenceRule(state);
        annotationsManifestIntegrityRule = new AnnotationsManifestIntegrityRule(state);
        singleAnnotationManifestExistenceRule = new SingleAnnotationManifestExistenceRule(state);
        singleAnnotationManifestIntegrityRule = new SingleAnnotationManifestIntegrityRule(state);
        annotationDataExistenceRule = new AnnotationDataExistenceRule(state);
        annotationDataIntegrityRule = new AnnotationDataIntegrityRule(state);
    }

    @Override
    protected void verifyRule(ResultHolder holder, SignatureContent verifiable) {
        if (!processAnnotationsManifestVerification(holder, verifiable)) {
            return;
        }

        //Annotations
        for (FileReference reference : getSingleAnnotationManifestReferences(verifiable)) {
            processAnnotationVerification(holder, Pair.of(verifiable, reference));
        }
    }

    private boolean processAnnotationsManifestVerification(ResultHolder holder, SignatureContent verifiable) {
        try {
            annotationsManifestExistenceRule.verify(holder, verifiable);
            annotationsManifestIntegrityRule.verify(holder, verifiable);
        } catch (RuleTerminatingException e) {
            LOGGER.info("Halting verification chain for annotations manifest!, caused by '{}'", e.getMessage());
            return false;
        }
        return true;
    }

    private List<? extends FileReference> getSingleAnnotationManifestReferences(SignatureContent verifiable) {
        AnnotationsManifest annotationsManifest = verifiable.getAnnotationsManifest().getRight();
        return annotationsManifest.getSingleAnnotationManifestReferences();
    }

    private void processAnnotationVerification(ResultHolder holder, Pair<SignatureContent, FileReference> verifiable) {
        try {
            singleAnnotationManifestExistenceRule.verify(holder, verifiable);
            singleAnnotationManifestIntegrityRule.verify(holder, verifiable);

            annotationDataExistenceRule.verify(holder, verifiable);
            annotationDataIntegrityRule.verify(holder, verifiable);
        } catch (RuleTerminatingException e) {
            LOGGER.info("Halting verification chain for annotation!, caused by '{}'", e.getMessage());
            return;
        }
    }

}
